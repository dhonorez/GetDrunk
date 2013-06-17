package com.truvo.getdrunk.web;

import java.util.List;

public class QueryResponse {

	private List<Business> businesses;

	public QueryResponse(List<Business> businesses) {
		super();
		this.businesses = businesses;
	}

	public List<Business> getBusinesses() {
		return businesses;
	}

	public void setBusinesses(List<Business> businesses) {
		this.businesses = businesses;
	}

}
