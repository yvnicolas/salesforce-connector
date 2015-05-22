package com.dynamease.salesforce.objectentities;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Gregoire on 19/05/2015.
 */
public class Contact {



    @JsonProperty("OwnerId")
    String ownerId;

    @JsonProperty("AccountId")
    String accountId;

    @JsonProperty("LastName")
    String LastName = null;

    @JsonProperty("FirstName")
    String firstName = null;

    @JsonProperty("Name")
    String name = null;


    @JsonProperty("Phone")
    String phone = null;

    @JsonProperty("Fax")
    String fax = null;

    @JsonProperty("MobilePhone")
    String mobilePhone = null;

    @JsonProperty("HomePhone")
    String homePhone = null;

    @JsonProperty("OtherPhone")
    String otherPhone = null;

    @JsonProperty("AssistantPhone")
    String assistantPhone = null;

    @JsonProperty("Email")
    String email = null;

    @JsonProperty("Title")
    String title = null;

    @JsonProperty("Department")
    String department = null;

    @JsonProperty("BirthDate")
    String birthDate = null;

    Account account;



    User user;

    public String getLastName() {
        return LastName;
    }

    public void setLastName(String lastName) {
        this.LastName = lastName;
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

    public String getHomePhone() {
        return homePhone;
    }

    public void setHomePhone(String homePhone) {
        this.homePhone = homePhone;
    }

    public String getOtherPhone() {
        return otherPhone;
    }

    public void setOtherPhone(String otherPhone) {
        this.otherPhone = otherPhone;
    }

    public String getAssistantPhone() {
        return assistantPhone;
    }

    public void setAssistantPhone(String assistantPhone) {
        this.assistantPhone = assistantPhone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }
    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
