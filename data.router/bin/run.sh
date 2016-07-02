#!/bin/bash

ZK_HOST=`cat .zkhost.properties`
export ZK_HOST=$ZK_HOST

./kafka_2.11-0.10.0.0/bin/zookeeper-server-start.sh \
    ./kafka_2.11-0.10.0.0/config/zookeeper.properties &> zookeeper.log &

sleep 20

./kafka_2.11-0.10.0.0/bin/kafka-server-start.sh \
    ./kafka_2.11-0.10.0.0/config/server.properties &> kafka.log &

sleep 20

./kafka_2.11-0.10.0.0/bin/kafka-topics.sh --create --zookeeper $ZK_HOST:2181 \
    --replication-factor 1 --partitions 1 --topic rules
./kafka_2.11-0.10.0.0/bin/kafka-topics.sh --create --zookeeper $ZK_HOST:2181 \
    --replication-factor 1 --partitions 1 --topic forex
./kafka_2.11-0.10.0.0/bin/kafka-topics.sh --create --zookeeper $ZK_HOST:2181 \
    --replication-factor 1 --partitions 1 --topic forexJ
./kafka_2.11-0.10.0.0/bin/kafka-topics.sh --create --zookeeper $ZK_HOST:2181 \
    --replication-factor 1 --partitions 1 --topic ticks
./kafka_2.11-0.10.0.0/bin/kafka-topics.sh --create --zookeeper $ZK_HOST:2181 \
    --replication-factor 1 --partitions 1 --topic oandaticks
./kafka_2.11-0.10.0.0/bin/kafka-topics.sh --create --zookeeper $ZK_HOST:2181 \
    --replication-factor 1 --partitions 1 --topic histticks

./kafka_2.11-0.10.0.0/bin/kafka-topics.sh --list --zookeeper $ZK_HOST:2181
