package com.truvo.getdrunk.elasticsearch.index.bds.domain;

public class BDSHeading implements Comparable<BDSHeading> {

	private Integer priority;
	private String headingCode;

	public BDSHeading(Integer priority, String headingCode) {
		super();
		this.headingCode = headingCode;
		this.priority = priority;
	}

	@Override
	public int compareTo(BDSHeading other) {
		int compareToByPriority = (priority).compareTo(other.getPriority());
		if (compareToByPriority == 0) {
			return headingCode.compareTo(other.getHeadingCode());
		}
		return compareToByPriority;
	}

	public String getHeadingCode() {
		return headingCode;
	}

	public void setHeadingCode(String headingCode) {
		this.headingCode = headingCode;
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

}
