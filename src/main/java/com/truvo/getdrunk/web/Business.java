package com.truvo.getdrunk.web;

public class Business {

	private String name;
	private Coordinate coordinate;
	private String category;
	private String website;
	private String phone;
	private Address address;

	public String getName() {
		return name;
	}

	public Coordinate getCoordinate() {
		return coordinate;
	}

	public String getCategory() {
		return category;
	}

	public String getWebsite() {
		return website;
	}

	public String getPhone() {
		return phone;
	}

	public Address getAddress() {
		return address;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setCoordinate(Coordinate coordinate) {
		this.coordinate = coordinate;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

}
