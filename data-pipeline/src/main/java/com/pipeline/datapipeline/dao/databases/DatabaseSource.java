package com.pipeline.datapipeline.dao.databases;

public interface DatabaseSource {
    void openConnection() throws Exception;
    void executeQuery(String query);
    Object fetchQuery(String query);
    void closeConnection() throws Exception;
    boolean checkConnection();
}
