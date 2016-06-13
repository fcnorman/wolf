#!/bin/bash

sudo add-apt-repository ppa:webupd8team/java -y
sudo apt-get update
sudo apt-get install oracle-java8-installer

wget http://mirror.cogentco.com/pub/apache/kafka/0.8.1.1/kafka_2.9.2-0.8.1.1.tgz
tar xvzf kafka_2.9.2-0.8.1.1.tgz

