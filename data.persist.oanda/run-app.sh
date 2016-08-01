#!/bin/bash

echo "Starting data.persist.oanda..."

rm -rf *.log

java -jar ./build/libs/data.persist.oanda-all.jar >data_persist_oanda.log &



