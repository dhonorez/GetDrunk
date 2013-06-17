package com.truvo.getdrunk.elasticsearch.index.bds.domain;

import java.util.ArrayList;
import java.util.List;

public class BDSPublicationPreferences {

	private boolean completeListingSuppression = false;
	private boolean ratingSuppression = false;
	private boolean financialInfoSuppression = false;
	private boolean businessFreeListingSuppression = false;
	private List<String> freeListingSuppressedHeadings = new ArrayList<String>();
	private List<String> guaranteedFreeListingHeadings = new ArrayList<String>();
	private boolean truvoProgram = false;

	public boolean isCompleteListingSuppression() {
		return completeListingSuppression;
	}

	public void setCompleteListingSuppression(boolean completeListingSuppression) {
		this.completeListingSuppression = completeListingSuppression;
	}

	public boolean isRatingSuppression() {
		return ratingSuppression;
	}

	public void setRatingSuppression(boolean ratingSuppression) {
		this.ratingSuppression = ratingSuppression;
	}

	public boolean isFinancialInfoSuppression() {
		return financialInfoSuppression;
	}

	public void setFinancialInfoSuppression(boolean financialInfoSuppression) {
		this.financialInfoSuppression = financialInfoSuppression;
	}

	public boolean isBusinessFreeListingSuppression() {
		return businessFreeListingSuppression;
	}

	public List<String> getFreeListingSuppressedHeadings() {
		return freeListingSuppressedHeadings;
	}

	public void setFreeListingSuppressedHeadings(List<String> freeListingSuppressedHeadings) {
		this.freeListingSuppressedHeadings = freeListingSuppressedHeadings;
	}

	public void setBusinessFreeListingSuppression(boolean businessFreeListingSuppression) {
		this.businessFreeListingSuppression = businessFreeListingSuppression;
	}

	public List<String> getGuaranteedFreeListingHeadings() {
		return guaranteedFreeListingHeadings;
	}

	public void setGuaranteedFreeListingHeadings(List<String> guaranteedFreeListingHeadings) {
		this.guaranteedFreeListingHeadings = guaranteedFreeListingHeadings;
	}

	public boolean isTruvoProgram() {
		return truvoProgram;
	}

	public void setTruvoProgram(boolean truvoProgram) {
		this.truvoProgram = truvoProgram;
	}

}
