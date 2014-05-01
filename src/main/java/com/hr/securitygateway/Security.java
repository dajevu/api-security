package com.hr.securitygateway;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.hr.securitygateway.digester.ClientSecurity;
import com.hr.securitygateway.exceptions.HrSecurityException;
import com.hr.securitygateway.rabbit.RabbitPublisher;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.servlet.ServletRequest;
import java.io.IOException;

@Service
public class Security implements Processor {

    @Autowired
    private volatile RabbitPublisher publisher;

    private static final Logger log = LoggerFactory.getLogger(Security.class);

    private static final String AUTH_TOKEN = "AuthorizationToken";

    ClassPathResource resource = new ClassPathResource("/HrSecurity.xml");

    ClientSecurity security = null;

    public Security() throws IOException {

        XmlMapper xmlMapper = new XmlMapper();

        security =  xmlMapper.readValue(resource.getFile(), ClientSecurity.class);

        log.debug("Instantiated client db...loaded: " + security.getClients().size());
    }

    @Override
    public void process(Exchange exchange) throws Exception {

        String hash = null;

        ClientSecurity.Client client = null;

        String guid = java.util.UUID.randomUUID().toString();

        Message inMessage = exchange.getIn();

        String requestBody = inMessage.getBody(String.class);

        exchange.setProperty(RabbitPublisher.CORRELATION_HEADER, guid);

        log.debug("Request is:: " + requestBody);

        String accessToken = (String) exchange.getIn().getHeader("Authorization");

        if (accessToken == null) {
            log.error("No access token provided in inbound request");
            throw new HrSecurityException("Missing OAuth 2.0 Authorization Header");
        } else { // check for Bearer in header
        	if (accessToken.indexOf("Bearer") > -1) {
        		exchange.setProperty(AUTH_TOKEN, accessToken.substring(7));
        	} else {
                log.error("Invalid Access Token Provided - No Bearer in Header");
                throw new HrSecurityException("Invalid Access Token Provided - No Bearer in Header");
        	}
        		
        }

        log.debug("User:: " + accessToken + " attempting access...");

        String urlPath = inMessage.getHeader(Exchange.HTTP_PATH, String.class);
        log.info("Path ::" + urlPath);

        String contentType = inMessage.getHeader(Exchange.CONTENT_TYPE, String.class);
        log.debug("ContentType is:: " + contentType);

        ServletRequest request = (ServletRequest) inMessage.getHeader(Exchange.HTTP_SERVLET_REQUEST);
        String remoteAddress = request.getRemoteAddr();

        log.debug("Inbound IP address is:: " + remoteAddress);

        /*
        if (!lookupIpAddress(remoteAddress)) {
            log.error("User identified by " + accessToken + " has an invalid inbound IP address");
            throw new HrSecurityException("Invalid user ip address");
        }
        */

        exchange.getOut().setBody(requestBody);
        exchange.getOut().setHeader(Exchange.HTTP_PATH, urlPath);
        exchange.getOut().setHeader(Exchange.CONTENT_TYPE, contentType);

        publisher.sendRequest(requestBody, exchange.getIn().getHeaders(), guid);
    }

    private String computeHash(String securityToken, String signatureMethod) throws HrSecurityException {

        byte[] hash = null;

        if (signatureMethod.equalsIgnoreCase("HmacSHA256")) {
            hash = DigestUtils.sha256(securityToken);
        }  else if (signatureMethod.equalsIgnoreCase("HmacSHA512")) {
            hash = DigestUtils.sha512(securityToken);
        }  else {
            throw new HrSecurityException("Unable to compute Hash");
        }

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < hash.length; i++) {
            sb.append(Integer.toString((hash[i] & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();

    }

    public ClientSecurity.Client lookupUser(String accessKey, String signatureMethod ) throws HrSecurityException {

        if (accessKey == null)
            return null;

        for (ClientSecurity.Client client : security.getClients()) {
            if (client.getSecuritySetup().getAccessKeyId().equals(accessKey)) {
                return client;
            }
        }

        return null;

    }

    public boolean lookupIpAddress(String ipAddress) {

        for (ClientSecurity.Client client : security.getClients()) {

            for (String addr : client.getIpAddresses()) {
                 if (addr.equals(ipAddress))
                     return true;
            }
        }

        return false;
    }

}
