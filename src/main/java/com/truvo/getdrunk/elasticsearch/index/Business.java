package com.truvo.getdrunk.elasticsearch.index;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Business {

	private String id;

	private Long advertiserId;
	private Long businessId;

	private Map<String, String> businessNames = new HashMap<String, String>();
	private Map<String, List<String>> headings = new HashMap<String, List<String>>();

	private Map<String, Address> addresses = new HashMap<String, Address>();

	private List<String> phoneNumbers = new ArrayList<String>();
	private List<String> mobileNumbers = new ArrayList<String>();
	private List<String> faxNumbers = new ArrayList<String>();
	private List<String> emails = new ArrayList<String>();
	private List<String> websites = new ArrayList<String>();

	private Integer averageRating;
	private Integer numberOfReviews;
	private boolean paid;
	private boolean open;

	private Date updateDate;

	public Business(String id) {
		super();
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Long getAdvertiserId() {
		return advertiserId;
	}

	public void setAdvertiserId(Long advertiserId) {
		this.advertiserId = advertiserId;
	}

	public Long getBusinessId() {
		return businessId;
	}

	public void setBusinessId(Long businessId) {
		this.businessId = businessId;
	}

	public Map<String, String> getBusinessNames() {
		return businessNames;
	}

	public void setBusinessNames(Map<String, String> businessNames) {
		this.businessNames = businessNames;
	}

	public Map<String, List<String>> getHeadings() {
		return headings;
	}

	public void setHeadings(Map<String, List<String>> headings) {
		this.headings = headings;
	}

	public Map<String, Address> getAddresses() {
		return addresses;
	}

	public void setAddresses(Map<String, Address> addresses) {
		this.addresses = addresses;
	}

	public List<String> getPhoneNumbers() {
		return phoneNumbers;
	}

	public void setPhoneNumbers(List<String> phoneNumbers) {
		this.phoneNumbers = phoneNumbers;
	}

	public List<String> getMobileNumbers() {
		return mobileNumbers;
	}

	public void setMobileNumbers(List<String> mobileNumbers) {
		this.mobileNumbers = mobileNumbers;
	}

	public List<String> getFaxNumbers() {
		return faxNumbers;
	}

	public void setFaxNumbers(List<String> faxNumbers) {
		this.faxNumbers = faxNumbers;
	}

	public List<String> getEmails() {
		return emails;
	}

	public void setEmails(List<String> emails) {
		this.emails = emails;
	}

	public List<String> getWebsites() {
		return websites;
	}

	public void setWebsites(List<String> websites) {
		this.websites = websites;
	}

	public Integer getAverageRating() {
		int nextInt = new Random().nextInt(5);
		return nextInt + 1;
	}

	public void setAverageRating(Integer averageRating) {
		this.averageRating = averageRating;
	}

	public Integer getNumberOfReviews() {
		int nextInt = new Random().nextInt(15);
		return nextInt + 1;
	}

	public boolean isPaid() {
		int nextInt = new Random().nextInt(10);
		if (nextInt > 3) {
			return false;
		}
		return true;
	}

	public void setPaid(boolean paid) {
		this.paid = paid;
	}

	public boolean isOpen() {
		int nextInt = new Random().nextInt(10);
		if (nextInt > 7) {
			return false;
		}
		return true;
	}

	public void setOpen(boolean open) {
		this.open = open;
	}

	public void setNumberOfReviews(Integer numberOfReviews) {
		this.numberOfReviews = numberOfReviews;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

}
