package com.hr.securitygateway.oauth;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AccessTokenDetails implements Serializable {

	private static final long serialVersionUID = 2565520926580311199L;
	private String jti;
	private String sub;
	private List<String> authorities;
	private List<String> scope;
	private String client_id;
	private String cid;
	private String grant_type;
	private String iat;
	private String iss;
	private List<String> aud;
	
	public String getJti() {
		return jti;
	}
	public void setJti(String jti) {
		this.jti = jti;
	}
	public String getSub() {
		return sub;
	}
	public void setSub(String sub) {
		this.sub = sub;
	}
	public List<String> getAuthorities() {
		return authorities;
	}
	public void setAuthorities(List<String> authorities) {
		this.authorities = authorities;
	}
	public List<String> getScope() {
		return scope;
	}
	public void setScope(List<String> scope) {
		this.scope = scope;
	}
	public String getClient_id() {
		return client_id;
	}
	public void setClient_id(String client_id) {
		this.client_id = client_id;
	}
	public String getCid() {
		return cid;
	}
	public void setCid(String cid) {
		this.cid = cid;
	}
	public String getGrant_type() {
		return grant_type;
	}
	public void setGrant_type(String grant_type) {
		this.grant_type = grant_type;
	}
	public String getIat() {
		return iat;
	}
	public void setIat(String iat) {
		this.iat = iat;
	}
	public String getIss() {
		return iss;
	}
	public void setIss(String iss) {
		this.iss = iss;
	}
	public List<String> getAud() {
		return aud;
	}
	public void setAud(List<String> aud) {
		this.aud = aud;
	}
	
	public static AccessTokenDetails getAccessTokenDetails(String rawJson) throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		
		return mapper.readValue(rawJson, AccessTokenDetails.class);
	}
	@Override
	public String toString() {
		return "AccessTokenDetails [jti=" + jti + ", sub=" + sub
				+ ", authorities=" + authorities + ", scope=" + scope
				+ ", client_id=" + client_id + ", cid=" + cid + ", grant_type="
				+ grant_type + ", iat=" + iat + ", iss=" + iss + ", aud=" + aud
				+ "]";
	}
	
	
}
/* Example json received from UAA
{
    "jti": "6priv8850345-2865-4ce7-86ad-68984708b1a9",
    "sub": "acme",
    "authorities": ["http://www.test.com/user", "http://www.test.com/api"],
    "scope": ["http://www.test.com/user", "http://www.test.com/api"],
    "client_id": "acme",
    "cid": "acme",
    "grant_type": "client_credentials",
    "iat": 1378489744,
    "iss": "http://localhost:8080/uaa/oauth/token",
    "aud": ["http://www.test"]
}
*/
