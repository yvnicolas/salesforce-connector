package com.dynamease.salesforce.connector;

/**
 * Created by Gregoire on 18/05/2015.
 */
public class SalesforceRestRequest {

    String value_;
    Type type_;

    public SalesforceRestRequest(Type requestType,String value) {
        value_ = value;
        type_ = requestType;
    }

    public String getValue() {
        return value_;
    }


    public Type getType() {
        return type_;
    }


    public enum Type{
        PHONE_NUMBER
    }
}
