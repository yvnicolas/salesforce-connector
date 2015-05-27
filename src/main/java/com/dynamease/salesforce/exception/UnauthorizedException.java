package com.dynamease.salesforce.exception;

import com.fasterxml.jackson.core.JsonProcessingException;



/**
 * Created by Gregoire on 26/05/2015.
 */
public class UnauthorizedException extends SalesforceApiException {

    public final static String ERROR401 = "401";

    public UnauthorizedException(String msg, JsonProcessingException e) {
        super(msg, e);
    }

    public UnauthorizedException(String msg) {
        super(msg);
    }

    public UnauthorizedException(String msg, Throwable e) {
        super(msg, e);
    }

    public UnauthorizedException(Exception e) {
        super(e);
    }
}
