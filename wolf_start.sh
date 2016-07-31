#!/bin/bash

PWD=`pwd`
git pull

cd data.router
source ./bin/run.sh

cd $PWD

cd data.provider.hist
gradle shadowJar
source ./run-app.sh

cd $PWD
cd data.persist.hist
gradle shadowJar
source ./run-app.sh


