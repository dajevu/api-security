package com.hr.securitygateway.exceptions;


import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecurityHandler implements Processor {

    private static final Logger log = LoggerFactory.getLogger(SecurityHandler.class);

    @Override
    public void process(Exchange exchange) throws Exception {

        Throwable caused = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Throwable.class);

        log.error("Processing error exception : " + caused);

        exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, "401");

        exchange.getOut().setBody("<b>Unauthorized - Please contact support</b>");

    }
}
