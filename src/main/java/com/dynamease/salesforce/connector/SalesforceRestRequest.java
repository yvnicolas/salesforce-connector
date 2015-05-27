package com.dynamease.salesforce.connector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Gregoire on 18/05/2015.
 */
public class SalesforceRestRequest {

    private String object_;
    private String criteria_;
    private int resultDepth=1;

    private String[] relationshipFilter = null;



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


    public List<String> filterFields(List<String> fields){
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
