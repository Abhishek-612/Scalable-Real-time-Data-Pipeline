package com.pipeline.datapipeline.dao.databases;

import com.mongodb.BasicDBObject;
import com.mongodb.client.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.bson.Document;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Component
public class MongoDBDatabase implements DatabaseSource {

    private static final Logger LOGGER = LogManager.getLogger();
    private MongoClient mongoClient = null;
    private MongoDatabase database = null;

    @Value("${mongodb.server}")
    private String mongoServer = "mongodb";
    @Value("${mongodb.port}")
    private String mongoPort = "27017";
    @Value("${mongodb.database}")
    private String mongoDatabase = "datasurge";


    // MongoDB Constants
    public static final String CREATE_COLLECTION = "CREATE_COLLECTION";
    public static final String DROP_COLLECTION = "DROP_COLLECTION";
    public static final String INSERT_TO_COLLECTION = "INSERT_TO_COLLECTION";
    public static final String FIND_IN_COLLECTION = "FIND_IN_COLLECTION";


    public MongoDBDatabase() {
    }

    @Override
    public void executeQuery(String query) {
        try {
            openConnection();

            String[] tokens = query.split(" ");
            String operation = tokens[0].toLowerCase();

            switch (operation) {
                // Implement MongoDB specific operations for 'executeQuery' here.
                // For example, you can create, update, or delete documents in a collection.

                case CREATE_COLLECTION:
                    createCollection(tokens[1]);
                    break;
                case DROP_COLLECTION:
                    deleteCollection(tokens[1]);
                    break;
                case INSERT_TO_COLLECTION:
                    insertToCollection(tokens[1]);
                // Add more MongoDB-specific operations as needed.
                default:
                    LOGGER.error("Unsupported operation: " + operation);
            }

            closeConnection();
        } catch (Exception e) {
            LOGGER.error("Could not execute MongoDB query: " + query + " Error: " + e.getMessage());
        }
    }

    @Override
    public Object fetchQuery(String query) {
        Object resultSet = null;

        try {
            openConnection();

            String[] tokens = query.split(" ");
            String operation = tokens[0].toLowerCase();

            switch (operation) {
                // Implement MongoDB specific operations for 'fetchQuery' here.
                // For example, you can query a collection and return the results.

                case FIND_IN_COLLECTION:
                    resultSet = find(tokens[1], tokens[2]);
                    break;
                // Add more MongoDB-specific operations as needed.

                default:
                    LOGGER.error("Unsupported operation: " + operation);
            }

            closeConnection();
        } catch (Exception e) {
            LOGGER.error("Could not execute MongoDB query: " + query + " Error: " + e.getMessage());
        }
        return resultSet;
    }

    @Override
    public void openConnection() {
        try {
            LOGGER.error(mongoServer+" "+mongoPort+" "+mongoDatabase);
            mongoClient = MongoClients.create("mongodb://" + mongoServer + ":" + mongoPort);
            database = mongoClient.getDatabase(mongoDatabase);
            LOGGER.info("MongoDB Connection successful.");
        } catch (Exception e) {
            LOGGER.error("MongoDB Connection could not be established! " + e.getMessage());
        }
    }

    @Override
    public void closeConnection() {
        try {
            if (mongoClient != null) {
                mongoClient.close();
                LOGGER.info("MongoDB Connection closed!");
            } else {
                throw new Exception("MongoDB Connection is null");
            }
        } catch (Exception e) {
            LOGGER.warn("No MongoDB connection to close! Details: " + e.getMessage());
        }
    }

    @Override
    public boolean checkConnection() {
        return (mongoClient != null);
    }

    private void createCollection(String collectionName) {
        database.createCollection(collectionName);
        LOGGER.info("Collection " + collectionName + " created successfully.");
    }

    private void deleteCollection(String collectionName) {
        database.getCollection(collectionName).drop();
        LOGGER.info("Collection " + collectionName + " deleted successfully.");
    }

    private void insertToCollection(String data) {

    }

    private Object find(String collectionName, String queryFilter) {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        List<Document> queryResult = new ArrayList<>();

        BasicDBObject query = BasicDBObject.parse(queryFilter);

        MongoCursor<Document> cursor = collection.find(query).iterator();
        try {
            while (cursor.hasNext()) {
                queryResult.add(cursor.next());
            }
        } finally {
            cursor.close();
        }

        return queryResult;
    }
}
