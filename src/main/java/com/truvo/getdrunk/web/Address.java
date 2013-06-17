package com.truvo.getdrunk.web;

public class Address {

	private String street;
	private int number;
	private int zipcode;
	private String city;

	public Address(String street, int number, int zipcode, String city) {
		super();
		this.street = street;
		this.number = number;
		this.zipcode = zipcode;
		this.city = city;
	}

	public String getStreet() {
		return street;
	}

	public int getNumber() {
		return number;
	}

	public int getZipcode() {
		return zipcode;
	}

	public String getCity() {
		return city;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public void setZipcode(int zipcode) {
		this.zipcode = zipcode;
	}

	public void setCity(String city) {
		this.city = city;
	}

}
