package com.mongodb.conection;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerConnection {
	private List<Document> databases = new ArrayList<Document>();
	
	public ServerConnection( List<MongoDatabase> serverDatabases) {
		Logger mongoLogger = Logger.getLogger( "org.mongodb.driver" );
		mongoLogger.setLevel(Level.SEVERE); // e.g. or Log.WARNING, etc.
		
        String connectionString = System.getProperty("mongodb.uri");
        System.out.println("Connecting to: " + connectionString);
        try { 
        	MongoClient mongoClient = MongoClients.create(connectionString);
        	MongoDatabase ADMIN_DB = mongoClient.getDatabase("ADMIN_DB");
            MongoCollection<Document> dbs_collection = ADMIN_DB.getCollection("Databases");
            FindIterable<Document> iterable = dbs_collection.find();
            MongoCursor<Document> cursor = iterable.iterator();
            while (cursor.hasNext()) {
                this.databases.add(cursor.next());
            }
            System.out.println("FETCHED DATABASES: \n" + this.databases.toString());
            mongoClient.close();
            System.out.println("Disconnecting from: " + connectionString);
            //----------------------------------
            System.out.println("Fetching DBs.....");
            
            for(Document doc : this.databases) {
            	connectionString = doc.getString("credenciales");
                mongoClient = MongoClients.create(connectionString); 
                
            	serverDatabases.add(mongoClient.getDatabase(doc.getString("nombre")));
            	System.out.println(doc.getString("nombre"));    	
        	}
//           //en server
//        	iterable = this.serverDatabases.get(0).getCollection("Users").find();
//        	cursor = iterable.iterator();
//            while (cursor.hasNext()) {
//                System.out.println(cursor.next().toJson());
//                
//            }
        }catch(Error e){
        	e.printStackTrace();
        }
       
	}
	


    public List<Document> getDatabases() {
		return databases;
	}
	public void setDatabases(List<Document> databases) {
		this.databases = databases;
	}
}

