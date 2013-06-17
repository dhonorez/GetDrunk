package com.truvo.getdrunk.elasticsearch.index.bds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.activemq.memory.LRUMap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestOperations;

import com.truvo.getdrunk.elasticsearch.index.bds.domain.BDSAddress;
import com.truvo.getdrunk.elasticsearch.index.bds.domain.BDSAdvertiserStatus;
import com.truvo.getdrunk.elasticsearch.index.bds.domain.BDSBusinessData;
import com.truvo.getdrunk.elasticsearch.index.bds.domain.BDSEmail;
import com.truvo.getdrunk.elasticsearch.index.bds.domain.BDSFax;
import com.truvo.getdrunk.elasticsearch.index.bds.domain.BDSFolder;
import com.truvo.getdrunk.elasticsearch.index.bds.domain.BDSHeading;
import com.truvo.getdrunk.elasticsearch.index.bds.domain.BDSHomepage;
import com.truvo.getdrunk.elasticsearch.index.bds.domain.BDSLanguage;
import com.truvo.getdrunk.elasticsearch.index.bds.domain.BDSMobile;
import com.truvo.getdrunk.elasticsearch.index.bds.domain.BDSPhone;
import com.truvo.schema.basedata.AddressGeoDetailsType;
import com.truvo.schema.basedata.AddressTranslationType;
import com.truvo.schema.basedata.BusinessNameType;
import com.truvo.schema.basedata.BusinessNameValueType;
import com.truvo.schema.basedata.ContactMechanismBaseType;
import com.truvo.schema.basedata.ContactMechanismLabelType;
import com.truvo.schema.basedata.ContactMechanismUseTypeEnum;
import com.truvo.schema.basedata.DialNumberContactMechanismType;
import com.truvo.schema.basedata.EmailContactMechanismType;
import com.truvo.schema.basedata.EnterpriseReferenceType;
import com.truvo.schema.basedata.FlagType;
import com.truvo.schema.basedata.HeadingReferenceType;
import com.truvo.schema.basedata.LanguageType;
import com.truvo.schema.basedata.OrganizationProfileType;
import com.truvo.schema.basedata.PartyIdentityUriType;
import com.truvo.schema.basedata.PartyProfileHolderType;
import com.truvo.schema.basedata.PartyScopedBusinessNameHolderType;
import com.truvo.schema.basedata.PartySiteIdentityUriType;
import com.truvo.schema.basedata.PartySiteMemberRefType;
import com.truvo.schema.basedata.PartySiteScopedBusinessNameHolderType;
import com.truvo.schema.basedata.RelationshipTypeEnum;
import com.truvo.schema.basedata.RelationshipWithMembersReferenceType;
import com.truvo.schema.basedata.RelationshipsHolderUriType;
import com.truvo.schema.basedata.RetrievePartyResponse;
import com.truvo.schema.basedata.RetrievePartySiteResponse;
import com.truvo.schema.basedata.SearchPartySiteResponse;
import com.truvo.schema.basedata.SearchRelationshipMembersResponse;
import com.truvo.schema.basedata.SearchRelationshipsResponse;
import com.truvo.schema.basedata.WebSiteContactMechanismType;

public class BDSServiceRestImpl {

	private static final Logger logger = LoggerFactory.getLogger(BDSServiceRestImpl.class);

	private RestOperations restTemplate;
	private String baseUri = "";

	@SuppressWarnings("unchecked")
	private Map<AdvertiserBusinessKey, BDSBusinessData> bdsDataCache = Collections.synchronizedMap(new LRUMap(1000));
	private boolean useCache = false;

	public BDSServiceRestImpl(RestOperations restTemplate, String baseUri) {
		super();
		this.restTemplate = restTemplate;
		this.baseUri = baseUri;
	}

	private class AdvertiserBusinessKey {
		private long advertiserId;
		private long businessId;

		public AdvertiserBusinessKey(long advertiserId, long businessId) {
			this.advertiserId = advertiserId;
			this.businessId = businessId;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + (int) (advertiserId ^ (advertiserId >>> 32));
			result = prime * result + (int) (businessId ^ (businessId >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			AdvertiserBusinessKey other = (AdvertiserBusinessKey) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (advertiserId != other.advertiserId)
				return false;
			if (businessId != other.businessId)
				return false;
			return true;
		}

		private BDSServiceRestImpl getOuterType() {
			return BDSServiceRestImpl.this;
		}
	}

	public BDSBusinessData retrieveBusinessBaseData(long advertiserId, long businessId) {
		if (useCache) {
			final BDSBusinessData cachedResult = bdsDataCache.get(new AdvertiserBusinessKey(advertiserId, businessId));
			if (cachedResult != null) {
				return cachedResult;
			}
		}

		/** rest call for the business info **/
		Map<String, String> uriVariables = new HashMap<String, String>();
		uriVariables.put("partyClassification", "ORGANIZATION");
		uriVariables.put("partyPublicId", Long.toString(advertiserId));
		uriVariables.put("partySitePublicId", Long.toString(businessId));
		String uri = baseUri + "/api/v1/parties/{partyClassification}/{partyPublicId}/partysites/{partySitePublicId}";

		try {
			RetrievePartySiteResponse partySiteResponse = restTemplate.getForObject(uri, RetrievePartySiteResponse.class, uriVariables);
			BDSBusinessData result = createBusinessData(partySiteResponse, advertiserId, businessId);

			if (useCache) {
				bdsDataCache.put(new AdvertiserBusinessKey(advertiserId, businessId), result);
			}

			return result;
		} catch (RuntimeException e) {
			return null;
		}
	}

	private BDSBusinessData createBusinessData(RetrievePartySiteResponse partySiteResponse, Long advertiserId, Long businessId) {
		BDSBusinessData businessData = new BDSBusinessData();
		businessData.setAdvertiserId(advertiserId);
		businessData.setBusinessId(businessId);

		/** rest call for advertiser info **/
		RetrievePartyResponse partyResponse = null;
		PartyIdentityUriType partyReference = partySiteResponse.getPartyReference();
		if (partyReference != null) {
			String uri = partyReference.getUri();
			// /api/v1/parties/{partyClassification}/{partyPublicId}
			try {
				partyResponse = restTemplate.getForObject(uri, RetrievePartyResponse.class);

				createVAT(partyResponse, businessData);
				createAdvertiserStatus(partyResponse, businessData);
			} catch (RuntimeException e) {
			}
		}

		createBusinessNames(partySiteResponse, partyResponse, businessData);
		createAlternativeNames(partySiteResponse, businessData);

		createAddresses(partySiteResponse, businessData);

		createPhoneList(partySiteResponse, businessData);
		createMobileList(partySiteResponse, businessData);
		createFaxList(partySiteResponse, businessData);
		createEmailList(partySiteResponse, businessData);
		createHomepageList(partySiteResponse, businessData);

		createHeadings(partySiteResponse, businessData);

		/** create folder data **/
		RelationshipsHolderUriType participatingRelationShipsReference = partySiteResponse.getParticipatingRelationshipsReference();
		if (participatingRelationShipsReference != null) {
			String uri = participatingRelationShipsReference.getUri();
			// /api/v1/parties/{partyClassification}/{partyPublicId}/partysites/{partySitePublicId}/relationships
			try {
				SearchRelationshipsResponse searchRelationshipsResponse = restTemplate.getForObject(uri, SearchRelationshipsResponse.class);
				createFolders(searchRelationshipsResponse, businessData);
			} catch (RuntimeException e) {
			}
		}

		return businessData;
	}

	private void createBusinessNames(RetrievePartySiteResponse partySiteResponse, RetrievePartyResponse partyResponse, BDSBusinessData businessData) {
		if (partySiteResponse != null) {
			PartySiteScopedBusinessNameHolderType businessNames = partySiteResponse.getBusinessNames();

			if (businessNames != null) {
				// first fill in with available official names (lowest priority)
				createOfficialBusinessnames(partyResponse, businessData, businessNames);

				// overwrite with the publication names because they have a higher priority than the official names.
				createDisplayBusinessnames(businessData, businessNames);

				// if there is no data for a locale (*), use data from another locale on (*)
				createBusinessnamesForMissingLanguages(businessData);

			}
		}
	}

	private void createBusinessnamesForMissingLanguages(BDSBusinessData businessData) {
		BDSLanguage priorityLanguage = null;
		List<BDSLanguage> unfoundLanguages = new ArrayList<BDSLanguage>();
		unfoundLanguages.add(BDSLanguage.DUTCH);
		unfoundLanguages.add(BDSLanguage.FRENCH);
		unfoundLanguages.add(BDSLanguage.ENGLISH);
		final Iterator<BDSLanguage> it = unfoundLanguages.iterator();
		while (it.hasNext()) {
			final BDSLanguage bdsLang = it.next();
			if (businessData.getBusinessName().get(bdsLang) != null) {
				if (priorityLanguage == null) {
					priorityLanguage = bdsLang;
				}
				it.remove();
			}
		}
		for (BDSLanguage unfoundLang : unfoundLanguages) {
			businessData.getBusinessName().put(unfoundLang, businessData.getBusinessName().get(priorityLanguage));
		}
	}

	private void createDisplayBusinessnames(BDSBusinessData businessData, PartySiteScopedBusinessNameHolderType businessNames) {
		if (businessNames.getDisplayBusinessName() != null && businessNames.getDisplayBusinessName().getValue() != null) {
			for (BusinessNameValueType businessNameValueType : businessNames.getDisplayBusinessName().getValue()) {
				if (businessNameValueType != null) {
					if (businessNameValueType.getLanguage() == LanguageType.NL) {
						businessData.getBusinessName().put(BDSLanguage.DUTCH, businessNameValueType.getContent());
					} else if (businessNameValueType.getLanguage() == LanguageType.FR) {
						businessData.getBusinessName().put(BDSLanguage.FRENCH, businessNameValueType.getContent());
					} else if (businessNameValueType.getLanguage() == LanguageType.EN) {
						businessData.getBusinessName().put(BDSLanguage.ENGLISH, businessNameValueType.getContent());
					}
				}
			}
		}
	}

	private void createOfficialBusinessnames(RetrievePartyResponse partyResponse, BDSBusinessData businessData,
			PartySiteScopedBusinessNameHolderType businessNames) {
		if (businessNames.getOfficialBusinessName() != null && businessNames.getOfficialBusinessName().getValue() != null) {
			String legalForm = null;
			if (partyResponse != null && partyResponse.getProfile() != null && partyResponse.getProfile().getOrganizationProfile() != null
					&& partyResponse.getProfile().getOrganizationProfile().getDefaultEnterpriseReference() != null) {
				legalForm = partyResponse.getProfile().getOrganizationProfile().getDefaultEnterpriseReference().getLegalFormDisplayValue();
			}

			for (BusinessNameValueType businessNameValueType : businessNames.getOfficialBusinessName().getValue()) {
				if (businessNameValueType != null) {
					String fullName = businessNameValueType.getContent();
					if (!StringUtils.isBlank(legalForm)) {
						fullName = fullName.concat(" " + legalForm);
					}

					if (businessNameValueType.getLanguage() == LanguageType.NL) {
						businessData.getBusinessName().put(BDSLanguage.DUTCH, fullName);
					} else if (businessNameValueType.getLanguage() == LanguageType.FR) {
						businessData.getBusinessName().put(BDSLanguage.FRENCH, fullName);
					} else if (businessNameValueType.getLanguage() == LanguageType.EN) {
						businessData.getBusinessName().put(BDSLanguage.ENGLISH, fullName);
					}
				}
			}
		}
	}

	private void createAlternativeNames(RetrievePartySiteResponse partySiteResponse, BDSBusinessData businessData) {
		if (partySiteResponse != null) {
			PartySiteScopedBusinessNameHolderType businessNames = partySiteResponse.getBusinessNames();

			businessData.getAlternativeNames().put(BDSLanguage.DUTCH, new ArrayList<String>());
			businessData.getAlternativeNames().put(BDSLanguage.FRENCH, new ArrayList<String>());
			businessData.getAlternativeNames().put(BDSLanguage.ENGLISH, new ArrayList<String>());

			if (businessNames != null && businessNames.getAlternateBusinessName() != null && businessNames.getAlternateBusinessName().getValue() != null) {
				for (BusinessNameValueType businessNameValueType : businessNames.getAlternateBusinessName().getValue()) {
					String altName = businessNameValueType.getContent();
					if (businessNameValueType.getLanguage() == LanguageType.NL) {
						businessData.getAlternativeNames().get(BDSLanguage.DUTCH).add(altName);
					} else if (businessNameValueType.getLanguage() == LanguageType.FR) {
						businessData.getAlternativeNames().get(BDSLanguage.FRENCH).add(altName);
					} else if (businessNameValueType.getLanguage() == LanguageType.EN) {
						businessData.getAlternativeNames().get(BDSLanguage.ENGLISH).add(altName);
					}
				}
			}
		}
	}

	private void createAddresses(RetrievePartySiteResponse partySiteResponse, BDSBusinessData businessData) {
		if (partySiteResponse != null) {
			BDSAddress addressNl = null;
			BDSAddress addressFr = null;
			BDSAddress addressEn = null;
			BDSAddress addressOther = null;

			// first fill in with available official address (lowest priority)
			if (partySiteResponse.getOfficialAddress() != null && partySiteResponse.getOfficialAddress().getAddressTranslation() != null) {
				for (AddressTranslationType addressTranslationType : partySiteResponse.getOfficialAddress().getAddressTranslation()) {
					switch (addressTranslationType.getLanguage()) {
					case NL:
						addressNl = fillInAddressDetails(addressTranslationType);
						break;
					case FR:
						addressFr = fillInAddressDetails(addressTranslationType);
						break;
					case EN:
						addressEn = fillInAddressDetails(addressTranslationType);
						break;
					default:
						addressOther = fillInAddressDetails(addressTranslationType);

					}
				}
			}

			// overwrite with the display address because they have a higher priority than the official names.
			if (partySiteResponse.getDisplayAddress() != null && partySiteResponse.getDisplayAddress().getAddressTranslation() != null) {
				for (AddressTranslationType addressTranslationType : partySiteResponse.getDisplayAddress().getAddressTranslation()) {
					switch (addressTranslationType.getLanguage()) {
					case NL:
						addressNl = fillInAddressDetails(addressTranslationType);
						break;
					case FR:
						addressFr = fillInAddressDetails(addressTranslationType);
						break;
					case EN:
						addressEn = fillInAddressDetails(addressTranslationType);
						break;
					default:
						addressOther = fillInAddressDetails(addressTranslationType);
					}
				}
			}

			// fill missing address Language by filled languages
			if (addressFr == null) {
				if (addressNl != null) {
					addressFr = addressNl;
				} else if (addressEn != null) {
					addressFr = addressEn;
				} else {
					addressFr = addressOther;
				}
			}

			if (addressEn == null) {
				if (addressNl != null) {
					addressEn = addressNl;
				} else if (addressFr != null) {
					addressEn = addressFr;
				} else {
					addressEn = addressOther;
				}
			}

			if (addressNl == null) {
				if (addressFr != null) {
					addressNl = addressFr;
				} else if (addressEn != null) {
					addressNl = addressEn;
				} else {
					addressNl = addressOther;
				}
			}

			// only for xcoord and ycoord overwrite with geo address (highest priority for xcoord and ycoord)
			if (partySiteResponse.getGeoAddress() != null && partySiteResponse.getGeoAddress().getAddressTranslation() != null) {
				for (AddressTranslationType addressTranslationType : partySiteResponse.getGeoAddress().getAddressTranslation()) {
					AddressGeoDetailsType geoDetails = addressTranslationType.getGeoDetails();
					switch (addressTranslationType.getLanguage()) {
					case NL:
						addressNl.setxCoord(geoDetails.getCoordinates().getLongitude());
						addressNl.setyCoord(geoDetails.getCoordinates().getLatitude());
						break;
					case FR:
						addressFr.setxCoord(geoDetails.getCoordinates().getLongitude());
						addressFr.setyCoord(geoDetails.getCoordinates().getLatitude());
						break;
					case EN:
						addressEn.setxCoord(geoDetails.getCoordinates().getLongitude());
						addressEn.setyCoord(geoDetails.getCoordinates().getLatitude());
						break;
					default:
						addressOther.setxCoord(geoDetails.getCoordinates().getLongitude());
						addressOther.setyCoord(geoDetails.getCoordinates().getLatitude());
					}
				}
			}

			businessData.getAddresses().put(BDSLanguage.DUTCH, addressNl);
			businessData.getAddresses().put(BDSLanguage.FRENCH, addressFr);
			businessData.getAddresses().put(BDSLanguage.ENGLISH, addressEn);

			createAddressesForMissingLanguages(businessData, addressNl, addressFr, addressEn);
		}
	}

	private void createAddressesForMissingLanguages(BDSBusinessData businessData, BDSAddress addressNl, BDSAddress addressFr, BDSAddress addressEn) {
		// if there is no data for a locale (*), use data from another locale on (*)
		/*
		 * nl : 1 fr ; 2 en fr : 1 nl ; 2 en en : 1 fr ; 2 nl
		 */
		// BDSLanguage priorityLanguage = null;
		List<BDSLanguage> unfoundLanguages = new ArrayList<BDSLanguage>();
		unfoundLanguages.add(BDSLanguage.DUTCH);
		unfoundLanguages.add(BDSLanguage.FRENCH);
		unfoundLanguages.add(BDSLanguage.ENGLISH);

		final Iterator<BDSLanguage> it = unfoundLanguages.iterator();
		while (it.hasNext()) {
			final BDSLanguage next = it.next();
			if (businessData.getAddresses().get(next) == null) {
				switch (next) {
				case DUTCH:
					if (addressFr != null) {
						businessData.getAddresses().put(next, addressFr);
					} else {
						businessData.getAddresses().put(next, addressEn);
					}
					break;
				case FRENCH:
					if (addressNl != null) {
						businessData.getAddresses().put(next, addressNl);
					} else {
						businessData.getAddresses().put(next, addressEn);
					}
					break;
				case ENGLISH:
					if (addressFr != null) {
						businessData.getAddresses().put(next, addressFr);
					} else {
						businessData.getAddresses().put(next, addressNl);
					}
					break;
				}
			}
		}
	}

	private BDSAddress fillInAddressDetails(AddressTranslationType addressTranslationType) {
		BDSAddress address = new BDSAddress();
		if (addressTranslationType != null) {
			address.setStreet(addressTranslationType.getStreet());
			address.setHousenumber(addressTranslationType.getHouseNumber());
			address.setBusnumber(addressTranslationType.getHouseNumberSuffix());
			address.setPostalcode(addressTranslationType.getPostalCode());
			address.setLocality(addressTranslationType.getLocality());
			address.setCountryCode(addressTranslationType.getCountry().toString());
			AddressGeoDetailsType geoDetails = addressTranslationType.getGeoDetails();
			if (geoDetails != null && geoDetails.isResolved() && geoDetails.getCoordinates() != null) {
				address.setxCoord(geoDetails.getCoordinates().getLongitude());
				address.setyCoord(geoDetails.getCoordinates().getLatitude());
			}
			return address;
		}
		return null;
	}

	private void createPhoneList(RetrievePartySiteResponse partySiteResponse, BDSBusinessData businessData) {
		if (partySiteResponse != null && partySiteResponse.getContactMechanisms() != null) {
			for (DialNumberContactMechanismType dialNumberType : partySiteResponse.getContactMechanisms().getPhone()) {

				if (dialNumberType.getUse().contains(ContactMechanismUseTypeEnum.OFFICIAL_BUSINESS_TYPE)) {
					String areaCode = dialNumberType.getAreaCode();
					String countryCode = dialNumberType.getCountryCode();
					String phoneNumber = dialNumberType.getPhoneNumber();
					String fullPhoneNumber = ContactMechanismFormatter.formatPhone(countryCode, areaCode, phoneNumber);

					int priority = dialNumberType.getPriority();

					Map<BDSLanguage, String> bdsLabel = createBdsLabel(dialNumberType);

					createBdsLabelsMissingLanguages(bdsLabel);

					BDSPhone phone = new BDSPhone(priority, ContactMechanismUseTypeEnum.FREE.toString(), bdsLabel, fullPhoneNumber);

					List<String> bdsHeadingRestriction = new ArrayList<String>();
					for (HeadingReferenceType headingRestriction : dialNumberType.getHeadingRestriction()) {
						if (isHeadingResolved(headingRestriction)) {
							bdsHeadingRestriction.add(headingRestriction.getCode());
						}
					}
					phone.setHeadingRestriction(bdsHeadingRestriction);

					businessData.getPhoneList().add(phone);
				}
			}
		}
	}

	private void createBdsLabelsMissingLanguages(Map<BDSLanguage, String> bdsLabel) {
		// if there is no data for a locale (*), use data from another locale on (*)
		BDSLanguage priorityLanguage = null;
		List<BDSLanguage> unfoundLanguages = new ArrayList<BDSLanguage>();
		unfoundLanguages.add(BDSLanguage.DUTCH);
		unfoundLanguages.add(BDSLanguage.FRENCH);
		unfoundLanguages.add(BDSLanguage.ENGLISH);

		final Iterator<BDSLanguage> it = unfoundLanguages.iterator();
		while (it.hasNext()) {
			final BDSLanguage bdsLang = it.next();
			if (bdsLabel.get(bdsLang) != null) {
				if (priorityLanguage == null) {
					priorityLanguage = bdsLang;
				}
				it.remove();
			}
		}
		for (BDSLanguage unfoundLang : unfoundLanguages) {
			bdsLabel.put(unfoundLang, bdsLabel.get(priorityLanguage));
		}
	}

	private void createMobileList(RetrievePartySiteResponse partySiteResponse, BDSBusinessData businessData) {
		if (partySiteResponse != null && partySiteResponse.getContactMechanisms() != null) {
			for (DialNumberContactMechanismType dialNumberType : partySiteResponse.getContactMechanisms().getMobile()) {
				ContactMechanismUseTypeEnum useType = null;
				if (dialNumberType.getUse() != null && dialNumberType.getUse().size() > 0 && dialNumberType.getUse().get(0) != null) {
					useType = dialNumberType.getUse().get(0);
				}

				if (useType != ContactMechanismUseTypeEnum.KEYED_AD && useType != ContactMechanismUseTypeEnum.OFFICIAL_ADVERTISER_TYPE
						&& useType != ContactMechanismUseTypeEnum.BILL_TO) {
					String areaCode = dialNumberType.getAreaCode();
					String countryCode = dialNumberType.getCountryCode();
					String mobileNumber = dialNumberType.getPhoneNumber();
					String fullMobileNumber = ContactMechanismFormatter.formatPhone(countryCode, areaCode, mobileNumber);

					int priority = dialNumberType.getPriority();

					Map<BDSLanguage, String> bdsLabel = createBdsLabel(dialNumberType);

					createBdsLabelsMissingLanguages(bdsLabel);

					BDSMobile mobile = new BDSMobile(priority, useType.toString(), bdsLabel, fullMobileNumber);

					List<String> bdsHeadingRestriction = new ArrayList<String>();
					for (HeadingReferenceType headingRestriction : dialNumberType.getHeadingRestriction()) {
						if (isHeadingResolved(headingRestriction)) {
							bdsHeadingRestriction.add(headingRestriction.getCode());
						}
					}
					mobile.setHeadingRestriction(bdsHeadingRestriction);

					businessData.getMobileList().add(mobile);
				}
			}
		}
	}

	private void createFaxList(RetrievePartySiteResponse partySiteResponse, BDSBusinessData businessData) {
		if (partySiteResponse != null && partySiteResponse.getContactMechanisms() != null) {
			for (DialNumberContactMechanismType dialNumberType : partySiteResponse.getContactMechanisms().getFax()) {
				ContactMechanismUseTypeEnum useType = null;
				if (dialNumberType.getUse() != null) {
					if (dialNumberType.getUse().size() > 0) {
						if (dialNumberType.getUse().get(0) != null) {
							useType = dialNumberType.getUse().get(0);
						}
					}
				}

				if (useType != ContactMechanismUseTypeEnum.KEYED_AD) {
					String areaCode = dialNumberType.getAreaCode();
					String countryCode = dialNumberType.getCountryCode();
					String faxNumber = dialNumberType.getPhoneNumber();
					String fullFaxNumber = ContactMechanismFormatter.formatPhone(countryCode, areaCode, faxNumber);

					int priority = dialNumberType.getPriority();

					Map<BDSLanguage, String> bdsLabel = createBdsLabel(dialNumberType);

					BDSFax fax = new BDSFax(priority, useType.toString(), bdsLabel, fullFaxNumber);

					List<String> bdsHeadingRestriction = new ArrayList<String>();
					for (HeadingReferenceType headingRestriction : dialNumberType.getHeadingRestriction()) {
						if (isHeadingResolved(headingRestriction)) {
							bdsHeadingRestriction.add(headingRestriction.getCode());
						}
					}
					fax.setHeadingRestriction(bdsHeadingRestriction);

					businessData.getFaxList().add(fax);
				}
			}
		}
	}

	private void createEmailList(RetrievePartySiteResponse partySiteResponse, BDSBusinessData businessData) {
		if (partySiteResponse != null && partySiteResponse.getContactMechanisms() != null) {
			for (EmailContactMechanismType emailType : partySiteResponse.getContactMechanisms().getEmail()) {
				String emailValue = emailType.getEmail();
				int priority = emailType.getPriority();

				String useType = "F";

				Map<BDSLanguage, String> bdsLabel = createBdsLabel(emailType);
				BDSEmail email = new BDSEmail(priority, useType, bdsLabel, emailValue);

				List<String> bdsHeadingRestriction = new ArrayList<String>();
				for (HeadingReferenceType headingRestriction : emailType.getHeadingRestriction()) {
					if (isHeadingResolved(headingRestriction)) {
						bdsHeadingRestriction.add(headingRestriction.getCode());
					}
				}
				email.setHeadingRestriction(bdsHeadingRestriction);

				businessData.getEmailList().add(email);
			}
		}
	}

	private Map<BDSLanguage, String> createBdsLabel(ContactMechanismBaseType contactMechanismType) {
		Map<BDSLanguage, String> bdsLabel = new HashMap<BDSLanguage, String>();
		for (ContactMechanismLabelType label : contactMechanismType.getLabel()) {
			String labelValue = label.getContent();

			switch (label.getLanguage()) {
			case NL:
				bdsLabel.put(BDSLanguage.DUTCH, labelValue);
				break;
			case FR:
				bdsLabel.put(BDSLanguage.FRENCH, labelValue);
				break;
			case EN:
				bdsLabel.put(BDSLanguage.ENGLISH, labelValue);
				break;
			default:
			}
		}
		return bdsLabel;
	}

	private void createHomepageList(RetrievePartySiteResponse partySiteResponse, BDSBusinessData businessData) {
		if (partySiteResponse != null && partySiteResponse.getContactMechanisms() != null) {
			for (WebSiteContactMechanismType webSiteType : partySiteResponse.getContactMechanisms().getWebSite()) {
				String url = webSiteType.getUrl();
				int priority = webSiteType.getPriority();

				String useType = "F";

				Map<BDSLanguage, String> bdsLabel = createBdsLabel(webSiteType);

				boolean valid = isValidWebsite(webSiteType.getUtmAccept(), webSiteType.getValid());

				BDSHomepage homepage = new BDSHomepage(priority, useType, bdsLabel, url, valid);

				List<String> bdsHeadingRestriction = new ArrayList<String>();
				if (webSiteType.getHeadingRestriction() != null) {
					for (HeadingReferenceType headingRestriction : webSiteType.getHeadingRestriction()) {
						if (isHeadingResolved(headingRestriction)) {
							bdsHeadingRestriction.add(headingRestriction.getCode());
						}
					}
				}
				homepage.setHeadingRestriction(bdsHeadingRestriction);

				businessData.getHomepageList().add(homepage);
			}
		}
	}

	private boolean isValidWebsite(FlagType utmAccept, FlagType valid) {
		return utmAccept == FlagType.TRUE && valid == FlagType.TRUE;
	}

	private void createVAT(RetrievePartyResponse partyResponse, BDSBusinessData businessData) {
		if (partyResponse != null) {
			PartyProfileHolderType profile = partyResponse.getProfile();
			if (profile != null) {
				OrganizationProfileType organizationProfile = profile.getOrganizationProfile();
				if (organizationProfile != null) {
					EnterpriseReferenceType defaultEnterpriseReference = organizationProfile.getDefaultEnterpriseReference();
					if (defaultEnterpriseReference != null) {
						businessData.setVat(defaultEnterpriseReference.getVatNumber());
					}
				}
			}
		}

	}

	private void createAdvertiserStatus(RetrievePartyResponse partyResponse, BDSBusinessData businessData) {
		if (partyResponse != null && partyResponse.getDetailStatus() != null) {
			String code = partyResponse.getDetailStatus().getStatusCode();
			Date date = partyResponse.getDetailStatus().getStatusDate().toGregorianCalendar().getTime();
			businessData.setAdvertiserStatus(new BDSAdvertiserStatus(code, date));
		}
	}

	private void createHeadings(RetrievePartySiteResponse partySiteResponse, BDSBusinessData businessData) {
		if (partySiteResponse != null && partySiteResponse.getHeadingReference() != null) {
			for (HeadingReferenceType heading : partySiteResponse.getHeadingReference()) {
				if (isHeadingResolved(heading)) {
					String headingCode = heading.getCode();
					Integer priority = heading.getPriority();
					BDSHeading bdsHeading = new BDSHeading(priority, headingCode);
					businessData.getHeadings().add(bdsHeading);
				}
			}
		}
	}

	private void createFolders(SearchRelationshipsResponse searchRelationshipsResponse, BDSBusinessData businessData) {
		try {
			if (searchRelationshipsResponse != null && searchRelationshipsResponse.getRelationship() != null) {
				for (RelationshipWithMembersReferenceType relationShipWithMembersReferenceType : searchRelationshipsResponse.getRelationship()) {
					if (relationShipWithMembersReferenceType != null
							&& relationShipWithMembersReferenceType.getType().equals(RelationshipTypeEnum.MEMBER_OF_FOLDER)
							&& relationShipWithMembersReferenceType.getObjectParty() != null) {
						String mainOfficeId = relationShipWithMembersReferenceType.getPartySiteMemberDefault().getPublicId();
						String uri = relationShipWithMembersReferenceType.getObjectParty().getUri();
						boolean mainoffice = false;
						if (businessData.getBusinessId().toString().equalsIgnoreCase(mainOfficeId)) {
							mainoffice = true;
						}
						/** /api/v1/relationships/{relationshipId}/members **/
						RetrievePartyResponse retrieveFolderResponse = restTemplate.getForObject(uri, RetrievePartyResponse.class);
						String folderId = retrieveFolderResponse.getPublicId();

						Map<BDSLanguage, String> folderNames = new HashMap<BDSLanguage, String>();
						PartyScopedBusinessNameHolderType businessNames = retrieveFolderResponse.getBusinessNames();
						if (businessNames != null) {
							BusinessNameType officialBusinessName = businessNames.getOfficialBusinessName();
							addFolderName(folderNames, officialBusinessName);

							// overwrite with the display business names (higher priority)
							BusinessNameType displayBusinessName = businessNames.getDisplayBusinessName();
							addFolderName(folderNames, displayBusinessName);
						}
						createBdsLabelsMissingLanguages(folderNames);

						String advertiserId = null;
						String uriFolderRelationships = retrieveFolderResponse.getRelationshipsReference().getUri();
						SearchRelationshipsResponse searchFolderRelationshipsResponse = restTemplate.getForObject(uriFolderRelationships,
								SearchRelationshipsResponse.class);
						if (searchFolderRelationshipsResponse.getRelationship() != null) {
							for (RelationshipWithMembersReferenceType folderRelationship : searchFolderRelationshipsResponse.getRelationship()) {
								if (folderRelationship.getType().equals(RelationshipTypeEnum.PAYER_OF_FOLDER) && folderRelationship.getSubjectParty() != null) {
									advertiserId = folderRelationship.getSubjectParty().getPublicId();
								}
							}
						}

						if (advertiserId != null) {
							BDSFolder folder = new BDSFolder(Long.parseLong(advertiserId), Long.parseLong(folderId), mainoffice, folderNames);
							businessData.getFolders().add(folder);
						}
					}
				}
			}
		} catch (RuntimeException e) {
		}
	}

	private void addFolderName(Map<BDSLanguage, String> folderNames, BusinessNameType officialBusinessName) {
		if (officialBusinessName != null && officialBusinessName.getValue() != null) {
			for (BusinessNameValueType businessNameValueType : officialBusinessName.getValue()) {
				if (businessNameValueType != null && !StringUtils.isBlank(businessNameValueType.getContent())) {
					if (businessNameValueType.getLanguage() == LanguageType.NL) {
						folderNames.put(BDSLanguage.DUTCH, businessNameValueType.getContent());
					} else if (businessNameValueType.getLanguage() == LanguageType.FR) {
						folderNames.put(BDSLanguage.FRENCH, businessNameValueType.getContent());
					} else if (businessNameValueType.getLanguage() == LanguageType.EN) {
						folderNames.put(BDSLanguage.ENGLISH, businessNameValueType.getContent());
					}
				}
			}
		}
	}

	public List<BDSBusinessData> retrieveBusinessesForFolder(long folderId) {
		List<BDSBusinessData> result = new ArrayList<BDSBusinessData>();

		/** rest call for the folder relationship info (members) **/
		Map<String, String> uriVariables = new HashMap<String, String>();
		uriVariables.put("partyClassification", "GROUP");
		uriVariables.put("partyPublicId", Long.toString(folderId));
		String uri = baseUri + "/api/v1/parties/{partyClassification}/{partyPublicId}/relationships";
		SearchRelationshipsResponse searchRelationshipsResponse = restTemplate.getForObject(uri, SearchRelationshipsResponse.class, uriVariables);

		if (searchRelationshipsResponse != null && searchRelationshipsResponse.getRelationship() != null) {
			for (RelationshipWithMembersReferenceType relationship : searchRelationshipsResponse.getRelationship()) {
				if (relationship.getType() == RelationshipTypeEnum.MEMBER_OF_FOLDER) {
					result = getBusinessesForFolder(relationship.getPartySiteMembersUri());
				}
			}
		}

		return result;
	}

	private List<BDSBusinessData> getBusinessesForFolder(String partySiteMembersUri) {
		SearchRelationshipMembersResponse searchRelationshipMembersResponse = restTemplate.getForObject(partySiteMembersUri,
				SearchRelationshipMembersResponse.class);

		// create the list for this uri
		List<BDSBusinessData> localBusinessList = new ArrayList<BDSBusinessData>();
		if (searchRelationshipMembersResponse != null && searchRelationshipMembersResponse.getPartySiteMember() != null) {
			for (PartySiteMemberRefType partySiteMemberRefType : searchRelationshipMembersResponse.getPartySiteMember()) {
				String advertiserId = partySiteMemberRefType.getPartyPublicId();
				String businessId = partySiteMemberRefType.getPublicId();

				BDSBusinessData businessData = new BDSBusinessData();
				businessData.setAdvertiserId(Long.parseLong(advertiserId));
				businessData.setBusinessId(Long.parseLong(businessId));

				localBusinessList.add(businessData);
			}
		}

		// get the businesses for the next pages
		if (searchRelationshipMembersResponse != null && searchRelationshipMembersResponse.getHeader() != null
				&& searchRelationshipMembersResponse.getHeader().getNextPageUri() != null
				&& !StringUtils.isBlank(searchRelationshipMembersResponse.getHeader().getNextPageUri())) {

			List<BDSBusinessData> businessListOfNextPages = getBusinessesForFolder(searchRelationshipMembersResponse.getHeader().getNextPageUri());

			// add the local list to the list constructed by the next pages
			businessListOfNextPages.addAll(localBusinessList);
			return businessListOfNextPages;
		} else {
			return localBusinessList;
		}

	}

	public List<BDSBusinessData> retrieveBusinessesForAdvertiser(long advertiserId) {
		/** rest call for the businesses belonging to this advertiser **/
		Map<String, String> uriVariables = new HashMap<String, String>();
		uriVariables.put("partyClassification", "ORGANIZATION");
		uriVariables.put("partyPublicId", Long.toString(advertiserId));
		String uri = baseUri + "/api/v1/parties/{partyClassification}/{partyPublicId}/partysites";

		try {
			SearchPartySiteResponse searchPartySiteResponse = restTemplate.getForObject(uri, SearchPartySiteResponse.class, uriVariables);
			if (searchPartySiteResponse != null && searchPartySiteResponse.getPartySiteReference() != null) {
				List<BDSBusinessData> result = createBusinessDataListFromPartySiteRespons(searchPartySiteResponse.getPartySiteReference());
				if (searchPartySiteResponse.getHeader() != null && searchPartySiteResponse.getHeader().getNextPageUri() != null
						&& !StringUtils.isBlank(searchPartySiteResponse.getHeader().getNextPageUri())) {
					List<BDSBusinessData> businessesForNextPages = getBusinessesForAdvertiser(searchPartySiteResponse.getHeader().getNextPageUri());
					result.addAll(businessesForNextPages);
				}
				return result;
			}
		} catch (RuntimeException e) {
		}

		return new ArrayList<BDSBusinessData>();
	}

	private List<BDSBusinessData> getBusinessesForAdvertiser(String partySiteUri) {
		List<BDSBusinessData> result = new ArrayList<BDSBusinessData>();
		// get the businesses for this page
		SearchPartySiteResponse searchPartySiteResponse = restTemplate.getForObject(partySiteUri, SearchPartySiteResponse.class);
		if (searchPartySiteResponse != null && searchPartySiteResponse.getPartySiteReference() != null) {
			result.addAll(createBusinessDataListFromPartySiteRespons(searchPartySiteResponse.getPartySiteReference()));
		}

		// get the businesses for the next page
		if (searchPartySiteResponse != null && searchPartySiteResponse.getHeader() != null && searchPartySiteResponse.getHeader().getNextPageUri() != null
				&& !StringUtils.isBlank(searchPartySiteResponse.getHeader().getNextPageUri())) {
			result.addAll(getBusinessesForAdvertiser(searchPartySiteResponse.getHeader().getNextPageUri()));
		}

		return result;
	}

	private List<BDSBusinessData> createBusinessDataListFromPartySiteRespons(List<PartySiteIdentityUriType> partySiteReference) {
		List<BDSBusinessData> result = new ArrayList<BDSBusinessData>();
		for (PartySiteIdentityUriType partySiteIdentityUriType : partySiteReference) {
			if (partySiteIdentityUriType.isBusiness()) {
				String advertiserId = partySiteIdentityUriType.getPartyPublicId();
				String businessId = partySiteIdentityUriType.getPublicId();

				BDSBusinessData businessData = new BDSBusinessData();
				businessData.setAdvertiserId(Long.parseLong(advertiserId));
				businessData.setBusinessId(Long.parseLong(businessId));
				result.add(businessData);
			}
		}
		return result;
	}

	private boolean isHeadingResolved(HeadingReferenceType heading) {
		return heading != null && heading.isResolved();
	}

	public void setUseCache(boolean useCache) {
		this.useCache = useCache;
	}
}
