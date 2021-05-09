package com.mongodb.conection;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class AuthDBConnection {
	private static MongoDatabase authdb;

	public AuthDBConnection() {
		Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
		mongoLogger.setLevel(Level.SEVERE); // e.g. or Log.WARNING, etc.

		String connectionString = System.getProperty("mongodb.uri");
		System.out.println("Connecting to: " + connectionString);
		try {
			MongoClient mongoClient = MongoClients.create(connectionString);
			this.authdb = mongoClient.getDatabase("ADMIN_DB");
			System.out.println("Conected");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static MongoDatabase getDB() {
		new AuthDBConnection();
		return authdb;
	}

}
