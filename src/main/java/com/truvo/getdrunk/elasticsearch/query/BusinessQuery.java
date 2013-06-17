package com.truvo.getdrunk.elasticsearch.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.GeoPolygonFilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import com.truvo.getdrunk.web.Business;
import com.truvo.getdrunk.web.Coordinate;
import com.truvo.getdrunk.web.Query;
import com.truvo.getdrunk.web.QueryResponse;

public class BusinessQuery {

	public static QueryResponse query(Query query) {

		// Node node = nodeBuilder().clusterName("InDomoCluster").client(true).node();
		// Client client = node.client();

		Settings settings = ImmutableSettings.settingsBuilder().put("cluster.name", "InDomoCluster").build();
		Client client = new TransportClient(settings).addTransportAddress(new InetSocketTransportAddress("192.168.0.121", 9300));

		// Builder settings = ImmutableSettings.settingsBuilder();
		// settings.put("client.transport.sniff", true);
		// settings.put("cluster.name", "InDomoCluster");
		// settings.build();
		//
		// TransportClient c = new TransportClient(settings);
		// c.addTransportAddress(new InetSocketTransportAddress("127.0.0.1", 9300));
		// c.addTransportAddress(new InetSocketTransportAddress("192.168.0.121", 9300));
		// Client client = c;

		if (query != null && query.getCoordinates() != null) {
			// FilterBuilder filter = FilterBuilders.geoPolygonFilter("location");

			GeoPolygonFilterBuilder geoPolygonFilter = FilterBuilders.geoPolygonFilter("location");

			for (Coordinate coord : query.getCoordinates()) {
				geoPolygonFilter.addPoint(coord.getY(), coord.getX());
			}

			QueryBuilder esQuery = QueryBuilders.constantScoreQuery(geoPolygonFilter);

			SearchSourceBuilder source = new SearchSourceBuilder().query(esQuery);

			SearchRequest request = Requests.searchRequest("businesses").source(source);

			int maxSize = 20;
			if (query != null) {
				maxSize = query.getMaxResults();
			}
			SearchRequestBuilder searchRequestBuilder = client.prepareSearch("businesses");
			searchRequestBuilder.setQuery(esQuery);

			SearchResponse response = searchRequestBuilder.setSize(maxSize).setExplain(false).execute().actionGet();

			QueryResponse queryResponse = convertResponse(response);
			return queryResponse;
		} else {
			int maxSize = 20;
			if (query != null) {
				maxSize = query.getMaxResults();
			}
			// SearchResponse response = client.prepareSearch("businesses").setTypes("nl").setFrom(0).setSize(maxSize).execute().actionGet();
			SearchResponse response = client.prepareSearch().execute().actionGet();

			QueryResponse queryResponse = convertResponse(response);
			return queryResponse;
		}

	}

	private static QueryResponse convertResponse(SearchResponse response) {
		List<Business> businesses = new ArrayList<Business>();
		for (SearchHit hit : response.getHits()) {
			Business business = new Business();
			Map<String, Object> fields = hit.getSource();

			business.setName((String) fields.get("name"));

			List<String> headings = (List<String>) fields.get("headings");
			business.setHeadings(headings);

			Map<String, String> bCoord = (Map<String, String>) fields.get("location");

			Coordinate coordinate = new Coordinate(Double.parseDouble(bCoord.get("lat")), Double.parseDouble(bCoord.get("lon")));
			business.setCoordinate(coordinate);

			List<String> phoneNumbers = (List<String>) fields.get("phoneNumbers");
			if (phoneNumbers != null && phoneNumbers.size() > 0) {
				business.setPhone(phoneNumbers.get(0));
			}

			businesses.add(business);
		}
		return new QueryResponse(businesses);
	}

	public static void main(String[] args) {
		QueryResponse response = BusinessQuery.query(null);
		System.out.println(response);
	}

}
