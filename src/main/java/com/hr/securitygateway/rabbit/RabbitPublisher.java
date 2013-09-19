package com.hr.securitygateway.rabbit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

@Service
public class RabbitPublisher {
    
    public static final String CORRELATION_HEADER = "HrCorrelationId" ;
    public static final String TYPE_HEADER = "HrRequestReply";

    @Autowired
    private volatile RabbitTemplate amqpRequestTemplate;

    @Autowired
    private volatile RabbitTemplate amqpResponseTemplate;

    private static final Logger log = LoggerFactory.getLogger(RabbitPublisher.class);

    public void sendResponse(String message, Map<String, Object> properties, String correlationId) {

        Message rabbitMessage;

        MessageProperties messageProperties = buildMessageProperties(properties, correlationId);

        messageProperties.setHeader(TYPE_HEADER, "RESPONSE");

        if (message == null) {
            rabbitMessage = new Message("No MessageBody".getBytes(), messageProperties);
        } else {
            messageProperties.setContentLength(message.getBytes().length);
            rabbitMessage = new Message(message.getBytes(), messageProperties);
        }

        log.debug("Publishing to rabbit :: " + message);

        amqpResponseTemplate.send(rabbitMessage);

    }

    public void sendRequest(String message, Map<String, Object> properties, String correlationId) {
    	
    	System.out.println("Message is:: " + message);

        Message rabbitMessage;

        MessageProperties messageProperties = buildMessageProperties(properties, correlationId);

        messageProperties.setHeader(TYPE_HEADER, "REQUEST");


        if (message == null) {
            rabbitMessage = new Message("No MessageBody".getBytes(), messageProperties);
        } else {
            messageProperties.setContentLength(message.getBytes().length);
            rabbitMessage = new Message(message.getBytes(), messageProperties);
        }

        log.debug("Publishing to rabbit :: " + message);

        amqpRequestTemplate.send(rabbitMessage);

    }

    private MessageProperties buildMessageProperties(Map<String, Object> properties, String correlationId) {

        MessageProperties messageProperties;

        messageProperties = new MessageProperties();
        messageProperties.setContentType("application/json");
        messageProperties.setTimestamp(new Date());
        messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);

        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            messageProperties.setHeader(key, value);
        }

        messageProperties.setHeader(CORRELATION_HEADER, correlationId);

        return messageProperties;
    }


}
