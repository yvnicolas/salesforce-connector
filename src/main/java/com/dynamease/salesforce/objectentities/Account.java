package com.dynamease.salesforce.objectentities;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Json Bean used by jackson. Fields must be mapped using @JsonProperty
 * because of SalesForce field names that start with a upper case character
 *
 * Created by Gregoire on 19/05/2015.
 */
public class Account {
    @JsonProperty("Name")
    String name = null;

    @JsonProperty("Industry")
    String industry = null;

    @JsonProperty("Website")
    String website = null;

    @JsonProperty("AnnualRevenue")
    String annualRevenue = null;

    @JsonProperty("NumberOfEmployees")
    String numberOfEmployees = null;

    @JsonProperty("Site")
    String site = null;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getAnnualRevenue() {
        return annualRevenue;
    }

    public void setAnnualRevenue(String annualRevenue) {
        this.annualRevenue = annualRevenue;
    }

    public String getNumberOfEmployees() {
        return numberOfEmployees;
    }

    public void setNumberOfEmployees(String numberOfEmployees) {
        this.numberOfEmployees = numberOfEmployees;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    @Override
    public String toString() {
        return "Account{" +
                "name='" + name + '\'' +
                ", industry='" + industry + '\'' +
                ", website='" + website + '\'' +
                ", annualRevenue='" + annualRevenue + '\'' +
                ", numberOfEmployees='" + numberOfEmployees + '\'' +
                ", site='" + site + '\'' +
                '}';
    }
}
