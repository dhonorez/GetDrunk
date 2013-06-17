package com.truvo.getdrunk.elasticsearch.index.locality;

import com.truvo.getdrunk.elasticsearch.index.locality.domain.Location;

public interface LocalityService {

	Location getLocationByPostalCode(String street, String postalcode, String submunicipality, int retryAttempts);

	void clearCache();
}
