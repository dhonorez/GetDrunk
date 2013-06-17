package com.truvo.getdrunk.elasticsearch.index;

import java.util.ArrayList;
import java.util.List;

import com.truvo.getdrunk.elasticsearch.index.bds.BDSServiceRestImpl;
import com.truvo.getdrunk.elasticsearch.index.bds.domain.BDSBusinessData;

public class BusinessIdsFetcher {

	private BDSServiceRestImpl bdsService;

	public BusinessIdsFetcher(BDSServiceRestImpl bdsService) {
		super();
		this.bdsService = bdsService;
	}

	public List<Long> fetchBusinessIdsForAdvertiser(long advertiserId) {
		List<BDSBusinessData> businesses = bdsService.retrieveBusinessesForAdvertiser(advertiserId);

		List<Long> businessIds = new ArrayList<Long>();
		for (BDSBusinessData business : businesses) {
			businessIds.add(business.getBusinessId());
		}

		return businessIds;
	}
}
