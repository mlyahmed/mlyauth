package com.mlyauth.domain;

import com.google.common.collect.Sets;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;

@Entity
@Table(name="PERSON")
public class Person  implements Serializable {

    @Id
    @Column(name = "ID", nullable = false)
    @TableGenerator(name = "PERSON_ID", table = "IDS_SEQUENCES", pkColumnName = "SEQUENCENAME", pkColumnValue = "PERSON_ID", valueColumnName = "SEQUENCEVALUE", initialValue = 9999, allocationSize=1)
    @GeneratedValue(generator = "PERSON_ID", strategy = GenerationType.TABLE)
    private long id;

    @Column(name = "EXTERNAL_ID", nullable = false)
    private String externalId;

    @Column(name = "FIRSTNAME", nullable = false)
    private String firstname;

    @Column(name = "LASTNAME", nullable = false)
    private String lastname;

    @Column(name = "BIRTHDATE", nullable = false)
    private java.util.Date birthdate;

    @Column(name = "EMAIL", nullable = false, unique = true)
    private String email;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "AUTHENTICATION_INFO_ID")
    private AuthenticationInfo authenticationInfo;


    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "PERSON_APPLICATION", joinColumns = @JoinColumn(name = "PERSON_ID"), inverseJoinColumns = @JoinColumn(name = "APPLICATION_ID"))
    private Set<Application> applications;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "PERSON_PROFILE", joinColumns = @JoinColumn(name = "PERSON_ID"), inverseJoinColumns = @JoinColumn(name = "PROFILE_CODE"))
    private Set<Profile> profiles;

    public static Person newInstance() {
        return new Person();
    }

    public long getId() {
        return id;
    }

    public Person setId(long id) {
        this.id = id;
        return this;
    }

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "ROLE_CODE")
    private Role role;

    public String getExternalId() {
        return externalId;
    }

    public Person setExternalId(String externalId) {
        this.externalId = externalId;
        return this;
    }

    public Role getRole() {
        return role;
    }

    public Person setRole(Role role) {
        this.role = role;
        return this;
    }

    public String getFirstname() {
        return firstname;
    }

    public Person setFirstname(String firstname) {
        this.firstname = firstname;
        return this;
    }

    public String getLastname() {
        return lastname;
    }

    public Person setLastname(String lastname) {
        this.lastname = lastname;
        return this;
    }

    public Date getBirthdate() {
        return birthdate;
    }

    public Person setBirthdate(Date birthdate) {
        this.birthdate = birthdate;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public Person setEmail(String email) {
        this.email = email;
        return this;
    }

    public AuthenticationInfo getAuthenticationInfo() {
        return authenticationInfo;
    }

    public Person setAuthenticationInfo(AuthenticationInfo authenticationInfo) {
        this.authenticationInfo = authenticationInfo;
        return this;
    }

    public Set<Application> getApplications() {
        if(applications == null) applications = Sets.newHashSet();
        return applications;
    }

    public Person setApplications(Set<Application> applications) {
        this.applications = applications;
        return this;
    }

    public Set<Profile> getProfiles() {
        return profiles;
    }

    public void setProfiles(Set<Profile> profiles) {
        this.profiles = profiles;
    }
}
