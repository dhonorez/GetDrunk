package com.truvo.getdrunk.elasticsearch.index;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.truvo.getdrunk.elasticsearch.index.bds.BDSServiceRestImpl;
import com.truvo.getdrunk.elasticsearch.index.bds.domain.BDSAddress;
import com.truvo.getdrunk.elasticsearch.index.bds.domain.BDSBusinessData;
import com.truvo.getdrunk.elasticsearch.index.bds.domain.BDSEmail;
import com.truvo.getdrunk.elasticsearch.index.bds.domain.BDSFax;
import com.truvo.getdrunk.elasticsearch.index.bds.domain.BDSHeading;
import com.truvo.getdrunk.elasticsearch.index.bds.domain.BDSHomepage;
import com.truvo.getdrunk.elasticsearch.index.bds.domain.BDSLanguage;
import com.truvo.getdrunk.elasticsearch.index.bds.domain.BDSMobile;
import com.truvo.getdrunk.elasticsearch.index.bds.domain.BDSPhone;
import com.truvo.getdrunk.elasticsearch.index.heading.HeadingService;
import com.truvo.getdrunk.elasticsearch.index.heading.domain.Heading;
import com.truvo.getdrunk.elasticsearch.index.locality.LocalityService;
import com.truvo.getdrunk.elasticsearch.index.locality.domain.Locality;
import com.truvo.getdrunk.elasticsearch.index.locality.domain.Location;

public class BusinessDataAggregator {

	private static final List<String> languageIsoList = Arrays.asList("nl", "fr", "en");

	// TODO
	// OH TOEVOEGEN
	// DE ANDERE LOCALITIES TOEVOEGEN (via csm)

	private BDSServiceRestImpl bdsService;
	private HeadingService headingService;
	private LocalityService localityService;

	public BusinessDataAggregator(BDSServiceRestImpl bdsService, HeadingService headingService, LocalityService localityService) {
		super();
		this.bdsService = bdsService;
		this.headingService = headingService;
		this.localityService = localityService;
	}

	public Business aggregateBusinessData(Long advertiserId, Long businessId) {
		BDSBusinessData bdsData = bdsService.retrieveBusinessBaseData(advertiserId, businessId);
		Business business = new Business(createBusinessId(advertiserId, businessId));
		business.setAdvertiserId(advertiserId);
		business.setBusinessId(businessId);

		addNames(business, bdsData.getBusinessName());

		addHeadings(business, bdsData.getHeadings());

		addAddresses(business, bdsData.getAddresses());

		addPhoneNumbers(business, bdsData.getPhoneList());
		addMobileNumbers(business, bdsData.getMobileList());
		addFaxNumbers(business, bdsData.getFaxList());
		addEmails(business, bdsData.getEmailList());
		addWebsites(business, bdsData.getHomepageList());

		// addRatingAndReviewInfo(business);

		return business;
	}

	private void addNames(Business business, Map<BDSLanguage, String> businessNames) {
		for (BDSLanguage lang : businessNames.keySet()) {
			business.getBusinessNames().put(lang.toString(), businessNames.get(lang));
		}
	}

	private void addHeadings(Business business, List<BDSHeading> headings) {
		business.getHeadings().put("nl", new ArrayList<String>());
		business.getHeadings().put("fr", new ArrayList<String>());
		business.getHeadings().put("en", new ArrayList<String>());

		for (BDSHeading heading : headings) {
			String headingCode = heading.getHeadingCode();
			final Heading headingDto = headingService.getHeadingByCode(headingCode);
			for (String languageIso : languageIsoList) {
				business.getHeadings().get(languageIso).add(headingDto.getOfficialNames().get(languageIso));
			}
		}

	}

	private void addAddresses(Business business, Map<BDSLanguage, BDSAddress> addresses) {
		for (BDSLanguage lang : addresses.keySet()) {
			BDSAddress bdsAddress = addresses.get(lang);
			Address address = convertAddress(bdsAddress, lang);
			business.getAddresses().put(lang.toString(), address);
		}
	}

	private Address convertAddress(BDSAddress bdsAddress, BDSLanguage lang) {
		Address address = new Address();
		address.setBusnumber(bdsAddress.getBusnumber());
		address.setCountryCode(bdsAddress.getCountryCode());
		address.setHousenumber(bdsAddress.getHousenumber());
		address.setSubmunicipality(bdsAddress.getLocality());
		address.setPostalcode(bdsAddress.getPostalcode());
		address.setStreet(bdsAddress.getStreet());
		address.setxCoord(bdsAddress.getxCoord());
		address.setyCoord(bdsAddress.getyCoord());

		addLocalities(address, lang);
		return address;
	}

	private void addLocalities(Address address, BDSLanguage lang) {
		Location location = localityService.getLocationByPostalCode(address.getStreet(), address.getPostalcode(), address.getSubmunicipality(), 3);
		if (location != null) {
			address.setSubmunicipality(getLocalityOfficialName(location.getSubMunicipality(), getLanguageIso(lang)));
			address.setMunicipality(getLocalityOfficialName(location.getMunicipality(), getLanguageIso(lang)));
			address.setProvince(getLocalityOfficialName(location.getProvince(), getLanguageIso(lang)));
			address.setRegion(getLocalityOfficialName(location.getRegion(), getLanguageIso(lang)));
		}
	}

	private String getLocalityOfficialName(Locality locality, String languageIso) {
		if (locality != null && locality.getOfficialNames() != null) {
			return locality.getOfficialNames().get(languageIso.toUpperCase());
		}
		return null;
	}

	private String getLanguageIso(BDSLanguage lang) {
		if (lang == BDSLanguage.DUTCH) {
			return "nl";
		} else if (lang == BDSLanguage.FRENCH) {
			return "fr";
		} else if (lang == BDSLanguage.ENGLISH) {
			return "en";
		}
		return null;
	}

	private void addPhoneNumbers(Business business, List<BDSPhone> phoneList) {
		for (BDSPhone phone : phoneList) {
			business.getPhoneNumbers().add(phone.getValue());
		}
	}

	private void addMobileNumbers(Business business, List<BDSMobile> mobileList) {
		for (BDSMobile mobile : mobileList) {
			business.getMobileNumbers().add(mobile.getValue());
		}
	}

	private void addFaxNumbers(Business business, List<BDSFax> faxList) {
		for (BDSFax fax : faxList) {
			business.getFaxNumbers().add(fax.getValue());
		}
	}

	private void addEmails(Business business, List<BDSEmail> emailList) {
		for (BDSEmail email : emailList) {
			business.getEmails().add(email.getValue());
		}
	}

	private void addWebsites(Business business, List<BDSHomepage> homepageList) {
		for (BDSHomepage website : homepageList) {
			business.getWebsites().add(website.getValue());
		}
	}

	private String createBusinessId(Long advertiserId, Long businessId) {
		return advertiserId + "_" + businessId;
	}

}
