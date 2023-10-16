package com.pipeline.datapipeline.utils;

import com.pipeline.datapipeline.dao.databases.CassandraDatabase;
import com.pipeline.datapipeline.dao.databases.HBaseDatabase;
import com.pipeline.datapipeline.dao.databases.MongoDBDatabase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DBQueryResolver {

    private static final Logger LOGGER = LogManager.getLogger();

    private static String getHBaseQuery(String queryType, Object query) {
        return null;
    }

    private static String getCassandraQuery(String queryType, Object query) {
        return null;
    }

    private static String getMongoDBQuery(String queryType, Object query) {
        switch(queryType) {
            case Constants.INSERT:
                return MongoDBDatabase.INSERT_TO_COLLECTION + " " + query.toString();
        }
        return null;
    }

    public static String getQuery(String dbName, String queryType, Object query) {
        switch(dbName) {
            case Constants.MONGODB:
                return getMongoDBQuery(queryType, query);
            case Constants.CASSANDRA:
                return getCassandraQuery(queryType, query);
            case Constants.HBASE:
                return getHBaseQuery(queryType, query);
            default:
                LOGGER.error(dbName + " database is not available yet!");
        }
        return null;
    }
}
