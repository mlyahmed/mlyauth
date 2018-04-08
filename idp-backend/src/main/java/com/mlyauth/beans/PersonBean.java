package com.mlyauth.beans;

import com.mlyauth.constants.RoleCode;

import java.util.Collection;
import java.util.LinkedHashSet;

public class PersonBean {

    private long id;
    private RoleCode role;
    private String externalId;
    private String firstname;
    private String lastname;
    private String birthdate;
    private String email;
    private char[] password;
    private Collection<String> applications = new LinkedHashSet<>();

    public static PersonBean newInstance(){
        return new PersonBean();
    }

    public long getId() {
        return id;
    }

    public PersonBean setId(long id) {
        this.id = id;
        return this;
    }


    public RoleCode getRole() {
        return role;
    }

    public PersonBean setRole(RoleCode role) {
        this.role = role;
        return this;
    }

    public String getExternalId() {
        return externalId;
    }

    public PersonBean setExternalId(String externalId) {
        this.externalId = externalId;
        return this;
    }

    public String getFirstname() {
        return firstname;
    }

    public PersonBean setFirstname(String firstname) {
        this.firstname = firstname;
        return this;
    }

    public String getLastname() {
        return lastname;
    }

    public PersonBean setLastname(String lastname) {
        this.lastname = lastname;
        return this;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public PersonBean setBirthdate(String birthdate) {
        this.birthdate = birthdate;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public PersonBean setEmail(String email) {
        this.email = email;
        return this;
    }

    public char[] getPassword() {
        return password;
    }

    public PersonBean setPassword(char[] password) {
        this.password = password;
        return this;
    }

    public Collection<String> getApplications() {
        return applications;
    }

    public PersonBean setApplications(Collection<String> applications) {
        this.applications = applications;
        return this;
    }
}
