package com.mongodb.conection;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bson.Document;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class ProxyConnection  {
	
	private static MongoCollection<Document> proxy_servers_collection; 
	
	public ProxyConnection() {
		Logger mongoLogger = Logger.getLogger( "org.mongodb.driver" );
		mongoLogger.setLevel(Level.SEVERE); // e.g. or Log.WARNING, etc.
		
        String connectionString = System.getProperty("mongodb.uri");
        System.out.println("Connecting to: " + connectionString);
        try { 
			MongoClient mongoClient = MongoClients.create(connectionString);
	    	MongoDatabase db = mongoClient.getDatabase("ADMIN_DB");
	    	this.proxy_servers_collection = db.getCollection("Servers");
	    	
        }catch (Error e) {
        	e.printStackTrace();
        }
	}
	
	public static MongoCollection<Document> setProxyMongoDBConnection(){
		new ProxyConnection();
		return proxy_servers_collection;
		
	}

}
