package com.truvo.getdrunk.web;

import java.util.List;

public class Query {

	private String category;
	private int maxResults;
	private List<Coordinate> coordinates;
	private boolean openNow;

	public String getCategory() {
		return category;
	}

	public int getMaxResults() {
		return maxResults;
	}

	public List<Coordinate> getCoordinates() {
		return coordinates;
	}
	
	public boolean isOpenNow() {
		return openNow;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public void setMaxResults(int maxResults) {
		this.maxResults = maxResults;
	}

	public void setCoordinates(List<Coordinate> coordinates) {
		this.coordinates = coordinates;
	}
	
	public void setOpenNow(boolean openNow) {
		this.openNow = openNow;
	}

	@Override
	public String toString() {
		return "Query [category=" + category + ", maxResults=" + maxResults
				+ ", coordinates=" + coordinates + ", openNow=" + openNow + "]";
	}

}
