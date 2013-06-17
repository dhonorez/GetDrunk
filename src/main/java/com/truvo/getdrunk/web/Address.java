package com.truvo.getdrunk.web;

public class Address {

	private String street;
	private String number;
	private String zipcode;
	private String city;

	public Address(String street, String number, String zipcode, String city) {
		super();
		this.street = street;
		this.number = number;
		this.zipcode = zipcode;
		this.city = city;
	}

	public String getStreet() {
		return street;
	}

	public String getNumber() {
		return number;
	}

	public String getZipcode() {
		return zipcode;
	}

	public String getCity() {
		return city;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}

	public void setCity(String city) {
		this.city = city;
	}

}
