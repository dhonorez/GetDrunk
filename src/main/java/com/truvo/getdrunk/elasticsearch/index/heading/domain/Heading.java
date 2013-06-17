package com.truvo.getdrunk.elasticsearch.index.heading.domain;

import java.util.HashMap;
import java.util.Map;

public class Heading {

	private String code;
	private String number;
	private Map<String, String> officialNames = new HashMap<String, String>();

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public Map<String, String> getOfficialNames() {
		return officialNames;
	}

	public void setOfficialNames(Map<String, String> officialNames) {
		this.officialNames = officialNames;
	}

	public void addOfficialName(String languageIso, String name) {
		officialNames.put(languageIso, name);
	}

}
