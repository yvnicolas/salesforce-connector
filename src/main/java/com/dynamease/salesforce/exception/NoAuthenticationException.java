package com.dynamease.salesforce.exception;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Created by Gregoire on 26/05/2015.
 * Thrown when no authentication is provided to the API
 */
public class NoAuthenticationException extends SalesforceApiException{
    public NoAuthenticationException(String msg, JsonProcessingException e) {
        super(msg, e);
    }

    public NoAuthenticationException(String msg) {
        super(msg);
    }

    public NoAuthenticationException(String msg, Throwable e) {
        super(msg, e);
    }
}
