package com.truvo.getdrunk.elasticsearch.index.bds.domain;

import java.util.Date;

public class BDSAdvertiserStatus {

	private String code;
	private Date date;

	public BDSAdvertiserStatus(String code, Date date) {
		super();
		this.code = code;
		this.date = date;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

}
