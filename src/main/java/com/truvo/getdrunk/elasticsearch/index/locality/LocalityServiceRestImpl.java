package com.truvo.getdrunk.elasticsearch.index.locality;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.UriBuilder;

import org.apache.activemq.memory.LRUMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import com.truvo.getdrunk.elasticsearch.index.locality.domain.Locality;
import com.truvo.getdrunk.elasticsearch.index.locality.domain.LocalityLevel;
import com.truvo.getdrunk.elasticsearch.index.locality.domain.Location;
import com.truvo.schema.csm.location._0.DisplayNameType;
import com.truvo.schema.csm.location._0.LocationInternalRefWithBasicDetailsType;
import com.truvo.schema.csm.location._0.Locations;
import com.truvo.schema.geolink.api.geolinkcommon._1.AddressMatchType;
import com.truvo.schema.geolink.api.geolinkcommon._1.ErrorType;
import com.truvo.schema.geolink.api.geolinksuggest._0.AddressSuggestionResponse;

public class LocalityServiceRestImpl implements LocalityService {

	private static final Logger logger = LoggerFactory.getLogger(LocalityServiceRestImpl.class);

	private static final String LOCATION_RESOURCE_URI = "/geo/locations/postalcode/";

	private static final Map<String, String> regionLanguageMap = new HashMap<String, String>();
	static {
		regionLanguageMap.put("Vlaamse Gewest", "nl");
		regionLanguageMap.put("Région flamande", "nl");
		regionLanguageMap.put("Flemish Region", "nl");
		regionLanguageMap.put("Waalse Gewest", "fr");
		regionLanguageMap.put("Région wallonne", "fr");
		regionLanguageMap.put("Walloon Region", "fr");
	}

	private RestTemplate restTemplate;
	private String csmServiceRoot; // http://csm.truvo.net.tst.lab.truvo.net/api/v1/BE

	private String geoLinkServiceRoot;
	private static final String GEOLINK_SUGGEST_PATH = "/suggest/?suggestStrategy=SUGGEST&languageCode=&streetName={street}=&houseNumber=&houseNumberSuffix=&postalCode={postalcode}2640&locality={locality}";

	private final boolean useGeolinkFallbackAddress = false;

	private final Map<PostalcodeSubmunicipality, Location> locationsCache;

	@SuppressWarnings("unchecked")
	public LocalityServiceRestImpl() {
		locationsCache = Collections.synchronizedMap(new LRUMap(3000));
	}

	@Override
	public Location getLocationByPostalCode(String street, String postalcode, String submunicipality, int retryAttempts) {

		final PostalcodeSubmunicipality postalcodeSubmunicipality = new PostalcodeSubmunicipality(postalcode, submunicipality);
		boolean addressCorrected = false;

		// check cache
		if (locationsCache.containsKey(postalcodeSubmunicipality)) {
			return (Location) locationsCache.get(postalcodeSubmunicipality);
		}

		// not found in cache -> do call

		Location result = null;
		try {
			List<LocationInternalRefWithBasicDetailsType> locationList = getLocationTree(postalcode, submunicipality);

			if (CollectionUtils.isEmpty(locationList)) {
				logger.debug("Missing postalCodeLocality combination: " + postalcodeSubmunicipality);

				if (useGeolinkFallbackAddress) {

					// get corrected value from geolink
					URI geoLinkURI = buildGeolinkURI(street, postalcode, submunicipality);
					AddressSuggestionResponse addressSuggest = restTemplate.getForObject(geoLinkURI, AddressSuggestionResponse.class);
					if (addressSuggest.getAddressMatch().size() > 0) {

						// use corrected value for new csm call
						// print warnings
						final AddressMatchType correctAddress = addressSuggest.getAddressMatch().get(0);
						if (correctAddress.getWarnings() != null) {
							for (ErrorType warning : correctAddress.getWarnings().getWarning()) {
								logger.warn("warning from geolink: " + warning.getDescription() + " for " + postalcodeSubmunicipality);
							}
						}

						String correctedPostalcode = correctAddress.getAddressDetails().getPostalCode();
						String correctedSubmunicipality = correctAddress.getAddressDetails().getLocality();
						locationList = getLocationTree(correctedPostalcode, correctedSubmunicipality);
						addressCorrected = true;

						if (CollectionUtils.isEmpty(locationList)) {
							return null;
						}
					} else {
						return null;
					}

				} else {
					return null;
				}
			}

			String regionLanguage = getRegionLanguage(locationList);

			result = new Location();
			for (LocationInternalRefWithBasicDetailsType loc : locationList) {
				final Locality locality = convert(loc, regionLanguage);
				switch (locality.getLevel()) {
				case SUBMUNICIPALITY:
					if (addressCorrected) {
						for (Entry<String, String> officialNameEntry : locality.getOfficialNames().entrySet()) {
							officialNameEntry.setValue(submunicipality);
						}
					}
					result.setSubMunicipality(locality);
					break;
				case MUNICIPALITY:
					result.setMunicipality(locality);
					break;
				case REGION:
					result.setRegion(locality);
					break;
				case PROVINCE:
					result.setProvince(locality);
				default:
					break;
				}
			}

		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException exception) {
			exception.printStackTrace();
		} catch (Exception e) {
			if (retryAttempts > 0) {
				try {
					Thread.sleep(3000);
					return getLocationByPostalCode(street, postalcode, submunicipality, --retryAttempts);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}

		if (result != null && !addressCorrected) {
			locationsCache.put(postalcodeSubmunicipality, result);
		}

		return result;
	}

	private List<LocationInternalRefWithBasicDetailsType> getLocationTree(String postalcode, String submunicipality) throws URISyntaxException,
			UnsupportedEncodingException {
		URI uri = buildCsmURI(postalcode, submunicipality);
		final Locations locations = restTemplate.getForObject(uri, Locations.class);
		return locations.getLocation();
	}

	private String getRegionLanguage(List<LocationInternalRefWithBasicDetailsType> locationList) {
		for (LocationInternalRefWithBasicDetailsType loc : locationList) {
			if (loc.getType().getCode().equals(LocalityLevel.REGION.name())) {
				return regionLanguageMap.get(loc.getDisplayName().get(0).getContent());
			}
		}
		return null;
	}

	private URI buildCsmURI(String postalcode, String submunicipality) throws URISyntaxException, UnsupportedEncodingException {
		StringBuilder sb = new StringBuilder();
		sb.append(csmServiceRoot).append(LOCATION_RESOURCE_URI).append(UriUtils.encodeFragment(postalcode, "UTF-8")).append("/")
				.append(UriUtils.encodeFragment(submunicipality, "UTF-8"));
		return new URI(sb.toString());
	}

	private URI buildGeolinkURI(String street, String postalcode, String submunicipality) {
		UriBuilder uriBuilder = UriBuilder.fromUri(geoLinkServiceRoot + GEOLINK_SUGGEST_PATH);

		Map<String, String> params = new HashMap<String, String>();
		params.put("street", street);
		params.put("postalcode", postalcode);
		params.put("locality", submunicipality);

		return uriBuilder.buildFromMap(params);
	}

	private static Locality convert(LocationInternalRefWithBasicDetailsType location, String regionLanguage) {
		Locality locality = new Locality();
		locality.setCode(location.getCode());
		locality.setLevel(LocalityLevel.valueOf(location.getType().getCode()));

		Map<String, String> officialNames = new HashMap<String, String>();
		if (!locality.getLevel().equals(LocalityLevel.REGION)) {

			if (location.isLanguageFacility() | regionLanguage == null) {
				for (DisplayNameType displayName : location.getDisplayName()) {
					officialNames.put(displayName.getLanguageIso().name(), displayName.getContent());
				}
			} else {
				// use language of the region for the three official names
				String displayNameToUse = null;
				for (DisplayNameType displayName : location.getDisplayName()) {
					if (displayName.getLanguageIso().name().equalsIgnoreCase(regionLanguage)) {
						displayNameToUse = displayName.getContent();
						break;
					}
				}
				officialNames.put("NL", displayNameToUse);
				officialNames.put("FR", displayNameToUse);
				officialNames.put("EN", displayNameToUse);
			}

		} else { // always translate regions
			for (DisplayNameType displayName : location.getDisplayName()) {
				officialNames.put(displayName.getLanguageIso().name(), displayName.getContent());
			}
		}

		locality.setOfficialNames(officialNames);
		return locality;
	}

	public String getCsmServiceRoot() {
		return csmServiceRoot;
	}

	public void setCsmServiceRoot(String csmServiceRoot) {
		this.csmServiceRoot = csmServiceRoot;
	}

	public void setGeoLinkServiceRoot(String geoLinkServiceRoot) {
		this.geoLinkServiceRoot = geoLinkServiceRoot;
	}

	public RestTemplate getRestTemplate() {
		return restTemplate;
	}

	public void setRestTemplate(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	private static class PostalcodeSubmunicipality {
		private String postalcode;
		private String submunicipality;

		public PostalcodeSubmunicipality(String postalcode, String submunicipality) {
			this.postalcode = postalcode;
			this.submunicipality = submunicipality;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((postalcode == null) ? 0 : postalcode.hashCode());
			result = prime * result + ((submunicipality == null) ? 0 : submunicipality.hashCode());
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
			PostalcodeSubmunicipality other = (PostalcodeSubmunicipality) obj;

			if (postalcode == null) {
				if (other.postalcode != null)
					return false;
			} else if (!postalcode.equals(other.postalcode))
				return false;
			if (submunicipality == null) {
				if (other.submunicipality != null)
					return false;
			} else if (!submunicipality.equals(other.submunicipality))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "PostalcodeSubmunicipality [postalcode=" + postalcode + ", submunicipality=" + submunicipality + "]";
		}
	}

	@Override
	public void clearCache() {
		logger.info("start clearing locality cache");
		locationsCache.clear();
		logger.info("finished clearing locality cache");
	}
}
