package com.hr.securitygateway.digester;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ClientSecurity {

    @JacksonXmlProperty(localName = "Client")
    private List<Client> clients;

    public ClientSecurity() {
    }

    public List<Client> getClients() {
        return clients;
    }

    public void setClients(List<Client> clients) {
        this.clients = clients;
    }

    public static class Client {
        @JacksonXmlProperty(localName = "name", isAttribute=true)
        private String name;

        @JacksonXmlProperty(localName = "SecuritySetup")
        private SecuritySetup securitySetup;

        @JacksonXmlProperty(localName = "InternalApi")
        private InternalApi internalApi;

        @JacksonXmlProperty(localName = "IpAddresses")
        private List<String> ipAddresses;

        public Client() {
            ipAddresses = new ArrayList<String>();
        }

        public void addIpAddress(String addr) {
            ipAddresses.add(addr);
        }

        public List<String> getIpAddresses() {
            return ipAddresses;
        }

        public void setIpAddresses(List<String> ipAddresses) {
            this.ipAddresses = ipAddresses;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public SecuritySetup getSecuritySetup() {
            return securitySetup;
        }

        public void setSecuritySetup(SecuritySetup securitySetup) {
            this.securitySetup = securitySetup;
        }

        public InternalApi getInternalApi() {
            return internalApi;
        }

        public void setInternalApi(InternalApi internalApi) {
            this.internalApi = internalApi;
        }
    }

    public static class SecuritySetup {
        @JacksonXmlProperty(localName = "AccessKeyId")
        private String accessKeyId;

        @JacksonXmlProperty(localName = "AccessSecretPass")
        private String accessSecretPass;

        public String getAccessKeyId() {
            return accessKeyId;
        }

        public void setAccessKeyId(String accessKeyId) {
            this.accessKeyId = accessKeyId;
        }

        public String getAccessSecretPass() {
            return accessSecretPass;
        }

        public void setAccessSecretPass(String accessSecretPass) {
            this.accessSecretPass = accessSecretPass;
        }
    }

    public static class InternalApi {
        @JacksonXmlProperty(localName = "UserId")
        private String userId;

        @JacksonXmlProperty(localName = "UserPass")
        private String userPass;

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUserPass() {
            return userPass;
        }

        public void setUserPass(String userPass) {
            this.userPass = userPass;
        }
    }



}
