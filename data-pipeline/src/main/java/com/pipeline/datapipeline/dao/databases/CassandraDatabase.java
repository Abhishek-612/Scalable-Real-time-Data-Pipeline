package com.pipeline.datapipeline.dao.databases;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

public class CassandraDatabase implements DatabaseSource {
    private static final Logger LOGGER = LogManager.getLogger();



    public CassandraDatabase() {
    }

    @Override
    public void executeQuery(String query) {

    }

    @Override
    public Object fetchQuery(String query) {
        // Implement query fetching logic here

        return null;
    }

    public void openConnection() {

    }

    public void closeConnection() {

    }

    @Override
    public boolean checkConnection() {
        return false;
    }
}
