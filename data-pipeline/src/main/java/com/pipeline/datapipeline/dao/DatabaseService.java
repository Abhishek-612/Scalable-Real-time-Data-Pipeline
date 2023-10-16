package com.pipeline.datapipeline.dao;

import com.pipeline.datapipeline.dao.databases.CassandraDatabase;
import com.pipeline.datapipeline.dao.databases.DatabaseSource;
import com.pipeline.datapipeline.dao.databases.HBaseDatabase;
import com.pipeline.datapipeline.dao.databases.MongoDBDatabase;
import com.pipeline.datapipeline.utils.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class DatabaseService {

    private static final Logger LOGGER = LogManager.getLogger();
    private static DatabaseSource databaseSource = null;

    public static DatabaseSource getDatabase(String dbName) {
        switch(dbName) {
            case Constants.MONGODB:
                databaseSource = new MongoDBDatabase();
                break;
            case Constants.CASSANDRA:
                databaseSource = new CassandraDatabase();
                break;
            case Constants.HBASE:
                databaseSource = new HBaseDatabase();
                break;
            default:
                LOGGER.error(dbName + " database is not available yet!");
        }
        return databaseSource;
    }

}
