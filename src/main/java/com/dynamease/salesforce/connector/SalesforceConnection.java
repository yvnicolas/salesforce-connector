package com.dynamease.salesforce.connector;

import com.dynamease.salesforce.objectentities.Account;
import com.dynamease.salesforce.objectentities.Contact;
import com.dynamease.salesforce.objectentities.User;
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
    private static final Logger logger = LoggerFactory.getLogger(SalesforceConnection.class);
    private RestTemplate restTemplate = new RestTemplate();
    private HttpHeaders headers = new HttpHeaders();
    String accessToken = null;
    String sfInstanceUrl = null; //SalesForce Instance on which we are supposed to connect to once token retrieved


    public SalesforceConnection() throws SalesforceApiException {
        OBJECTMAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECTMAPPER.configure(MapperFeature.AUTO_DETECT_SETTERS, true);
    }

    public void open(String clientId, String clientSecret, String login, String password) {
        MultiValueMap<String, String> salesforceCall = new LinkedMultiValueMap<String, String>();
        salesforceCall.add("grant_type", "password");
        salesforceCall.add("client_id", clientId);
        salesforceCall.add("client_secret", clientSecret);
        salesforceCall.add("username", login);
        salesforceCall.add("password", password);

        logger.debug("Submitting form value map {}", salesforceCall.toString());
        logger.debug("Headers : {}", headers.toString());

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<MultiValueMap<String, String>>(salesforceCall, this.headers);
        try {
            ResponseEntity<String> result = restTemplate.exchange(APILOGINURI, HttpMethod.POST, entity, String.class);
            logger.debug("Result headers : {}", result.getHeaders().toString());
            logger.debug("Result body : {}", result.getBody());
            Map<String, Object> resultBody = OBJECTMAPPER.readValue(result.getBody(), Map.class);
            accessToken = (String) resultBody.get("access_token");
            sfInstanceUrl = (String) resultBody.get("instance_url");
            logger.info("Result token : {}", accessToken);
        } catch (Exception e) {
            logger.error(e + "");
        }
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
                contact.setUser(execRestGetQuery(sfInstanceUrl + APIURI + "sobjects/User/" + contact.getOwnerId(), User.class));
                contact.setAccount(execRestGetQuery(sfInstanceUrl + APIURI + "sobjects/Account/" + contact.getAccountId(), Account.class));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contact;

    }

    private String buildQueryUrl(SalesforceRestRequest request) throws SalesforceApiException {
        StringBuilder sb = new StringBuilder(sfInstanceUrl + APIURI + "query/");
        StringBuilder sbQuery = new StringBuilder("?q=SELECT ");

        boolean first = true;
        for (Object currentField : ((List) getFieldValues("Contact").get("fields"))) {
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

    private <T> T execRestGetQuery(String url, Class<T> type) throws SalesforceApiException {
        if (accessToken == null) {
            throw new SalesforceApiException("No access token available");
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
        }
        return null;
    }

    public Map<String, Object> getFieldValues(String salesForceObjectName) throws SalesforceApiException {
        return execRestGetQuery(sfInstanceUrl + APIURI + "sobjects/" + salesForceObjectName + "/describe/", Map.class);
    }

    public void close() {
        accessToken = null;
        sfInstanceUrl = null;
    }
}
