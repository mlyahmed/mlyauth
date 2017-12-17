package com.mlyauth.beans;

import java.util.Collection;
import java.util.LinkedHashSet;

public class PersonBean {

    private long id;
    private String firstname;
    private String lastname;
    private String email;
    private String username;
    private char[] password;
    private Collection<String> applications = new LinkedHashSet<>();

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public char[] getPassword() {
        return password;
    }

    public void setPassword(char[] password) {
        this.password = password;
    }

    public Collection<String> getApplications() {
        return applications;
    }

    public void setApplications(Collection<String> applications) {
        this.applications = applications;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
