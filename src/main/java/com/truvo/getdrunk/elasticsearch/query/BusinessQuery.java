package com.truvo.getdrunk.elasticsearch.query;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;

import com.truvo.getdrunk.web.Business;
import com.truvo.getdrunk.web.Query;
import com.truvo.getdrunk.web.QueryResponse;

public class BusinessQuery {

	public static QueryResponse query(Query query) {

		Node node = nodeBuilder().clusterName("InDomoCluster").node();

		Client client = node.client();

		// final SearchRequestBuilder searchRequestBuilder = client.prepareSearch("businesses");
		SearchResponse response = client.prepareSearch("businesses").setTypes("nl").setFrom(0).setSize(query.getMaxResults()).execute().actionGet();

		return convertResponse(response);

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
