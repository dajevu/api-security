package com.hr.securitygateway;


import com.hr.securitygateway.rabbit.RabbitPublisher;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RabbitProcessor implements Processor {

    @Autowired
    private volatile RabbitPublisher publisher;

    @Override
    public void process(Exchange exchange) throws Exception {

        String messageBody = exchange.getIn().getBody(String.class);

        if (messageBody != null) {
            exchange.getOut().setBody(messageBody);
        }

        publisher.sendResponse(messageBody, exchange.getIn().getHeaders(), (String) exchange.getProperty(RabbitPublisher.CORRELATION_HEADER));

    }
}
