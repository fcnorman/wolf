#!/bin/bash

echo "Starting data.persist.oanda..."

rm -rf *.log

java -jar ./build/libs/data.persist.oanda-all.jar 1>data-persist-oanda.out 2>data-persist-oanda.err &



