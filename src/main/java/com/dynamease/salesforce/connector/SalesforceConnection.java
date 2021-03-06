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

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * A Connection to SalesForce, using a WEB OAuth workflow
 * Created by Gregoire on 18/05/2015.
 */
public class SalesforceConnection {

    private static final ObjectMapper OBJECTMAPPER = new ObjectMapper();
    private static final String APILOGINURI = "https://login.salesforce.com/services/oauth2/token";
    private static final String APIURI = "/services/data/v33.0/";
    private static final String BUSINESS_OBJECT_URI = "sobjects/";
    private static final String QUERY_URI = "query/";

    private static final Logger logger = LoggerFactory.getLogger(SalesforceConnection.class);


    private String cacheFolder = null;
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

    /**
     *
     * @param clientId
     * @param clientSecret
     * @param accessToken
     * @param instanceUrl url on which SalesForceConection will connect to;
     *                    it is given by SalesForce
     * @param refreshToken
     */
    public void open(String clientId, String clientSecret, String accessToken, String instanceUrl, String refreshToken){
        this.accessToken=accessToken;
        this.sfInstanceUrl=instanceUrl;
        this.refreshToken_ =refreshToken;
        this.clientId_=clientId;
        this.clientSecret_=clientSecret;
        headers.set("Authorization", "Bearer " + accessToken);
    }


    /**
     * SalesForce Query entry point
     * You must provide a well formed request
     * @param request
     * @return
     * @throws SalesforceApiException
     */
    public Contact process(SalesforceRestRequest request) throws SalesforceApiException {
        String url = buildCompleteFieldsQuery(request);

        String c = execRestGetQuery(url, String.class);
        Contact contact  = null;
        try {
            JsonNode root = OBJECTMAPPER.readTree(c);
            if(root!=null && root.get("records")!=null && root.get("records").get(0)!=null){
                contact = OBJECTMAPPER.readValue(root.get("records").get(0).toString(), Contact.class);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contact;

    }



    private String buildCompleteFieldsQuery(SalesforceRestRequest request) throws SalesforceApiException {
        StringBuilder sb = new StringBuilder(sfInstanceUrl + APIURI + QUERY_URI);
        StringBuilder sbQuery = new StringBuilder("?q=SELECT ");

        List<String> fieldNamesList = request.filterFields(getFieldNamesList(request.getBusinessObject(), request.getResultDepth()));
        boolean first=true;
        for (String field : fieldNamesList) {
            if (first) {
                first = false;
            } else {
                sbQuery.append(",");
            }
            sbQuery.append(field);
        }


        sbQuery.append(" FROM "+request.getBusinessObject()+" "+request.getCriteria());
        return sb.append(sbQuery.toString()).toString();
    }

    private List<String> getFieldNamesList(String objectName, int depth) throws SalesforceApiException {
        File cacheFile = null;
        if(cacheFolder!=null){
            File folder = new File(cacheFolder);
            if(folder.exists() && folder.isDirectory()){
                cacheFile = new File(folder,objectName+"-"+depth);
                if(cacheFile.exists()){
                    String fileContent = readFile(cacheFile);
                    String[] fieldNames = fileContent.split(",");
                    return Arrays.asList(fieldNames);
                }
            }
        }
        Map fieldValuesMap = (Map)getFieldValues(objectName);
        List fields = (List)fieldValuesMap.get("fields");
        List<String> result = new ArrayList<String>();
        for (Object currentField : fields) {
            result.add((String) ((Map) currentField).get("name"));
            if (((Map) currentField).get("referenceTo") != null
                    && ((List)((Map) currentField).get("referenceTo")).size()!=0
                    && depth > 0) {
                List<String> subList = getFieldNamesList((String) ((List)((Map) currentField).get("referenceTo")).get(0), depth - 1);
                for (String s : subList) {
                    result.add((String) ((Map) currentField).get("relationshipName") + "." + s);
                }
            }
        }

        saveToFile(cacheFile, result);
        return result;
    }

    private void saveToFile(File cacheFile, List<String> fields) {
        if(cacheFile==null) return;
        BufferedWriter writer = null;
        try
        {
            writer = new BufferedWriter( new FileWriter( cacheFile));
            boolean first = true;
            for(String field:fields){
                if(!first){
                    writer.write(",");
                }
                writer.write(field);
                first=false;
            }
            writer.flush();
        }
        catch ( IOException e)
        {
            logger.error("Error writing fields:"+e);
        }
        finally
        {
            try
            {
                if ( writer != null)
                    writer.close( );
            }
            catch ( IOException e)
            {
                logger.error("Error closing file:"+cacheFile.getAbsolutePath());
            }
        }
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


    private String readFile(File file) {

        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            return new String(bytes,"UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }


    public void setCacheFolder(String cacheFolder) {
        this.cacheFolder = cacheFolder;
    }
}
