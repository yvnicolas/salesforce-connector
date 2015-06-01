package com.dynamease.salesforce.connector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A query object that should be used to query SalesForce connector
 * Created by Gregoire on 18/05/2015.
 */
public class SalesforceRestRequest {

    private String object_;
    private String criteria_;

    /**
     * Result depth of object we want to retrieve:
     * a depth of 1 may return object desired an its direct children
     * a depth of 2 will return object disered, its direct children, and children of its children
     */
    private int resultDepth=1;

    private String[] relationshipFilter = null;


    /**
     * A SalesForce Connector Query
     * @param businessObject The SalesForce Business Object on which we want to permor a query
     * @param criteria A SOQL query criteria ("WHERE machin=truc")
     * @param relationshipFilter An array of object which must be excluded from query result
     */
    public SalesforceRestRequest(String businessObject, String criteria, String... relationshipFilter) {
       object_=businessObject;
        criteria_=criteria;
        this.relationshipFilter = relationshipFilter;
    }
    public String getCriteria() {
        return criteria_;
    }

    public String getBusinessObject() {
        return object_;
    }

    public int getResultDepth() {
        return resultDepth;
    }

    public void setResultDepth(int resultDepth) {
        this.resultDepth = resultDepth;
    }


    protected List<String> filterFields(List<String> fields){
        if(relationshipFilter==null || relationshipFilter.length==0)
            return fields;
        List<String> result = new ArrayList<String>();
        for(String field:fields){
            if(!field.contains(".")){
                result.add(field);
            }else{
                for(String filter:relationshipFilter){
                    if(field.startsWith(filter+".")){
                        result.add(field);
                    }
                }
            }
        }
        return result;
    }


}
