package com.pipeline.datapipeline.dao.databases;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;

import java.io.IOException;
import java.util.HashMap;
import java.util.NavigableMap;


public class HBaseDatabase implements DatabaseSource {

    private static final Logger LOGGER = LogManager.getLogger();
    private Configuration config = null;

    @Value("${hbase.server}")
    private String zookeeperQuorum;
    @Value("${hbase.zookeeper.property.clientPort}")
    private String zookeeperClientPort;

    private Connection connection = null;

    public HBaseDatabase() {

        config = HBaseConfiguration.create();
        config.set("hbase.zookeeper.quorum", zookeeperQuorum);
        config.set("hbase.zookeeper.property.clientPort", zookeeperClientPort);
    }


    @Override
    public void executeQuery(String query) {
        try {
            openConnection();

            String[] tokens = query.split(" ");
            String operation = tokens[0].toLowerCase();

            switch (operation) {
                case "createtable":
                    createTable(tokens[1], tokens);
                    break;
                case "deletetable":
                    deleteTable(tokens[1]);
                    break;
                case "addcolumnfamily":
                    addColumnFamily(tokens[1], tokens[2]);
                    break;
                case "removecolumnfamily":
                    removeColumnFamily(tokens[1], tokens[2]);
                    break;
                case "updatecolumnvalue":
                    updateColumnValue(tokens[1], tokens[2], tokens[3], tokens[4], tokens[5]);
                    break;
                case "insertvalue":
                    insertValue(tokens[1], tokens[2], tokens[3], tokens[4], tokens[5]);
                    break;
                default:
                    LOGGER.error("Unsupported operation: " + operation);
            }

            closeConnection();
        } catch(Exception e) {
            LOGGER.error("Could not execute query: " + query + "Error: " + e.getMessage());
        }
    }

    @Override
    public Object fetchQuery(String query) {
        Object resultSet = null;

        try {
            openConnection();

            String[] tokens = query.split(" ");
            String tableName = tokens[1];
            String rowKey = tokens[2];
            String columnFamily = tokens[3];
            String[] columnNames = tokens;

            TableName table = TableName.valueOf(tableName);
            Table hbaseTable = connection.getTable(table);

            // Create a Get object to retrieve the specific column
            Get get = new Get(Bytes.toBytes(rowKey));
            for (String columnName : columnNames) {
                get.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes(columnName));
            }

            // Retrieve the data from the table
            Result result = hbaseTable.get(get);

            // Display the retrieved value
            if (!result.isEmpty()) {
                NavigableMap<byte[], byte[]> familyMap = result.getFamilyMap(Bytes.toBytes(columnFamily));

                HashMap<String, String> resultMap = new HashMap<>();

                for (String columnName : columnNames) {
                    byte[] value = familyMap.get(Bytes.toBytes(columnName));
                    resultMap.put(columnName, (value != null) ? Bytes.toString(value) : null);
                }

                resultSet = resultMap;
            } else {
                LOGGER.error("Row does not exist.");
            }

            closeConnection();
        } catch(Exception e) {
            LOGGER.error("Could not execute query: " + query + "Error: " + e.getMessage());
        }
        return resultSet;
    }

    private void createTable(String tableName, String[] columnFamilies) throws IOException {
        TableName table = TableName.valueOf(tableName);

        if (connection.getAdmin().tableExists(table)) {
            LOGGER.error("Table " + tableName + " already exists.");
            return;
        }

        HTableDescriptor descriptor = new HTableDescriptor(table);

        for (int i = 2; i < columnFamilies.length; i++) {
            HColumnDescriptor columnDescriptor = new HColumnDescriptor(columnFamilies[i]);
            descriptor.addFamily(columnDescriptor);
        }

        connection.getAdmin().createTable(descriptor);
        LOGGER.info("Table " + tableName + " created successfully.");
    }

    private void deleteTable(String tableName) throws IOException {
        TableName table = TableName.valueOf(tableName);

        if (!connection.getAdmin().tableExists(table)) {
            LOGGER.error("Table " + tableName + " does not exist.");
            return;
        }

        if (connection.getAdmin().isTableEnabled(table)) {
            connection.getAdmin().disableTable(table);
        }

        connection.getAdmin().deleteTable(table);
        LOGGER.info("Table " + tableName + " deleted successfully.");
    }

    private void addColumnFamily(String tableName, String columnFamilyName) throws IOException {
        TableName table = TableName.valueOf(tableName);

        if (!connection.getAdmin().tableExists(table)) {
            LOGGER.error("Table " + tableName + " does not exist.");
            return;
        }

        HColumnDescriptor columnDescriptor = new HColumnDescriptor(columnFamilyName);
        connection.getAdmin().addColumnFamily(table, columnDescriptor);
        LOGGER.info("Column family " + columnFamilyName + " added to table " + tableName + ".");
    }

    private void removeColumnFamily(String tableName, String columnFamilyName) throws IOException {
        TableName table = TableName.valueOf(tableName);

        if (!connection.getAdmin().tableExists(table)) {
            LOGGER.error("Table " + tableName + " does not exist.");
            return;
        }

        if (!connection.getAdmin().isTableEnabled(table)) {
            connection.getAdmin().enableTable(table);
        }

        connection.getAdmin().deleteColumnFamily(table, Bytes.toBytes(columnFamilyName));
        LOGGER.info("Column family " + columnFamilyName + " removed from table " + tableName + ".");
    }

    private void insertValue(String tableName, String rowKey, String columnFamily, String columnName, String value) throws IOException {
        TableName table = TableName.valueOf(tableName);
        Table hbaseTable = connection.getTable(table);

        // Create a Put object to insert the new value
        Put put = new Put(Bytes.toBytes(rowKey));
        put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes(columnName), Bytes.toBytes(value));

        // Put the data into the table
        hbaseTable.put(put);

        LOGGER.info("Value inserted successfully.");
    }

    private void updateColumnValue(String tableName, String rowKey, String columnFamily, String columnName, String newValue) throws IOException {
        TableName table = TableName.valueOf(tableName);
        Table hbaseTable = connection.getTable(table);

        // Check if the row exists
        Get get = new Get(Bytes.toBytes(rowKey));
        Result result = hbaseTable.get(get);

        if (!result.isEmpty()) {
            Put put = new Put(Bytes.toBytes(rowKey));
            put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes(columnName), Bytes.toBytes(newValue));

            // Put the updated data back into the table
            hbaseTable.put(put);

            LOGGER.info("Column updated successfully.");
        } else {
            LOGGER.error("Row does not exist.");
        }
    }


    @Override
    public void openConnection() {
        try {
            connection = ConnectionFactory.createConnection(config);
            LOGGER.info("HBase Connection successful.");
        } catch(Exception e) {
            LOGGER.error("HBase Connection could not be established!");
        }
    }

    @Override
    public void closeConnection() {
        try {
            if (connection != null) {
                connection.close();
                LOGGER.info("HBase Connection closed!");
            } else {
                throw new Exception("Connection is null");
            }
        } catch(Exception e) {
            LOGGER.warn("No connection to close! Details: " + e.getMessage());
        }
    }

    @Override
    public boolean checkConnection() {
        return false;
    }
}
