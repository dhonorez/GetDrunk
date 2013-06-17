package com.truvo.getdrunk.elasticsearch.index.bds.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BDSBusinessData {

	private Long advertiserId;
	private Long businessId;

	private Map<BDSLanguage, String> businessName = new HashMap<BDSLanguage, String>();
	private Map<BDSLanguage, List<String>> alternativeNames = new HashMap<BDSLanguage, List<String>>();

	private Map<BDSLanguage, BDSAddress> addresses = new HashMap<BDSLanguage, BDSAddress>();

	private List<BDSPhone> phoneList = new ArrayList<BDSPhone>();
	private List<BDSMobile> mobileList = new ArrayList<BDSMobile>();
	private List<BDSFax> faxList = new ArrayList<BDSFax>();
	private List<BDSEmail> emailList = new ArrayList<BDSEmail>();
	private List<BDSHomepage> homepageList = new ArrayList<BDSHomepage>();

	private String vat;
	private BDSAdvertiserStatus advertiserStatus;

	private List<BDSHeading> headings = new ArrayList<BDSHeading>();
	private List<BDSFolder> folders = new ArrayList<BDSFolder>();

	private BDSPublicationPreferences publicationPreferences;

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

	public Map<BDSLanguage, String> getBusinessName() {
		return businessName;
	}

	public void setBusinessName(Map<BDSLanguage, String> businessName) {
		this.businessName = businessName;
	}

	public Map<BDSLanguage, List<String>> getAlternativeNames() {
		return alternativeNames;
	}

	public void setAlternativeNames(Map<BDSLanguage, List<String>> alternativeNames) {
		this.alternativeNames = alternativeNames;
	}

	public List<BDSHeading> getHeadings() {
		return headings;
	}

	public void setHeadings(List<BDSHeading> headings) {
		this.headings = headings;
	}

	public List<BDSPhone> getPhoneList() {
		return phoneList;
	}

	public void setPhoneList(List<BDSPhone> phoneList) {
		this.phoneList = phoneList;
	}

	public List<BDSMobile> getMobileList() {
		return mobileList;
	}

	public void setMobileList(List<BDSMobile> mobileList) {
		this.mobileList = mobileList;
	}

	public List<BDSFax> getFaxList() {
		return faxList;
	}

	public void setFaxList(List<BDSFax> faxList) {
		this.faxList = faxList;
	}

	public List<BDSEmail> getEmailList() {
		return emailList;
	}

	public void setEmailList(List<BDSEmail> emailList) {
		this.emailList = emailList;
	}

	public List<BDSHomepage> getHomepageList() {
		return homepageList;
	}

	public void setHomepageList(List<BDSHomepage> homepageList) {
		this.homepageList = homepageList;
	}

	public Map<BDSLanguage, BDSAddress> getAddresses() {
		return addresses;
	}

	public void setAddresses(Map<BDSLanguage, BDSAddress> addresses) {
		this.addresses = addresses;
	}

	public String getVat() {
		return vat;
	}

	public void setVat(String vat) {
		this.vat = vat;
	}

	public BDSPublicationPreferences getPublicationPreferences() {
		return publicationPreferences;
	}

	public void setPublicationPreferences(BDSPublicationPreferences publicationPreferences) {
		this.publicationPreferences = publicationPreferences;
	}

	public List<BDSFolder> getFolders() {
		return folders;
	}

	public void setFolders(List<BDSFolder> folders) {
		this.folders = folders;
	}

	public BDSAdvertiserStatus getAdvertiserStatus() {
		return advertiserStatus;
	}

	public void setAdvertiserStatus(BDSAdvertiserStatus advertiserStatus) {
		this.advertiserStatus = advertiserStatus;
	}

}
