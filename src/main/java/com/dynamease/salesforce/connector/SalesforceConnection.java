package com.dynamease.salesforce.connector;

import com.dynamease.salesforce.exception.NoAuthenticationException;
import com.dynamease.salesforce.exception.SalesforceApiException;
import com.dynamease.salesforce.exception.UnauthorizedException;
import com.dynamease.salesforce.objectentities.Account;
import com.dynamease.salesforce.objectentities.Contact;
import com.dynamease.salesforce.objectentities.User;
import com.dynamease.salesforce.tool.TokenRefreshHandler;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by Gregoire on 18/05/2015.
 */
public class SalesforceConnection {

    private static final ObjectMapper OBJECTMAPPER = new ObjectMapper();
    private static final String APILOGINURI = "https://login.salesforce.com/services/oauth2/token";
    private static final String APIURI = "/services/data/v33.0/";
    private static final String BUSINESS_OBJECT_URI = "sobjects/";
    private static final String QUERY_URI = "query/";

    private static final Logger logger = LoggerFactory.getLogger(SalesforceConnection.class);
    private RestTemplate restTemplate = new RestTemplate();
    private HttpHeaders headers = new HttpHeaders();
    String accessToken = null;
    String sfInstanceUrl = null; //SalesForce Instance on which we are supposed to connect to once token retrieved
    String refreshToken_ =null;
    String clientId_ = null;
    String clientSecret_ = null;

    private TokenRefreshHandler  tokenRefreshHandler= null;


    public SalesforceConnection() throws SalesforceApiException {
        OBJECTMAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECTMAPPER.configure(MapperFeature.AUTO_DETECT_SETTERS, true);
    }

    public void open(String clientId, String clientSecret, String accessToken, String instanceUrl, String refreshToken){
        this.accessToken=accessToken;
        this.sfInstanceUrl=instanceUrl;
        this.refreshToken_ =refreshToken;
        this.clientId_=clientId;
        this.clientSecret_=clientSecret;
        headers.set("Authorization", "Bearer " + accessToken);
    }



    public Contact process(SalesforceRestRequest request) throws SalesforceApiException {
        String url = buildQueryUrl(request);

        String c = execRestGetQuery(url, String.class);
        Contact contact  = null;
        try {
            JsonNode root = OBJECTMAPPER.readTree(c);
            if(root!=null && root.get("records")!=null && root.get("records").get(0)!=null){
                contact = OBJECTMAPPER.readValue(root.get("records").get(0).toString(), Contact.class);
                contact.setUser(execRestGetQuery(sfInstanceUrl + APIURI + BUSINESS_OBJECT_URI+ "User/" + contact.getOwnerId(), User.class));
                contact.setAccount(execRestGetQuery(sfInstanceUrl + APIURI + BUSINESS_OBJECT_URI+ "Account/" + contact.getAccountId(), Account.class));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contact;

    }

    private String buildQueryUrl(SalesforceRestRequest request) throws SalesforceApiException {
        StringBuilder sb = new StringBuilder(sfInstanceUrl + APIURI + QUERY_URI);
        StringBuilder sbQuery = new StringBuilder("?q=SELECT ");

        boolean first = true;
        Map fieldValuesMap = (Map)getFieldValues("Contact");
        List fields = (List)fieldValuesMap.get("fields");
        for (Object currentField : fields) {
            if (first) {
                first = false;
            } else {
                sbQuery.append(",");
            }
            sbQuery.append(((Map) currentField).get("name"));
        }


        if (SalesforceRestRequest.Type.PHONE_NUMBER.equals(request.getType())) {
            sbQuery.append(" FROM Contact WHERE MobilePhone like '"+request.getValue()+"'");
                return sb.append(sbQuery.toString()).toString();

        }
        return null;
    }

    private <T> T execRestGetQuery(String s, Class<T> mapClass) throws SalesforceApiException {
        return execRestGetQuery(s,mapClass,true);
    }


    private <T> T execRestGetQuery(String url, Class<T> type, boolean firstAttempt) throws SalesforceApiException {
        if (accessToken == null) {
            throw new NoAuthenticationException("No access token available");
        }

        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        logger.debug("Submitting form value map {}", params.toString());
        logger.debug("Headers : {}", headers.toString());

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<MultiValueMap<String, String>>(params, this.headers);
        try {
            ResponseEntity<String> result = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            logger.debug("Result headers : {}", result.getHeaders().toString());
            logger.debug("Result body : {}", result.getBody());
            if(type.equals(String.class)){
                return (T) result.getBody();
            }
            T resultObject = OBJECTMAPPER.readValue( result.getBody(), type);
            return resultObject;
        } catch (Exception e) {
            logger.error(e +"");
            if(e.getMessage()!=null && e.getMessage().contains(UnauthorizedException.ERROR401)){
                if(tokenRefreshHandler!=null && firstAttempt){
                    try {
                        Map<String,String> newConnectionKeys = refreshAccessToken();
                        tokenRefreshHandler.handleRefresh(newConnectionKeys);
                        accessToken = newConnectionKeys.get("access_token");
                        sfInstanceUrl = newConnectionKeys.get("instance_url");
                        headers.set("Authorization", "Bearer " + accessToken);
                        return execRestGetQuery(url, type, false);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                else{
                    throw new UnauthorizedException(e);

                }
            }
        }
        return null;
    }

    public Map<String, Object> getFieldValues(String salesForceObjectName) throws SalesforceApiException {
        return execRestGetQuery(sfInstanceUrl + APIURI + BUSINESS_OBJECT_URI + salesForceObjectName + "/describe/", Map.class);
    }


    public void close() {
        accessToken = null;
        sfInstanceUrl = null;
        refreshToken_ = null;
        this.clientId_ = null;
        this.clientSecret_ = null;
    }

    public Map<String,String> refreshAccessToken() throws IOException {
        return refreshAccessToken(refreshToken_);
    }

    private Map<String,String> refreshAccessToken(String refreshToken) throws IOException {
        if(refreshToken==null){
            logger.warn("NO Refresh token:access token retrieval was not possible");
            return null;
        }
        MultiValueMap<String, String> salesforceCall = new LinkedMultiValueMap<String, String>();
        salesforceCall.add("grant_type", "refresh_token");
        salesforceCall.add("client_id", clientId_);
        salesforceCall.add("client_secret", clientSecret_);
        salesforceCall.add("refresh_token", refreshToken);

        logger.debug("Submitting form value map {}", salesforceCall.toString());
        logger.debug("Headers : {}", headers.toString());

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<MultiValueMap<String, String>>(salesforceCall, new HttpHeaders());
        ResponseEntity<String> result = restTemplate.exchange(APILOGINURI, HttpMethod.POST, entity, String.class);
        Map<String,String> res = OBJECTMAPPER.readValue(result.getBody(),Map.class);
        return res;
    }

    public Map<String,String> getAccessToken(String clientId, String clientSecret, String code, String redirect_uri) throws IOException {
        MultiValueMap<String, String> salesforceCall = new LinkedMultiValueMap<String, String>();
        salesforceCall.add("grant_type", "authorization_code");
        salesforceCall.add("client_id", clientId);
        salesforceCall.add("client_secret", clientSecret);
        salesforceCall.add("redirect_uri", redirect_uri);
        salesforceCall.add("code", code);

        logger.debug("Submitting form value map {}", salesforceCall.toString());
        logger.debug("Headers : {}", headers.toString());

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<MultiValueMap<String, String>>(salesforceCall, this.headers);
        ResponseEntity<String> result = restTemplate.exchange(APILOGINURI, HttpMethod.POST, entity, String.class);
        Map<String,String> res = OBJECTMAPPER.readValue(result.getBody(), Map.class);
        return res;
    }


    public void setTokenRefreshHandler(TokenRefreshHandler tokenRefreshHandler) {
        this.tokenRefreshHandler = tokenRefreshHandler;
    }


}
