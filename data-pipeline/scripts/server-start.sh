#!/bin/bash

set -e


LOG_DIR=$(pwd)/scripts/logs
SERVER_LOG_DIR=${LOG_DIR}/server_logs
TMP_DIR=/tmp

KAFKA_PATH=/opt/homebrew/Cellar/kafka/3.4.0

CONFIG_FILE=servers_list.config
SCRIPT_LOG=${LOG_DIR}/server_setup.log
KAFKA_LOGS=${LOG_DIR}/kafka-logs

ZOOKEEPER_PROPS=/opt/homebrew/etc/kafka/zookeeper.properties


# Check if LOG_DIR exists, and create the directory if it doesn't exist
if [ ! -d "$LOG_DIR" ]; then
  mkdir -p "$LOG_DIR"
else 
  rm -rf ${LOG_DIR}/*
fi

if [ ! -d "$SERVER_LOG_DIR" ]; then
  mkdir -p "$SERVER_LOG_DIR"
fi




# Redirect script output to the log file
exec > >(tee -a ${SCRIPT_LOG})
exec 2>&1


# Error Trap
on_error() {
  echo "Script terminated unexpectedly."
  tail ${SCRIPT_LOG}
  exit 1
}
trap on_error ERR


echo "Script started at $(date)"

nohup zookeeper-server-start ${ZOOKEEPER_PROPS} > ${SERVER_LOG_DIR}/zookeeper_start.log 2>&1 &

echo "Zookeeper server started"

# Read the list of servers from the config file
while IFS=" " read -r broker_id server
do

  if [ -f ${KAFKA_LOGS}-${server}/.lock ]; then
      rm -f ${KAFKA_LOGS}-${server}/.lock
  fi

  if [ -f ${KAFKA_LOGS}-${server}/meta.properties ]; then
      rm -f ${KAFKA_LOGS}-${server}/meta.properties
  fi

    
  # Set up server.properties for the current server
  port=$(echo "${server}" | grep -oE '[0-9]+')
  echo "broker.id=${broker_id}" > ${TMP_DIR}/server_${broker_id}.properties
  echo "listeners=PLAINTEXT://:${port}" >> ${TMP_DIR}/server_${broker_id}.properties
  echo "log.dirs=${KAFKA_LOGS}-${server}" >> ${TMP_DIR}/server_${broker_id}.properties


  # Standard template for server.properties
  echo "num.network.threads=3" >> ${TMP_DIR}/server_${broker_id}.properties   # The number of threads that the server uses for receiving requests from the network and sending responses to the network
  echo "num.io.threads=8" >> ${TMP_DIR}/server_${broker_id}.properties  # The number of threads that the server uses for processing requests, which may include disk I/O
  echo "socket.send.buffer.bytes=102400" >> ${TMP_DIR}/server_${broker_id}.properties  # The send buffer (SO_SNDBUF) used by the socket server
  echo "socket.receive.buffer.bytes=102400" >> ${TMP_DIR}/server_${broker_id}.properties  # The receive buffer (SO_RCVBUF) used by the socket server
  echo "socket.request.max.bytes=104857600" >> ${TMP_DIR}/server_${broker_id}.properties  # The maximum size of a request that the socket server will accept (protection against OOM)
  echo "num.partitions=1" >> ${TMP_DIR}/server_${broker_id}.properties  # The number of partitions
  echo "num.recovery.threads.per.data.dir=1" >> ${TMP_DIR}/server_${broker_id}.properties  # The number of threads that the server uses for recovery
  # Internal Topic Settings
  echo "offsets.topic.replication.factor=1" >> ${TMP_DIR}/server_${broker_id}.properties
  echo "transaction.state.log.replication.factor=1" >> ${TMP_DIR}/server_${broker_id}.properties
  echo "transaction.state.log.min.isr=1" >> ${TMP_DIR}/server_${broker_id}.properties
  # Log Retention Policy
  echo "log.retention.hours=168" >> ${TMP_DIR}/server_${broker_id}.properties
  echo "log.retention.check.interval.ms=300000" >> ${TMP_DIR}/server_${broker_id}.properties
  # Zookeeper
  echo "zookeeper.connect=localhost:2181" >> ${TMP_DIR}/server_${broker_id}.properties  
  echo "zookeeper.connection.timeout.ms=18000" >> ${TMP_DIR}/server_${broker_id}.properties  # Timeout in ms for connecting to zookeeper
  # Group Coordinator Settings
  echo "group.initial.rebalance.delay.ms=0" >> ${TMP_DIR}/server_${broker_id}.properties

  chmod 777 ${TMP_DIR}/server_${broker_id}.properties


  # Start Kafka server for the current server
  sleep 2
  nohup kafka-server-start ${TMP_DIR}/server_${broker_id}.properties > "${SERVER_LOG_DIR}/broker_${broker_id}_logs.log" 2>&1 &

  echo "Kafka server started on ${server}"

done < "$CONFIG_FILE"

echo "Script finished at $(date)"
