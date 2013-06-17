package com.truvo.getdrunk.elasticsearch.index.bds.domain;

import java.util.Map;

public class BDSFolder {

	private long advertiserId;
	private long folderId;
	private Map<BDSLanguage, String> folderNames;
	private boolean mainOffice;

	public BDSFolder(long advertiserId, long folderId, boolean mainOffice, Map<BDSLanguage, String> folderNames) {
		super();
		this.advertiserId = advertiserId;
		this.folderId = folderId;
		this.mainOffice = mainOffice;
		this.folderNames = folderNames;
	}

	public long getFolderId() {
		return folderId;
	}

	public void setFolderId(long folderId) {
		this.folderId = folderId;
	}

	public long getAdvertiserId() {
		return advertiserId;
	}

	public void setAdvertiserId(long advertiserId) {
		this.advertiserId = advertiserId;
	}

	public boolean isMainOffice() {
		return mainOffice;
	}

	public void setMainOffice(boolean mainOffice) {
		this.mainOffice = mainOffice;
	}

	public Map<BDSLanguage, String> getFolderNames() {
		return folderNames;
	}

	public void setFolderNames(Map<BDSLanguage, String> folderNames) {
		this.folderNames = folderNames;
	}

}
