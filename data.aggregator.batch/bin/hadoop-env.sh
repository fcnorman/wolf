#!/bin/bash

# assume JAVA_HOME is already set correctly.  double-check, though.
HADOOP_INSTALL=/usr/local/hadoop-2.6.0
export HADOOP_INSTALL=$HADOOP_INSTALL

PATH=$PATH:$HADOOP_INSTALL/bin:$HADOOP_INSTALL/sbin
export PATH=$PATH

HADOOP_MAPRED_HOME=$HADOOP_INSTALL
export HADOOP_MAPRED_HOME=$HADOOP_MAPRED_HOME

HADOOP_COMMON_HOME=$HADOOP_INSTALL
export HADOOP_COMMON_HOME=$HADOOP_COMMON_HOME

HADOOP_HDFS_HOME=$HADOOP_INSTALL
export HADOOP_HDFS_HOME=$HADOOP_HDFS_HOME

YARN_HOME=$HADOOP_INSTALL
export YARN_HOME=$YARN_HOME

HADOOP_COMMON_LIB_NATIVE_DIR=$HADOOP_INSTALL/lib/native
export HADOOP_COMMON_LIB_NATIVE_DIR=$HADOOP_COMMON_LIB_NATIVE_DIR

HADOOP_OPTS="-Djava.library.path=$HADOOP_INSTALL/lib"
export HADOOP_OPTS=$HADOOP_OPTS
