package com.truvo.getdrunk.elasticsearch.index;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.sql.DataSource;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public class BusinessIndexer {

	// private static final String ADVERTISER_ID_QUERY = "select distinct customerid from unprocessedyellowlisting";
	private static final String ADVERTISER_ID_QUERY = "select id from advertisers";

	private String indexName = "businesses";

	private static Logger logger = LoggerFactory.getLogger(BusinessIndexer.class);

	private JdbcTemplate jdbcTemplate;
	private BusinessIdsFetcher businessIdsFetcher;
	private BusinessDataAggregator businessDataAggregator;

	private ThreadPoolTaskExecutor executor;

	private Client client;

	public BusinessIndexer(DataSource dataSource, BusinessIdsFetcher businessIdsFetcher, BusinessDataAggregator businessDataAggregator,
			ThreadPoolTaskExecutor executor) {
		super();
		this.jdbcTemplate = new JdbcTemplate(dataSource);
		this.businessIdsFetcher = businessIdsFetcher;
		this.businessDataAggregator = businessDataAggregator;
		this.executor = executor;
	}

	public void index() {
		Node node = nodeBuilder().clusterName("InDomoCluster").node();

		client = node.client();

		try {
			System.out.println("start fetching the advertiser ids");
			List<Long> advertiserIds = this.jdbcTemplate.query(ADVERTISER_ID_QUERY, new AdvertiserIdMapper());
			System.out.println("finished fetching the advertiser ids");

			Set<Future<?>> tasks = new HashSet<Future<?>>(advertiserIds.size());

			int counter = 0;
			for (Long advertiserId : advertiserIds) {
				tasks.add(executor.submit(new SaveAdvertiserExecutorTask(advertiserId)));
				++counter;
				if (counter % 500 == 0) {
					System.out.println("started " + counter + " advertiser process jobs");
				}
			}
			System.out.println("started " + counter + " advertiser process jobs");

			// wait until all tasks are finished
			for (Future future : tasks) {
				future.get();
			}
			System.out.println("finished the db import job");
		} catch (InterruptedException exception) {
			exception.printStackTrace();
		} catch (ExecutionException exception) {
			exception.printStackTrace();
		}

	}

	private void saveBusiness(Long advertiserId, Long businessId) {
		try {
			System.out.println("saving business " + businessId + " [advertiser " + advertiserId + "]");
			Business business = businessDataAggregator.aggregateBusinessData(advertiserId, businessId);

			XContentBuilder objectToInsert = jsonBuilder().startObject();
			objectToInsert.field("businessId", advertiserId.toString() + "_" + businessId.toString());

			String bn = business.getBusinessNames().get("DUTCH");
			objectToInsert.field("name", bn);

			List<String> phoneNumbers = business.getPhoneNumbers();
			objectToInsert.field("phoneNumbers", phoneNumbers);

			List<String> headings = business.getHeadings().get("nl");
			objectToInsert.field("headings", headings);

			Map<String, Address> addresses = business.getAddresses();
			Address address = addresses.get("DUTCH");
			if (address != null) {
				Double xcoord = address.getxCoord();
				Double ycoord = address.getyCoord();
				Map<String, String> locMap = new HashMap<String, String>();
				locMap.put("lat", xcoord.toString());
				locMap.put("lon", ycoord.toString());
				objectToInsert.field("location", locMap);
				System.out.println("inserting coordinates");
			}
			objectToInsert.endObject();

			IndexResponse response = client.prepareIndex("businesses", "nl", businessId.toString()).setSource(objectToInsert).execute().actionGet();

		} catch (IOException exception) {
			// TODO Auto-generated catch block
			exception.printStackTrace();
		}
	}

	private class AdvertiserIdMapper implements RowMapper<Long> {
		public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
			String advertiserId = rs.getString("id");
			return Long.parseLong(advertiserId);
		}
	}

	private class SaveAdvertiserExecutorTask implements Runnable {
		private final Long advertiserId;

		public SaveAdvertiserExecutorTask(Long advertiserId) {
			super();
			this.advertiserId = advertiserId;
		}

		@Override
		public void run() {
			System.out.println("start processing advertiser " + advertiserId);
			List<Long> businessIds = businessIdsFetcher.fetchBusinessIdsForAdvertiser(advertiserId);
			for (Long businessId : businessIds) {
				saveBusiness(advertiserId, businessId);
			}
			System.out.println("finished processing advertiser " + advertiserId);
		}
	}

}
