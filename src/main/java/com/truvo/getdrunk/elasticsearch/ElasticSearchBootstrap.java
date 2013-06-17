package com.truvo.getdrunk.elasticsearch;

import java.io.File;
import java.io.FileInputStream;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.truvo.getdrunk.elasticsearch.index.management.DataStoreManagerImpl;

public class ElasticSearchBootstrap {

	public static final String BOOTSTRAP_PROPERTIES = "business.indexer.bootstrap.properties";
	private static final String PROPERTY_KEY_COUNTRY = "target.country";

	private static final Logger logger = LoggerFactory.getLogger(ElasticSearchBootstrap.class);

	private static String country;

	/**
	 * ${workspace_loc:business-indexer}\bin
	 */
	public static void main(String[] args) throws Exception {
		File master = new File("../conf/" + BOOTSTRAP_PROPERTIES);
		if (master.exists()) {
			System.out.println("reading master settings from ");

			System.getProperties().load(new FileInputStream(master));
			System.getProperties().setProperty("USER", getUserName());

			showTargetProperties();

			country = System.getProperties().getProperty(PROPERTY_KEY_COUNTRY);

			ApplicationContext ctx = new ClassPathXmlApplicationContext("spring/context.xml");
			System.out.println();
			System.out.println("business indexer started");
			DataStoreManagerImpl dataStore = ctx.getBean("dataStoreManager", DataStoreManagerImpl.class);
			dataStore.refreshMongoAndESIndex();
			dataStore.importUnprocessedBusinessesIntoMongoAndESIndex();

			System.out.println("listening to incoming feeds...");

		} else {
			System.out.println("master settings file not found {}");

		}
	}

	private static void showTargetProperties() {
		System.out.println("-------------------------");
		for (Object key : new TreeSet(System.getProperties().keySet())) {
			String name = key.toString();
			if (name.startsWith("target")) {
				String value = System.getProperties().getProperty(name);
				System.out.println(name + "={}" + value);
			}
		}
		System.out.println("-------------------------");
	}

	/**
	 * Determines the username of the current user.
	 * 
	 * @return the username
	 */
	private static String getUserName() {
		String user = null;
		user = user == null ? System.getenv().get("USERNAME") : user; // Windows
		user = user == null ? System.getenv().get("USER") : user; // Linux
		return user;
	}

	public static String getCountry() {
		return country;
	}

	public static void setCountry(String input) {
		country = input;
	}

}
