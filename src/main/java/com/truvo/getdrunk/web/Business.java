package com.truvo.getdrunk.web;

import java.util.List;

public class Business {

	private String name;
	private Coordinate coordinate;
	private List<String> headings;
	private String website;
	private String email;
	private String phone;
	private Address address;
	private String averageRating;
	private String numberOfReviews;
	private String paid;
	private String open;

	public String getName() {
		return name;
	}

	public Coordinate getCoordinate() {
		return coordinate;
	}

	public List<String> getHeadings() {
		return headings;
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

	public void setHeadings(List<String> headings) {
		this.headings = headings;
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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getAverageRating() {
		return averageRating;
	}

	public void setAverageRating(String averageRating) {
		this.averageRating = averageRating;
	}

	public String getNumberOfReviews() {
		return numberOfReviews;
	}

	public void setNumberOfReviews(String numberOfReviews) {
		this.numberOfReviews = numberOfReviews;
	}

	public String getPaid() {
		return paid;
	}

	public void setPaid(String paid) {
		this.paid = paid;
	}

	public String getOpen() {
		return open;
	}

	public void setOpen(String open) {
		this.open = open;
	}

	@Override
	public String toString() {
		return "Business [name=" + name + ", coordinate=" + coordinate + ", headings=" + headings + ", website=" + website + ", phone=" + phone + ", address="
				+ address + "]";
	}

}
