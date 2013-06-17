package com.truvo.getdrunk.elasticsearch.index.management;

import java.io.File;
import java.io.FileInputStream;
import java.util.Scanner;

import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.indices.IndexAlreadyExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.truvo.getdrunk.elasticsearch.index.BusinessIndexer;

@ManagedResource
public class DataStoreManagerImpl implements DataStoreManager {

	private static final Logger logger = LoggerFactory.getLogger(DataStoreManagerImpl.class);

	private static final String ES_CLUSTER_NAME = "InDomoCluster";
	private static final String ES_INDEX_NAME = "businesses";
	private static final String ES_HOST = "127.0.0.1";

	private BusinessIndexer bulkImporter;
	private String host = "127.0.0.1";

	public DataStoreManagerImpl(BusinessIndexer bulkImporter, String host) {
		super();
		this.bulkImporter = bulkImporter;
		this.host = host;
	}

	@Override
	@ManagedOperation
	public void refreshMongoAndESIndex() {
		Settings settings = ImmutableSettings.settingsBuilder().put("cluster.name", ES_CLUSTER_NAME).build();
		Client client = new TransportClient(settings).addTransportAddress(new InetSocketTransportAddress(ES_HOST, 9300));

		createIndex(client);

		// create and configure the different types
		PutMappingResponse actionGetDutch = client.admin().indices().preparePutMapping(ES_INDEX_NAME).setType("nl").setSource(readConfig("dutch")).execute()
				.actionGet();

		boolean acknowledgedDutch = actionGetDutch.isAcknowledged();
		logger.info("received " + acknowledgedDutch + " for business index creation");

		PutMappingResponse actionGetFrench = client.admin().indices().preparePutMapping(ES_INDEX_NAME).setType("fr").setSource(readConfig("french")).execute()
				.actionGet();

		boolean acknowledgedFrench = actionGetFrench.isAcknowledged();
		logger.info("received " + acknowledgedFrench + " for business index creation");

		PutMappingResponse actionGetEnglish = client.admin().indices().preparePutMapping(ES_INDEX_NAME).setType("en").setSource(readConfig("english"))
				.execute().actionGet();

		boolean acknowledgedEnglish = actionGetEnglish.isAcknowledged();
		logger.info("received " + acknowledgedEnglish + " for business index creation");

		// initialize the rivers
		// addRivers();
		addTruvoMongoDBRiver();
		client.close();

	}

	private void addTruvoMongoDBRiver() {
		// TODO Auto-generated method stub

	}

	private static String readConfig(String locale) {
		StringBuilder text = new StringBuilder("{\"");
		text.append(locale).append("\":");
		try {
			String NL = System.getProperty("line.separator");
			File file = new File("../conf/fields.yaml");
			// Resource resource = new FileSystemResource(file);
			Scanner scanner = new Scanner(new FileInputStream(file), "UTF-8");
			while (scanner.hasNextLine()) {
				text.append(scanner.nextLine() + NL);
			}
			text.append("}");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return text.toString();
	}

	private void createIndex(Client client) {
		try {
			client.admin().indices().prepareCreate(ES_INDEX_NAME).execute().actionGet();

		} catch (IndexAlreadyExistsException ex) {
			logger.info("index " + ES_INDEX_NAME + " already exists");
		} catch (ElasticSearchException exception) {
			exception.printStackTrace();
		}
	}

	@Override
	@ManagedOperation
	public void importUnprocessedBusinessesIntoMongoAndESIndex() {
		this.bulkImporter.index();
	}

}
