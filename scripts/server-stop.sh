#!/bin/bash

set -e


LOG_DIR=/Users/abhishekrevadekar/Downloads/logs
SERVER_LOG_DIR=${LOG_DIR}/server_logs
TMP_DIR=/tmp

SCRIPT_LOG=${LOG_DIR}/server_terminate.log
KAFKA_LOGS=/opt/homebrew/var/lib/kafka-logs

ZOOKEEPER_PROPS=/opt/homebrew/etc/kafka/zookeeper.properties


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

for server_config in `ls ${TMP_DIR}/server_*.properties`
do
    # Stop Kafka server for the current server
    nohup kafka-server-stop ${server_config}s >> "${SERVER_LOG_DIR}/kafka_terminate.log" 2>&1 &

    # Remove server config file
    sleep 2
    rm -f ${server_config}

    echo "Kafka server stopped on ${server_config}"

done

nohup zookeeper-server-stop ${ZOOKEEPER_PROPS} > ${SERVER_LOG_DIR}/zookeeper_terminate.log 2>&1 &
echo "Zookeeper server stopped"



echo "Script finished at $(date)"


