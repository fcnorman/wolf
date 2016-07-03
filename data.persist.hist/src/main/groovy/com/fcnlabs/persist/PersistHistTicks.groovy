package com.fcnlabs.hist

import com.datastax.driver.core.LocalDate
import com.datastax.driver.core.exceptions.*
import com.fcnlabs.persist.CassandraConnector
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class PersistHistTicks {

    private static final Logger log = LogManager.getLogger(PersistHistTicks.class)

    public static void main(String[] args) {
        // Get configs from environment variables and
        // declare variables
        def env = System.getenv()
        String cassandra_host = env['CQLSH_HOST']
        String kafka_host = env['KAFKA_HOST']

        // Configure Cassandra Connection
        final CassandraConnector client = new CassandraConnector()
        final String ipAddress = cassandra_host;
        final int port = args.length > 1 ? Integer.parseInt(args[1]) : 9042;
        log.info("Connecting to IP Address " + ipAddress + ":" + port + "...");

        // Connect To Cassandra
        try {
            client.connect(ipAddress, port);
        } catch (Exception e) {
            log.error("Error connecting to Cassandra." + "\n\n" + e)
            return;
        }

        // Configure Kafka Connection
        Properties props = new Properties()
        props.put("bootstrap.servers", kafka_host + ":" + port.toString())
        props.put("group.id", "persist.hist.stream")
        props.put("enable.auto.commit", "true")
        props.put("auto.commit.interval.ms", "1000")
        props.put("session.timeout.ms", "30000")
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")

        // Connect To Kafka
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)

        // Subscribe To 'ticks' topic
        consumer.subscribe(Arrays.asList("histticks"))

        // Loop until we exit via Exception
        Date startTime = new Date()
        Integer processedRecords = 0
        Double tickler = 1000
        while (true) {
            // Get records from the Kafka topic 'ticks'
            ConsumerRecords<String, String> records = consumer.poll(100)
            for (ConsumerRecord<String, String> record : records) {
                log.debug("offset = " + record.offset() + " key = " + record.key() + " value = " + record.value())

                // record.value() example:   20120201 000003660,1.306600,1.306770,0
                //
                //
                // Row Fields:
                // DateTime Stamp,Bid Quote,Ask Quote,Volume
                //
                // DateTime Stamp Format:
                // YYYYMMDD HHMMSSNNN
                //
                // Legend:
                // YYYY – Year
                // MM – Month (01 to 12)
                // DD – Day of the Month
                // HH – Hour of the day (in 24h format)
                // MM – Minute
                // SS – Second
                // NNN – Millisecond

                def recValues = record.value().tokenize(',')

                def (rawDay, rawNanos) = recValues[0].tokenize(' ')

                LocalDate day = new LocalDate().fromYearMonthDay(rawDay.substring(0,4).toInteger(),
                                                                  rawDay.substring(4,2).toInteger(),
                                                                  rawDay.substring(6,2).toInteger())

                //Date dateNanos = new Date().parse('YYYYMMDD HHMMSSNNN', recValues[0], TimeZone.getTimeZone("EST"))

                //LocalDate day2 = new LocalDate().fromYearMonthDay(dateNanos.getYear(),
                //                                                  dateNanos.getMonth(),
                //                                                  dateNanos.getDay())

                Long nanos = 0
                nanos += rawNanos.substring(0,2).toLong() * 1000000000 * 60 *60
                nanos += rawNanos.substring(2,2).toLong() * 1000000000 * 60
                nanos += rawNanos.substring(4,2).toLong() * 1000000000
                nanos += rawNanos.substring(6,3).toLong() * 1000000

                Double bid = Double.parseDouble(recValues[1])
                Double ask = Double.parseDouble(recValues[2])
                //Integer volume = Integer.parseInt(recValues[3])

                //System.out.println(fcnDateTime);
                //System.out.println(bid);
                //System.out.println(ask);
                //CREATE TABLE IF NOT EXISTS eurusd_ticks (
                //        day   date,
                //        nanos time,
                //        bid   float,
                //        ask   float,
                //        PRIMARY KEY (day, nanos)
                //) WITH compaction = {'class' : 'LeveledCompactionStrategy' };

                //CREATE TABLE IF NOT EXISTS eurusd_ticks_tps (
                //        day  date,
                //        hour int,
                //        min  int,
                //        sec  int,
                //        tps  int,
                //        PRIMARY KEY (day, hour, min, sec)
                //) WITH compaction = {'class' : 'LeveledCompactionStrategy' };

                try {

                    client.getSession().execute(
                        "INSERT INTO hist_ticks.eurusd_ticks (day, nanos, bid, ask) VALUES (?, ?, ?, ?)",
                            day, nanos, bid, ask)

                } catch (NoHostAvailableException e) {
                    log.error("Error inserting tick to Cassandra.  NoHostAvailable." + "\n\n" + e)
                    client.close()
                    return
                } catch (UnavailableException e) {
                    log.error("Error inserting tick to Cassandra.  Unavailable." + "\n\n" + e)
                    client.close()
                    return
                } catch (ReadTimeoutException e) {
                    log.error("Error inserting tick to Cassandra.  ReadTimeout." + "\n\n" + e)
                    client.close()
                    return
                } catch (WriteTimeoutException e) {
                    log.error("Error inserting tick to Cassandra.  WriteTimeout." + "\n\n" + e)
                    client.close()
                    return;
                } catch (QueryValidationException e) {
                    log.error("Error inserting tick to Cassandra.  QueryValidation." + "\n\n" + e)
                    client.close()
                    return
                }

                processedRecords++

                if (processedRecords > tickler) {
                    Date intervalTime = new Date()

                    // interval between two times in milliseconds
                    Long interimInterval = intervalTime.getTime() - startTime.getTime()
                    Integer tps = processedRecords / (interimInterval * 1000)
                    log.info("Processed " + tickler + " records at tps of " + tps.toString())
                    tickler = tickler + tickler
                }
            }
        }
        // theoretical
        client.close()
    }
}
