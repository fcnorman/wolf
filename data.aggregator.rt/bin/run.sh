#!/bin/bash

sudo service cassandra stop

sleep 20

sudo service cassandra start

sleep 20

cqlsh -u cassandra -f ../src/1.create.table.cql

