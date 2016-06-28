package com.fcnlabs.oanda

// Get tickstream data from OANDA's streaming tick api and immediately
// publish it to Kafka queue eurusd_ticks

import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.ContentType.*
import groovy.json.JsonSlurper
import java.io.IOException
import java.io.InputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.sql.Timestamp

import org.apache.http.*
import org.apache.http.client.methods.*
import org.apache.http.impl.client.BasicResponseHandler
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.client.HttpClient
import org.apache.http.message.BasicHeader
import org.apache.http.util.EntityUtils

import org.json.simple.JSONObject
import org.json.simple.JSONValue

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import kafka.producer.KeyedMessage
import kafka.javaapi.producer.Producer
import kafka.producer.ProducerConfig
import scala.collection.Seq

class PublishTickStream {

    public static void main (String[]args) {
        def env                     = System.getenv()
        String api_key              = env['OANDA_API_KEY']
        String api_id               = env['OANDA_API_ID']
        String kafka_host           = env['KAFKA_HOST']
        String zk_host              = env['ZK_HOST']
        def authMap                 = [:]
        ZonedDateTime zonedDateTime = null
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:MM:ss.SSSZ");
        String isoDateTime          = null
        Long ts                     = null

        authMap['Authorization'] = "Bearer " + api_key

        def httpBuilder = new HTTPBuilder('https://stream-fxtrade.oanda.com')

        HttpClient httpClient = HttpClientBuilder.create().build()

        final String ipAddress = kafka_host;
        final int port = args.length > 1 ? Integer.parseInt(args[1]) : 9092;  
        System.out.println("Connecting to Kafka Broker at IP Address " + ipAddress + ":" + port + "...");  

        Properties props = new Properties()

        //List of Kafka brokers. Complete list of brokers is not
        //required as the producer will auto discover the rest of
        //the brokers. Change this to suit your deployment

        //  DOCKER_HOST : 49092->9092
        props.put("metadata.broker.list", kafka_host + ":9092")

        // Serializer used for sending data to kafka. Since we are sending string,
        // we are using StringEncoder.
        props.put("serializer.class", "kafka.serializer.StringEncoder");

        // We want acks from Kafka that messages are properly received.
        props.put("request.required.acks", "1");

        ProducerConfig config = new ProducerConfig(props)
        Producer<String, String> producer = new Producer<String, String>(config)

        try {

            String domain 	= "https://stream-fxtrade.oanda.com"
            String accountId 	= api_id
            String instruments 	= "EUR_USD"

            HttpUriRequest httpGet = new HttpGet(domain + "/v1/prices?accountId=" + accountId + "&instruments=" + instruments);
            httpGet.setHeader(new BasicHeader("Authorization", "Bearer " + api_key));

            System.out.println("Executing request: " + httpGet.getRequestLine());

            HttpResponse resp = httpClient.execute(httpGet);
            HttpEntity entity = resp.getEntity();

            if (resp.getStatusLine().getStatusCode() == 200 && entity != null) {

                InputStream stream = entity.getContent();
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(stream));

                while ((line = br.readLine()) != null) {

                    Object obj = JSONValue.parse(line);
                    JSONObject tick = (JSONObject) obj;

                    // unwrap if necessary
                    if (tick.containsKey("tick")) {
                        tick = (JSONObject) tick.get("tick");


                    }

                    String data_row = null

                    // ignore heartbeats
                    if (tick.containsKey("instrument")) {
                        System.out.println("-------");

                        String instrument = tick.get("instrument").toString();
                        String time = tick.get("time").toString();
                        zonedDateTime = ZonedDateTime.parse(time)
                        isoDateTime = zonedDateTime.format(formatter)
                        double bid = Double.parseDouble(tick.get("bid").toString());
                        double ask = Double.parseDouble(tick.get("ask").toString());

                        System.out.println(instrument);
                        System.out.println(time);
                        //System.out.println('ZonedDateTime: ' + zonedDateTime.toString())
                        //System.out.println('ISO 8601 Time: ' + isoDateTime.toString())
                        //ts = zonedDateTime.toInstant().getEpochSecond() * 1000L
                        System.out.println(bid);
                        System.out.println(ask);

                        data_row = instrument + "|" + time + "|" + bid + "|" + ask

                        // Create message to be sent to "tick_topic" topic with the tick
                        KeyedMessage<String, String> data = new KeyedMessage<String, String>("ticks", data_row)

                        // Send the message
                        producer.send(data)
                    }
                }
            } else {
                // print error message
                String responseString = EntityUtils.toString(entity, "UTF-8");
                System.out.println(responseString)
            }
        } catch (Exception e) {
            println("Error processing OANDA ticks." + "\n\n" + e)
            httpClient.getConnectionManager().shutdown();
            producer.close();
            return;
        } finally {
            httpClient.getConnectionManager().shutdown();
            producer.close();
        }
    }
}

