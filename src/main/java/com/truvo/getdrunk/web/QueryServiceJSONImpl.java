package com.truvo.getdrunk.web;

import static spark.Spark.get;
import static spark.Spark.post;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spark.Request;
import spark.Response;
import spark.Route;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.truvo.getdrunk.elasticsearch.query.BusinessQuery;

public class QueryServiceJSONImpl {

	private static ObjectMapper mapper = new ObjectMapper();
	private static JsonGenerator generator;

	private static Logger logger = LoggerFactory.getLogger(QueryServiceJSONImpl.class);

	public static void main(String[] args) throws Exception {
		get(new Route("/hello") {
			@Override
			public Object handle(Request request, Response response) {
				return "Let's get drunk!";
			}
		});

		post(new Route("/query") {
			@Override
			public Object handle(Request request, Response response) {
				logger.info(request.toString());

				QueryResponse queryResponse = null;

				try {
					Query query = mapper.readValue(request.body(), Query.class);
					logger.info(query.toString());
					queryResponse = BusinessQuery.query(query);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}

				logger.info("Elastic search returned {} result(s).", queryResponse.getBusinesses().size());

				response.type("application/json; charset=UTF-8");
				return asJson(queryResponse);
			}
		});

		post(new Route("/dummy") {
			@Override
			public Object handle(Request request, Response response) {
				logger.info(request.toString());

				Business business = createDummyBusinesses();

				QueryResponse queryResponse = new QueryResponse(Arrays.asList(business));

				response.type("application/json; charset=UTF-8");
				return asJson(queryResponse);
			}
		});

		get(new Route("/example") {
			@Override
			public Object handle(Request request, Response response) {
				logger.info(request.toString());

				Query query = new Query();
				query.setCategory("TETTEN");
				query.setMaxResults(10);
				query.setCoordinates(Arrays.asList(new Coordinate(1.06f, 13.0f)));

				return asJson(query);
			}
		});
	}

	private static Object asJson(Object json) {
		OutputStream outputStream = null;
		try {
			outputStream = new ByteArrayOutputStream();
			generator = new JsonFactory().createGenerator(outputStream, JsonEncoding.UTF8);
			mapper.writeValue(generator, json);
			return outputStream.toString();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return e.getMessage();
		} finally {
			IOUtils.closeQuietly(outputStream);
		}
	}
	
	private static Business createDummyBusinesses() {
		Business business = new Business();
		business.setHeadings(Arrays.asList("Cafés"));
		business.setName("Kelly's Irish Pub BVBA");
		business.setPhone("03 201 59 88");
		business.setWebsite("http://www.kellys.be");
		business.setAddress(new Address("De Keyserlei", "27", "2018", "Antwerpen"));
		business.setCoordinate(new Coordinate(4.418218577f, 51.217544792f));
		return business;
	}

}
