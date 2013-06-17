package com.truvo.getdrunk.elasticsearch.index.bds.domain;

import java.util.List;
import java.util.Map;

public interface BDSContactDetails {

	public Integer getPriority();

	public void setPriority(Integer priority);

	public String getType();

	public void setType(String type);

	public Map<BDSLanguage, String> getLabel();

	public void setLabel(Map<BDSLanguage, String> label);

	public String getValue();

	public void setValue(String value);

	public List<String> getHeadingRestriction();

	public void setHeadingRestriction(List<String> headingRestriction);
}
