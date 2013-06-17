package com.truvo.getdrunk.elasticsearch.index.management;

import javax.management.MXBean;

import org.springframework.jmx.export.annotation.ManagedOperation;

@MXBean
public interface DataStoreManager {

	@ManagedOperation
	public void refreshMongoAndESIndex();

	@ManagedOperation
	public void importUnprocessedBusinessesIntoMongoAndESIndex();

}
