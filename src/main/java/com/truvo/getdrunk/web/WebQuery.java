package com.truvo.getdrunk.web;

import static spark.Spark.get;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spark.Request;
import spark.Response;
import spark.Route;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WebQuery {

	private static Logger logger = LoggerFactory.getLogger(WebQuery.class);

	public static void main(String[] args) throws Exception {

		get(new Route("/hello") {
			@Override
			public Object handle(Request request, Response response) {
				return "Let's get drunk!";
			}
		});

		get(new Route("/query") {
			@Override
			public Object handle(Request request, Response response) {
				logger.info(request.toString());
				
				ObjectMapper m = new ObjectMapper();
				try {
					Query query = m.readValue(request.body(), Query.class);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				return request.body();
			}
		});

		get(new Route("/example") {
			@Override
			public Object handle(Request request, Response response) {
				logger.info(request.toString());
				
				ObjectMapper m = new ObjectMapper();
				Query query = new Query();
				query.setCategory("TETTEN");
				query.setMaxResults(10);
				query.setCoordinates(Arrays.asList(new Coordinate(1.06f, 13.0f)));
				
				try {
					return m.writeValueAsString(query);
				} catch (JsonProcessingException e) {
					e.printStackTrace();
					return null;
				}
			}
		});
		
	}

}
