package com.dynamease.salesforce.exception;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.UnsupportedEncodingException;

/**
 * Created by Gregoire on 18/05/2015.
 */
public class SalesforceApiException extends Exception {

    public SalesforceApiException(String msg, JsonProcessingException e) {
        super(msg,e);
    }

    public SalesforceApiException(String msg) {
        super(msg);
    }

    public SalesforceApiException(String msg, Throwable e) {
        super(msg,e);
    }

    public SalesforceApiException(Exception e) {
        super(e);
    }
}
