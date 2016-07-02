package com.fcnlabs.hist

import kafka.javaapi.producer.Producer
import kafka.producer.KeyedMessage
import kafka.producer.ProducerConfig
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class PublishHistTicks {

    private static final Logger log = LogManager.getLogger(PublishHistTicks.class)

    public static void main(String[] args) {
        def env = System.getenv()
        String cassandra_host = env['CQLSH_HOST']
        String kafka_host = env['KAFKA_HOST']
        String histdata_dir = env['HISTDATA_DIR']
        ZonedDateTime zonedDateTime = null
        LocalDateTime localDateTime = null
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd' 'HHmmssSSS")
        DateTimeFormatter cassie_formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'000Z'")
        String fcnDateTime = null

        final String ipAddress = kafka_host
        final int port = args.length > 1 ? Integer.parseInt(args[1]) : 9092
        log.info("Connecting to Kafka Broker at IP Address " + ipAddress + ":" + port + "...")

        Properties props = new Properties()

        //List of Kafka brokers. Complete list of brokers is not
        //required as the producer will auto discover the rest of
        //the brokers. Change this to suit your deployment

        //  DOCKER_HOST : 49092->9092
        props.put("metadata.broker.list", kafka_host + ":" + port.toString())

        // Serializer used for sending data to kafka. Since we are sending string,
        // we are using StringEncoder.
        props.put("serializer.class", "kafka.serializer.StringEncoder")

        // We want acks from Kafka that messages are properly received.
        props.put("request.required.acks", "1")

        ProducerConfig config = new ProducerConfig(props)
        Producer<String, String> producer = new Producer<String, String>(config)

        int i, j
        String jj
        String maxts = "2014-02-05T10:53:03.147000Z"

        for (i = 2014; i < 2017; i++) {
            for (j = 1; j < 13; j++) {
                jj = j.toString()
                if (j < 10) { jj = "0" + jj }

                Integer totalRecords = 0
                log.info("Processing " + histdata_dir + '/DAT_ASCII_EURUSD_T_' + i.toString() + jj + '.csv')

                Paths.get(histdata_dir + '/DAT_ASCII_EURUSD_T_' + i.toString() + jj + '.csv').withReader { reader ->
                    org.apache.commons.csv.CSVParser csv = new org.apache.commons.csv.CSVParser(reader, org.apache.commons.csv.CSVFormat.DEFAULT)

                    for (record in csv.iterator()) {
                        totalRecords++
                    }
                }

                Integer processedRecords = 0
                Double tickler = 0.10
                Date startTime = new Date()
                Paths.get(histdata_dir + '/DAT_ASCII_EURUSD_T_' + i.toString() + jj + '.csv').withReader { reader ->
                    org.apache.commons.csv.CSVParser csv = new org.apache.commons.csv.CSVParser(reader, org.apache.commons.csv.CSVFormat.DEFAULT)

                    log.info("Start time: " + new Date().toString())
                    for (record in csv.iterator()) {

                        // Create message to be sent to "tick_topic" topic with the tick
                        KeyedMessage<String, String> data = new KeyedMessage<String, String>("histticks", record)

                        // Send the message
                        producer.send(data)

                        processedRecords++

                        if ((processedRecords / totalRecords) > tickler) {
                            log.info((tickler * 100).toString() + "% records processed.  " + processedRecords.toString() + " / " + totalRecords.toString())
                            tickler = tickler + 0.10
                            Date intervalTime = new Date()
                            // interval between two times in milliseconds
                            Long interimInterval = intervalTime.getTime() - startTime.getTime()
                            log.info("Interval time: " + intervalTime.toString() + " Transactions Per Minute: " + (processedRecords / (interimInterval * 60000)).toString())

                        }
                    }
                }
                Date endTime = new Date()
                // interval between two times in milliseconds
                Long interval = endTime.getTime() - startTime.getTime()
                log.info("Stop time: " + new Date().toString() + " Transactions Per Minute: " + (processedRecords / (interval * 60000)).toString())
            }
        }
    }
}
