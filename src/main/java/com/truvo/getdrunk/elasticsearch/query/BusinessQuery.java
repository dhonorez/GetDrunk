package com.truvo.getdrunk.elasticsearch.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.GeoPolygonFilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermFilterBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.facet.FacetBuilders;
import org.elasticsearch.search.facet.terms.TermsFacetBuilder;
import org.elasticsearch.search.sort.SortOrder;

import com.truvo.getdrunk.web.Address;
import com.truvo.getdrunk.web.Business;
import com.truvo.getdrunk.web.Coordinate;
import com.truvo.getdrunk.web.Query;
import com.truvo.getdrunk.web.QueryResponse;

public class BusinessQuery {

	public static QueryResponse query(Query query) {
		// Local stuff
		// Node node = nodeBuilder().clusterName("InDomoCluster").client(true).node();
		// Client client = node.client();

		Settings settings = ImmutableSettings.settingsBuilder().put("cluster.name", "InDomoCluster").build();
		Client client = new TransportClient(settings).addTransportAddress(new InetSocketTransportAddress("192.168.0.121", 9300));

		GeoPolygonFilterBuilder geoPolygonFilter = null;
		if (query != null && query.getCoordinates() != null) {

			geoPolygonFilter = FilterBuilders.geoPolygonFilter("location");

			for (Coordinate coord : query.getCoordinates()) {
				geoPolygonFilter.addPoint(coord.getLon(), coord.getLat());
			}
		}

		TermFilterBuilder openQuery = null;
		if (query.isOpenNow()) {
			openQuery = new TermFilterBuilder("open", "true");
		}

		TermFilterBuilder categoryQuery = null;
		if (!StringUtils.isBlank(query.getCategory())) {
			categoryQuery = new TermFilterBuilder("headings", query.getCategory());
		}

		QueryBuilder esQuery = null;

		if (geoPolygonFilter != null) {
			if (openQuery != null) {
				if (categoryQuery != null) {
					esQuery = QueryBuilders.boolQuery().must(QueryBuilders.constantScoreQuery(geoPolygonFilter))
							.must(QueryBuilders.constantScoreQuery(openQuery)).must(QueryBuilders.constantScoreQuery(categoryQuery));
				} else {
					esQuery = QueryBuilders.boolQuery().must(QueryBuilders.constantScoreQuery(geoPolygonFilter))
							.must(QueryBuilders.constantScoreQuery(openQuery));
				}
			} else {
				if (categoryQuery != null) {
					esQuery = QueryBuilders.boolQuery().must(QueryBuilders.constantScoreQuery(geoPolygonFilter))
							.must(QueryBuilders.constantScoreQuery(categoryQuery));
				} else {
					esQuery = QueryBuilders.constantScoreQuery(geoPolygonFilter);
				}
			}
		} else {
			if (openQuery != null) {
				if (categoryQuery != null) {
					esQuery = QueryBuilders.boolQuery().must(QueryBuilders.constantScoreQuery(openQuery)).must(QueryBuilders.constantScoreQuery(categoryQuery));
				} else {
					esQuery = QueryBuilders.boolQuery().must(QueryBuilders.constantScoreQuery(openQuery));
				}
			} else {
				if (categoryQuery != null) {
					esQuery = QueryBuilders.boolQuery().must(QueryBuilders.constantScoreQuery(categoryQuery));
				} else {
					esQuery = QueryBuilders.matchAllQuery();
				}
			}
		}

		int maxSize = 20;
		if (query != null) {
			maxSize = query.getMaxResults();
		}

		TermsFacetBuilder facet = FacetBuilders.termsFacet("facet").field("headings").size(100);
		SearchRequestBuilder searchRequestBuilder = client.prepareSearch("businesses").addFacet(facet);

		searchRequestBuilder.addSort("name", SortOrder.ASC);

		searchRequestBuilder.setQuery(esQuery);

		SearchResponse response = searchRequestBuilder.setSize(maxSize).setExplain(false).execute().actionGet();

		QueryResponse queryResponse = convertResponse(response);
		return queryResponse;
		// SearchResponse response = client.prepareSearch("businesses").setTypes("nl").setFrom(0).setSize(maxSize).execute().actionGet();
		// SearchResponse response = client.prepareSearch().execute().actionGet();

	}

	private static QueryResponse convertResponse(SearchResponse response) {
		List<Business> businesses = new ArrayList<Business>();
		for (SearchHit hit : response.getHits()) {
			Business business = new Business();
			Map<String, Object> fields = hit.getSource();

			business.setName((String) fields.get("name"));

			Address address = new Address((String) fields.get("street"), (String) fields.get("number"), (String) fields.get("zipcode"),
					(String) fields.get("city"));
			business.setAddress(address);

			List<String> headings = (List<String>) fields.get("headings");
			business.setHeadings(headings);

			Map<String, String> bCoord = (Map<String, String>) fields.get("location");

			Coordinate coordinate = new Coordinate(Double.parseDouble(bCoord.get("lon")), Double.parseDouble(bCoord.get("lat")));
			business.setCoordinate(coordinate);

			List<String> phoneNumbers = (List<String>) fields.get("phoneNumbers");
			if (phoneNumbers != null && phoneNumbers.size() > 0) {
				business.setPhone(phoneNumbers.get(0));
			}

			List<String> websites = (List<String>) fields.get("websites");
			if (websites != null && websites.size() > 0) {
				business.setWebsite(websites.get(0));
			}

			List<String> emails = (List<String>) fields.get("emails");
			if (emails != null && emails.size() > 0) {
				business.setEmail(emails.get(0));
			}

			business.setAverageRating((String) fields.get("averageRating").toString());
			business.setNumberOfReviews((String) fields.get("numberOfReviews").toString());
			business.setOpen((String) fields.get("open").toString());
			business.setPaid((String) fields.get("paid").toString());

			businesses.add(business);
		}
		return new QueryResponse(businesses);
	}

	public static void main(String[] args) {
		Query q = new Query();
		// q.setCategory("restaurants");
		// q.setCategory("artsen");
		// q.setCategory("handelaars");
		// q.setCategory("immobiliën");
		// q.setCategory("architecten");
		// q.setCategory("kleding");
		// q.setCategory("cafés");
		// q.setCategory("juweliers");
		// q.setCategory("bars");
		// q.setCategory("evenementen");
		// q.setCategory("banken");
		// q.setCategory("garages");
		// q.setCategory("advocaten");
		q.setCategory("");
		q.setMaxResults(20);
		q.setOpenNow(false);

		List<Coordinate> coordinates = new ArrayList<Coordinate>();
		Coordinate c1 = new Coordinate(51.256758449834216, 4.06219482421875);
		coordinates.add(c1);
		Coordinate c2 = new Coordinate(51.163844261348274, 4.167938232421875);
		coordinates.add(c2);
		Coordinate c3 = new Coordinate(51.17202478938592, 4.4501495361328125);
		coordinates.add(c3);
		Coordinate c4 = new Coordinate(51.28768819403519, 4.38629150390625);
		coordinates.add(c4);
		// q.setCoordinates(coordinates);

		QueryResponse response = BusinessQuery.query(q);
		System.out.println(response);
	}
}
