package com.fcnlabs.hist

import com.datastax.driver.core.exceptions.NoHostAvailableException
import com.datastax.driver.core.exceptions.QueryValidationException
import com.datastax.driver.core.exceptions.ReadTimeoutException
import com.datastax.driver.core.exceptions.UnavailableException
import com.datastax.driver.core.exceptions.WriteTimeoutException

import java.time.ZonedDateTime
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import org.apache.commons.csv.CSVParser
import static org.apache.commons.csv.CSVFormat.*
import java.nio.file.Paths

class PublishHistTicks {

    public static void main(String[] args) {
        def env = System.getenv()
        String cassandra_host = env['CQLSH_HOST']
        String histdata_dir = env['HISTDATA_DIR']
        ZonedDateTime zonedDateTime = null
        LocalDateTime localDateTime = null
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd' 'HHmmssSSS")
        DateTimeFormatter cassie_formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'000Z'")
        String fcnDateTime = null

        final CassandraConnector client = new CassandraConnector();
        final String ipAddress = cassandra_host;
        final int port = args.length > 1 ? Integer.parseInt(args[1]) : 9042;
        System.out.println("Connecting to IP Address " + ipAddress + ":" + port + "...");

        try {
            client.connect(ipAddress, port);
        } catch (Exception e) {
            println("Error connecting to Cassandra." + "\n\n" + e)
            return;
        }

        int i, j
        String jj
        String maxts = "2014-02-05T10:53:03.147000Z"

        for (i = 2014; i < 2017; i++) {
            for (j = 1; j < 13; j++) {
                jj = j.toString()
                if (j < 10) { jj = "0" + jj }
                Paths.get(histdata_dir + '/DAT_ASCII_EURUSD_T_' + i.toString() + jj + '.csv').withReader { reader ->
                    org.apache.commons.csv.CSVParser csv = new org.apache.commons.csv.CSVParser(reader, org.apache.commons.csv.CSVFormat.DEFAULT)

                    for (record in csv.iterator()) {
                        //println("Values 0: " + record.values[0])
                        //println("Values 1: " + record.values[1])
                        //println("Values 2: " + record.values[2])
                        //println("Values 3: " + record.values[3])

                        //System.out.println("-------");

                        localDateTime = LocalDateTime.parse(record.values[0], formatter)
                        fcnDateTime = localDateTime.format(cassie_formatter)
                        double bid = Double.parseDouble(record.values[1]);
                        double ask = Double.parseDouble(record.values[2]);

                        //System.out.println(fcnDateTime);
                        //System.out.println(bid);
                        //System.out.println(ask);
                        //CREATE TABLE IF NOT EXISTS eurusd_ticks (
                        //        day   date,
                        //        nanos time,
                        //        bid   float,
                        //ask   float,
                        //        PRIMARY KEY (day, nanos)
                        //) WITH compaction = {'class' : 'LeveledCompactionStrategy' };

                        //CREATE TABLE IF NOT EXISTS eurusd_ticks_tps (
                        //        day  date,
                        //        hour int,
                        //min  int,
                        //        sec  int,
                        //tps  int,
                        //        PRIMARY KEY (day, hour, min, sec)
                        //) WITH compaction = {'class' : 'LeveledCompactionStrategy' };


                        try {
                            if (fcnDateTime > maxts) {
                                client.getSession().execute(
                                        "INSERT INTO hist_ticks.eurusd_ticks (day, nanos, bid, ask) VALUES (?, ?, ?, ?)",
                                        day, nanos, bid, ask);
                            }
                        } catch (NoHostAvailableException e) {
                            println("Error inserting tick to Cassandra.  NoHostAvailable." + "\n\n" + e)
                            client.close();
                            return;
                        } catch (UnavailableException e) {
                            println("Error inserting tick to Cassandra.  Unavailable." + "\n\n" + e)
                            client.close();
                            return;
                        } catch (ReadTimeoutException e) {
                            println("Error inserting tick to Cassandra.  ReadTimeout." + "\n\n" + e)
                            client.close();
                            return;
                        } catch (WriteTimeoutException e) {
                            println("Error inserting tick to Cassandra.  WriteTimeout." + "\n\n" + e)
                            client.close();
                            return;
                        } catch (QueryValidationException e) {
                            println("Error inserting tick to Cassandra.  QueryValidation." + "\n\n" + e)
                            client.close();
                            return;
                        }
                    }
                }
            }
        }
        client.close()
    }
}
