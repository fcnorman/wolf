#!/bin/bash

# old
#./kafka_2.9.2-0.8.1.1/bin/zookeeper-server-start.sh ./kafka_2.9.2-0.8.1.1/config/zookeeper.properties &> zookeeper.log &
#./kafka_2.9.2-0.8.1.1/bin/kafka-server-start.sh ./kafka_2.9.2-0.8.1.1/config/server.properties &> kafka.log &
#sleep 20

#./kafka_2.9.2-0.8.1.1/bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic rules
#./kafka_2.9.2-0.8.1.1/bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic forex
#./kafka_2.9.2-0.8.1.1/bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic forexJ

# new
./kafka_2.11-0.10.0.0/bin/zookeeper-server-start.sh \
    ./kafka_2.9.2-0.8.1.1/config/zookeeper.properties &> zookeeper.log &
./kafka_2.11-0.10.0.0/bin/kafka-server-start.sh \
    ./kafka_2.9.2-0.8.1.1/config/server.properties &> kafka.log &

sleep 20

./kafka_2.11-0.10.0.0/bin/kafka-topics.sh --create --zookeeper ec2-52-72-16-7.compute-1.amazonaws.com:2181 \
    --replication-factor 1 --partitions 1 --topic rules
./kafka_2.11-0.10.0.0/bin/kafka-topics.sh --create --zookeeper ec2-52-72-16-7.compute-1.amazonaws.com:2181 \
    --replication-factor 1 --partitions 1 --topic forex
./kafka_2.11-0.10.0.0/bin/kafka-topics.sh --create --zookeeper ec2-52-72-16-7.compute-1.amazonaws.com:2181 \
    --replication-factor 1 --partitions 1 --topic forexJ

