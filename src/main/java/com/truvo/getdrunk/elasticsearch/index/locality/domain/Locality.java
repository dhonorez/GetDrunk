package com.truvo.getdrunk.elasticsearch.index.locality.domain;

import java.util.HashMap;
import java.util.Map;

public class Locality {

	private String code;
	private Map<String, String> officialNames = new HashMap<String, String>();
	private LocalityLevel level;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Map<String, String> getOfficialNames() {
		return officialNames;
	}

	public void setOfficialNames(Map<String, String> officialNames) {
		this.officialNames = officialNames;
	}

	public LocalityLevel getLevel() {
		return level;
	}

	public void setLevel(LocalityLevel level) {
		this.level = level;
	}

}
