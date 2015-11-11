package ch.icclab.cyclops.rabbitMQClient; /**
 * Copyright 2014 Zuercher Hochschule fuer Angewandte Wissenschaften
 * All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * @description Simple Influx-Db Client library - using maven build framework
 * @author Piyush Harsh
 * @contact: piyush.harsh@zhaw.ch
 * @date 18.08.2015
 */

import ch.icclab.cyclops.mcn.model.MCNEvent;
import ch.icclab.cyclops.support.database.influxdb.client.InfluxDBClient;
import ch.icclab.cyclops.util.Load;
import com.google.gson.Gson;
import com.rabbitmq.client.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.influxdb.dto.BatchPoints;

import java.io.IOException;

public class RabbitMQClient implements Runnable {
    final static Logger logger = LogManager.getLogger(RabbitMQClient.class.getName());

    // Class variables needed for RabbitMQ
    private String threadName;
    private Channel channel;
    private Connection connection;
    private String consumerTag;

    // Settings for the RabbitMQ
    private Load.RabbitMQSettings rabbitMQSettings;

    // Database connection
    InfluxDBClient db;

    /**
     * Standard constructor that will load RabbitMQ settings
     */
    public RabbitMQClient(String name) {
        this.threadName = name;
        this.connection = null;
        this.channel = null;
        this.rabbitMQSettings = Load.getInstance().getRabbitMQSettings();
        this.db = new InfluxDBClient();
    }

    /**
     * Will return channel for RabbitMQ connection
     * @return channel reference or null
     */
    private Channel getChannel() {
        // connect to the RabbitMQ based on settings from Load
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername(rabbitMQSettings.getRabbitMQUsername());
        factory.setPassword(rabbitMQSettings.getRabbitMQPassword());
        factory.setVirtualHost(rabbitMQSettings.getRabbitMQVirtualHost());
        factory.setHost(rabbitMQSettings.getRabbitMQHost());
        factory.setPort(rabbitMQSettings.getRabbitMQPort());
        try
        {
            // create new connection
            connection = factory.newConnection();

            // create/connect to the channel
            channel = connection.createChannel();
            channel.queueDeclare(rabbitMQSettings.getRabbitMQQueueName(), true, false, false, null);
        }
        catch(Exception ex)
        {
            logger.error("Couldn't start Rabbit MQ: " + ex.getMessage());
            ex.printStackTrace();
        }

        // return channel reference, or null
        return channel;
    }

    /**
     * Our entry point to the newly created thread
     */
    public void run() {
        registerForListening();
    }

    /**
     * Register for listening for RabbitMQ
     */
    private void registerForListening() {
        // get channel reference
        channel = getChannel();

        if(channel != null)
        {
            // create consumer
            Consumer consumer = handleMessageDelivery(channel);
            try
            {
                // start listening
                consumerTag = channel.basicConsume(rabbitMQSettings.getRabbitMQQueueName(), true, consumer);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * Stop listening to the RabbitMQ
     */
    public void stopListening() {
        try {
            logger.error("Trying to stop rabbitMQ");
            channel.basicCancel(consumerTag);
            channel.close();
            connection.close();

            channel = null;
            connection = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Process JSON message as string
     * @param message in JSON format
     * @return SlaViolation object
     */
    private MCNEvent processMessage(String message) {
        Gson gson = new Gson();

        MCNEvent data = gson.fromJson(message, MCNEvent.class);

        return data;
    }

    /**
     * This is the body of message processing
     * @param channel where consumer should listen
     * @return consumer object
     */
    private Consumer handleMessageDelivery(Channel channel) {
        return new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException
            {
                try
                {
                    // make sure encoding is correct
                    String message = new String(body, "UTF-8");

                    // now parse it
                    MCNEvent mcnEvent = processMessage(message);

                    if (mcnEvent != null) {
                        // request empty container
                        BatchPoints container = db.giveMeEmptyContainer();

                        // add a point
                        container.point(mcnEvent.toPoint());

                        // let the database save it
                        db.saveContainerToDB(container);
                    }

                }
                catch (Exception ex)
                {
                    System.err.println("Caught exception in client thread: " + ex.getMessage());
                }

            }
        };
    }
}
