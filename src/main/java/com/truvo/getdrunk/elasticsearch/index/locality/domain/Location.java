package com.truvo.getdrunk.elasticsearch.index.locality.domain;

public class Location {

	private Locality subMunicipality;
	private Locality municipality;
	private Locality province;
	private Locality region;

	public Locality getSubMunicipality() {
		return subMunicipality;
	}

	public void setSubMunicipality(Locality subMunicipality) {
		this.subMunicipality = subMunicipality;
	}

	public Locality getMunicipality() {
		return municipality;
	}

	public void setMunicipality(Locality municipality) {
		this.municipality = municipality;
	}

	public Locality getProvince() {
		return province;
	}

	public void setProvince(Locality province) {
		this.province = province;
	}

	public Locality getRegion() {
		return region;
	}

	public void setRegion(Locality region) {
		this.region = region;
	}

}
