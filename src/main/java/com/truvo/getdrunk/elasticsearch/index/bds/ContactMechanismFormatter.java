package com.truvo.getdrunk.elasticsearch.index.bds;

import org.apache.commons.lang.StringUtils;

public final class ContactMechanismFormatter {

	private ContactMechanismFormatter() {
	}

	public static String formatPhone(String country, String area, String number) {
		try {
			if (country != null) {
				if (country.equals("32") || country.equals("0032") || country.equals("032") || country.equals("+32")) {
					if (area.length() == 2 && number.length() == 7) {
						return area + " " + number.substring(0, 3) + " " + number.substring(3, 5) + " " + number.substring(5, 7);
					} else if (area.length() == 3 && number.length() == 6) {
						return area + " " + number.substring(0, 2) + " " + number.substring(2, 4) + " " + number.substring(4, 6);
					} else if (area.length() == 4 && number.length() == 6) {
						return area + " " + number.substring(0, 2) + " " + number.substring(2, 4) + " " + number.substring(4, 6);
					} else {
						return area + " " + number;
					}
				} else {
					// +32476 74 82 81
					if (StringUtils.isBlank(area)) {
						return country + " " + number;
					} else {
						return country + "" + area + " " + number;
					}
				}
			} else {
				return createDefaultNumber(null, area, number);
			}
		} catch (Exception e) {
			return createDefaultNumber(country, area, number);
		}
	}

	private static String createDefaultNumber(String country, String area, String number) {
		String countryValue = "";
		if (!StringUtils.isBlank(country)) {
			countryValue = country;
		}

		String areaValue = "";
		if (!StringUtils.isBlank(area)) {
			areaValue = area;
		}

		String numberValue = "";
		if (!StringUtils.isBlank(number)) {
			numberValue = number;
		}
		return countryValue + areaValue + numberValue;
	}
}
