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

    public static PersonBean newInstance(){
        return new PersonBean();
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

    public String getEmail() {
        return email;
    }

    public PersonBean setEmail(String email) {
        this.email = email;
        return this;
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

    public PersonBean setApplications(Collection<String> applications) {
        this.applications = applications;
        return this;
    }

    public long getId() {
        return id;
    }

    public PersonBean setId(long id) {
        this.id = id;
        return this;
    }
}
