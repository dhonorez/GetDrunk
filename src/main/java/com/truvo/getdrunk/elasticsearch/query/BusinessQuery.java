package com.truvo.getdrunk.elasticsearch.query;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;

import com.truvo.getdrunk.web.Query;

public class BusinessQuery {

	public void query(Query query) {

		Node node = nodeBuilder().clusterName("InDomoCluster").node();

		Client client = node.client();

		// final SearchRequestBuilder searchRequestBuilder = client.prepareSearch("businesses");
		SearchResponse response = client.prepareSearch("businesses").setTypes("nl").setFrom(0).setSize(query.getMaxResults()).execute().actionGet();

	}

}
