package com.truvo.getdrunk.elasticsearch.index;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;

public class BusinessIndexer {

	public void index() {
		Node node = nodeBuilder().clusterName("GetDrunk").node();

		Client client = node.client();

		BulkRequestBuilder bulkRequest = client.prepareBulk();

	}

}
