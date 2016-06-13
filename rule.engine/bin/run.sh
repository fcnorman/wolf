#!/bin/bash

# do I really need autopurge?
#sudo echo autopurge.purgeInterval=24 >> /etc/zookeeper/conf/zoo.cfg
#sudo echo autopurge.snapRetainCount=5 >> /etc/zookeeper/conf/zoo.cfg
sudo /usr/share/zookeeper/bin/zkServer.sh stop
sleep 10
sudo /usr/share/zookeeper/bin/zkServer.sh start
sleep 10

# check if zookeeper is running
echo stat | nc localhost 2181

# run nimbus
# old version ./apache-storm-0.9.2-incubating/bin/storm nimbus &
# new version
./apache-storm-1.0.1/bin/storm nimbus >nimbus.log
sleep 10
# run storm ui
# old version ./apache-storm-0.9.2-incubating/bin/storm ui &
# new version
./apache-storm-1.0.1/bin/storm ui >ui.log
sleep 10
# run storm supervisor
# old version ./apache-storm-0.9.2-incubating/bin/storm supervisor &
# new version
./apache-storm-1.0.1/bin/storm supervisor >supervisor.log
sleep 10
# now build topology
# old version ./apache-storm-0.9.2-incubating/bin storm jar ./wolf/rule.engine/target/rule.engine-0.0.1-SNAPSHOT-jar-with-dependencies.jar rule.engine.RuleEngineTopology RuleEngine
# new version
cd ..
java -jar ./build/libs/rule.engine-all.jar RuleEngine >ruleengine.log

