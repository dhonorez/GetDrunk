package com.truvo.getdrunk.elasticsearch.index.bds.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BDSEmail implements Comparable<BDSEmail>, BDSContactDetails {

	private Integer priority;

	private String type;
	private Map<BDSLanguage, String> label;
	private String value;

	private List<String> headingRestriction = new ArrayList<String>();

	public BDSEmail(Integer priority, String type, Map<BDSLanguage, String> label, String value) {
		super();
		this.priority = priority;
		this.type = type;
		this.label = label;
		this.value = value;
	}

	@Override
	public int compareTo(BDSEmail other) {
		if (priority != null) {
			int compareToByPriority = (priority).compareTo(other.getPriority());
			if (compareToByPriority == 0) {
				return value.compareTo(other.getValue());
			}
			return compareToByPriority;
		} else {
			return value.compareTo(other.getValue());
		}
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Map<BDSLanguage, String> getLabel() {
		return label;
	}

	public void setLabel(Map<BDSLanguage, String> label) {
		this.label = label;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public List<String> getHeadingRestriction() {
		return headingRestriction;
	}

	public void setHeadingRestriction(List<String> headingRestriction) {
		this.headingRestriction = headingRestriction;
	}
}
