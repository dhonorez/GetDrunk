package com.truvo.getdrunk.elasticsearch.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;

import com.truvo.getdrunk.web.Business;
import com.truvo.getdrunk.web.Query;
import com.truvo.getdrunk.web.QueryResponse;

public class BusinessQuery {

	public static QueryResponse query(Query query) {

		// Node node = nodeBuilder().clusterName("InDomoCluster").node();
		// Client client = node.client();

		// final SearchRequestBuilder searchRequestBuilder = client.prepareSearch("businesses");
		Settings settings = ImmutableSettings.settingsBuilder().put("cluster.name", "InDomoCluster").build();
		Client client = new TransportClient(settings).addTransportAddress(new InetSocketTransportAddress("http://192.168.0.121", 9300));

		int maxSize = 20;
		if (query != null) {
			maxSize = query.getMaxResults();
		}
		SearchResponse response = client.prepareSearch("businesses").setTypes("nl").setFrom(0).setSize(maxSize).execute().actionGet();

		QueryResponse queryResponse = convertResponse(response);
		client.close();
		return queryResponse;

	}

	private static QueryResponse convertResponse(SearchResponse response) {
		List<Business> businesses = new ArrayList<Business>();
		for (SearchHit hit : response.getHits()) {
			Business business = new Business();
			Map<String, SearchHitField> fields = hit.getFields();

			business.setName(fields.get("name").getValue().toString());

			// business.setCategory(category)

			businesses.add(business);
		}
		return new QueryResponse(businesses);
	}
}
