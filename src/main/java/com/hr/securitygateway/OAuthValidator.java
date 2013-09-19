package com.hr.securitygateway;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.camel.Consume;
import org.apache.camel.Exchange;
import org.apache.camel.Headers;
import org.apache.camel.Properties;
import org.apache.commons.io.IOUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.hr.securitygateway.exceptions.HrSecurityException;
import com.hr.securitygateway.oauth.AccessTokenDetails;


public class OAuthValidator {
	
	private String uaaHost;
	private String uaaPort;
	private String uaaUsername;
	private String uaaPassword;
	
	@Consume()
	public void validateOAuth(@Properties Map<?, ?> properties, @Headers Map<?, ?> headers) throws HrSecurityException {
		AccessTokenDetails tokenDetails = null;

		try {
			tokenDetails = getTokenFromUaa((String) properties.get("AuthorizationToken"));
		} catch (ClientProtocolException e) {
			throw new HrSecurityException("Error accessing UAA server: " + e.getMessage());
		} catch (IOException e) {
			throw new HrSecurityException("Error validating token: " + e.getMessage());
		}

		System.out.println("tokanDetails are: " + tokenDetails);
		
		boolean urlValid = checkUrl(tokenDetails, (String) headers.get("CamelHttpPath"));
		
		if (!urlValid)
			throw new HrSecurityException("User not authorized for URL");
	}
	
	private boolean checkUrl(AccessTokenDetails accessToken, String inboundUrl) {
		
		for (String urlScope : accessToken.getScope()) {
			System.out.println("urlScope is: " + urlScope + " inboundUrl " + inboundUrl);
			if (inboundUrl.contains(urlScope)) {			
				return true;
			}
		}
		
		return false;
	}
	
	private AccessTokenDetails getTokenFromUaa(String token) throws ClientProtocolException, IOException {
		
		String responseText = null;
		
		HttpHost targetHost = new HttpHost(uaaHost, Integer.valueOf(uaaPort), "http");
		
		CredentialsProvider credsProvider = new BasicCredentialsProvider();
        
        credsProvider.setCredentials(
                new AuthScope(targetHost.getHostName(), targetHost.getPort()),
                new UsernamePasswordCredentials(uaaUsername, uaaPassword));
        
        CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider).build();
        
        try {

            // Create AuthCache instance
            AuthCache authCache = new BasicAuthCache();
            // Generate BASIC scheme object and add it to the local
            // auth cache
            BasicScheme basicAuth = new BasicScheme();
            authCache.put(targetHost, basicAuth);

            // Add AuthCache to the execution context
            HttpClientContext localContext = HttpClientContext.create();
            localContext.setAuthCache(authCache);

            HttpPost httpPost = new HttpPost("/uaa/check_token");

            List <NameValuePair> nvps = new ArrayList <NameValuePair>();
            nvps.add(new BasicNameValuePair("token", token));

            httpPost.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));
            
            System.out.println("executing request: " + httpPost.getRequestLine());
            System.out.println("to target: " + targetHost);

            CloseableHttpResponse response = httpclient.execute(targetHost, httpPost, localContext);
            try {
                HttpEntity entity = response.getEntity();
                
                responseText = IOUtils.toString( response.getEntity().getContent() );
            } finally {
                response.close();
            }
            
        } finally {
            httpclient.close();
        }
        
        return AccessTokenDetails.getAccessTokenDetails(responseText);
	}
	
	public static void main(String[] args) throws Exception {
        HttpHost targetHost = new HttpHost("localhost", 8080, "http");
        
        AccessTokenDetails accessToken;
        
        String responseText = null;
        
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        
        credsProvider.setCredentials(
                new AuthScope(targetHost.getHostName(), targetHost.getPort()),
                new UsernamePasswordCredentials("app", "appclientsecret"));
        
        CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider).build();
        
        try {

            // Create AuthCache instance
            AuthCache authCache = new BasicAuthCache();
            // Generate BASIC scheme object and add it to the local
            // auth cache
            BasicScheme basicAuth = new BasicScheme();
            authCache.put(targetHost, basicAuth);

            // Add AuthCache to the execution context
            HttpClientContext localContext = HttpClientContext.create();
            localContext.setAuthCache(authCache);

            HttpPost httpPost = new HttpPost("/uaa/check_token");

            List <NameValuePair> nvps = new ArrayList <NameValuePair>();
            nvps.add(new BasicNameValuePair("token", "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI2ODg1MDM0NS0yODY1LTRjZTctODZhZC02ODk4NDcwOGIxYTkiLCJzdWIiOiJhY21lIiwiYXV0aG9yaXRpZXMiOlsiaHR0cDovL3d3dy50ZXN0LmNvbS91c2VyIiwiaHR0cDovL3d3dy50ZXN0LmNvbS9hcGkiXSwic2NvcGUiOlsiaHR0cDovL3d3dy50ZXN0LmNvbS91c2VyIiwiaHR0cDovL3d3dy50ZXN0LmNvbS9hcGkiXSwiY2xpZW50X2lkIjoiYWNtZSIsImNpZCI6ImFjbWUiLCJncmFudF90eXBlIjoiY2xpZW50X2NyZWRlbnRpYWxzIiwiaWF0IjoxMzc4NDg5NzQ0LCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvdWFhL29hdXRoL3Rva2VuIiwiYXVkIjpbImh0dHA6Ly93d3cudGVzdCJdfQ.2HFXpdFU12Fh2ts0svcsf_tWfR_kTOZEnPavY89_YIQ"));

            httpPost.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));
            
            System.out.println("executing request: " + httpPost.getRequestLine());
            System.out.println("to target: " + targetHost);

            CloseableHttpResponse response = httpclient.execute(targetHost, httpPost, localContext);
            try {
                HttpEntity entity = response.getEntity();
                
                responseText = IOUtils.toString( response.getEntity().getContent() );
            } finally {
                response.close();
            }
            
        } finally {
            httpclient.close();
        }
        
        accessToken = AccessTokenDetails.getAccessTokenDetails(responseText);
        
        System.out.println("accessToken is: " + accessToken);
    }

	public String getUaaHost() {
		return uaaHost;
	}

	public void setUaaHost(String uaaHost) {
		this.uaaHost = uaaHost;
	}

	public String getUaaPort() {
		return uaaPort;
	}

	public void setUaaPort(String uaaPort) {
		this.uaaPort = uaaPort;
	}

	public String getUaaUsername() {
		return uaaUsername;
	}

	public void setUaaUsername(String uaaUsername) {
		this.uaaUsername = uaaUsername;
	}

	public String getUaaPassword() {
		return uaaPassword;
	}

	public void setUaaPassword(String uaaPassword) {
		this.uaaPassword = uaaPassword;
	}
	
	
}
