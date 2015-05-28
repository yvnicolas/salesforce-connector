package com.dynamease.salesforce.objectentities;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Json Bean used by jackson. Fields must be mapped using @JsonProperty
 * because of SalesForce field names that start with a upper case character
 *
 * Created by Gregoire on 19/05/2015.
 */
public class User {
    @JsonProperty("UserName")
    String userName = null;

    @JsonProperty("LastName")
    String lastName = null;

    @JsonProperty("FirstName")
    String firstName = null;

    @JsonProperty("Name")
    String name = null;

    @JsonProperty("CompanyName")
    String companyName = null;

    @JsonProperty("Division")
    String division = null;

    @JsonProperty("Department")
    String department = null;

    @JsonProperty("Title")
    String title = null;

    @JsonProperty("Email")
    String email = null;

    @JsonProperty("Phone")
    String phone = null;

    @JsonProperty("Fax")
    String fax = null;

    @JsonProperty("MobilePhone")
    String mobilePhone = null;

    @JsonProperty("Alias")
    String alias = null;


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getDivision() {
        return division;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getMobilePhone() {
        return mobilePhone;
    }

    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
