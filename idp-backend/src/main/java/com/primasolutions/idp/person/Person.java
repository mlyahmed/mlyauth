package com.primasolutions.idp.person;

import com.google.common.collect.Sets;
import com.primasolutions.idp.domain.Application;
import com.primasolutions.idp.domain.AuthenticationInfo;
import com.primasolutions.idp.domain.EncryptedDomain;
import com.primasolutions.idp.domain.Profile;
import com.primasolutions.idp.domain.Role;
import org.hibernate.annotations.Type;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "PERSON")
public class Person  implements EncryptedDomain {

    public static final int ID_INIT_VALUE = 9999;
    public static final int ID_INC_STEP = 1;

    @Id
    @Column(name = "ID", nullable = false)
    @TableGenerator(name = "PERSON_ID", table = "IDS_SEQUENCES", pkColumnName = "SEQUENCENAME",
            pkColumnValue = "PERSON_ID", valueColumnName = "SEQUENCEVALUE",
            initialValue = ID_INIT_VALUE, allocationSize = ID_INC_STEP)
    @GeneratedValue(generator = "PERSON_ID", strategy = GenerationType.TABLE)
    private long id;

    @Column(name = "EXTERNAL_ID", nullable = false)
    private String externalId;

    @Column(name = "FIRSTNAME", nullable = false)
    @Type(type = ENCRYPTED_STRING)
    private String firstname;

    @Column(name = "LASTNAME", nullable = false)
    @Type(type = ENCRYPTED_STRING)
    private String lastname;

    @Column(name = "BIRTHDATE", nullable = false)
    @Temporal(TemporalType.DATE)
    @Type(type = ENCRYPTED_DATE)
    private java.util.Date birthdate;

    @Column(name = "EMAIL", nullable = false, unique = true)
    @Type(type = ENCRYPTED_STRING)
    private String email;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "AUTHENTICATION_INFO_ID")
    private AuthenticationInfo authenticationInfo;


    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "PERSON_APPLICATION", joinColumns = @JoinColumn(name = "PERSON_ID"),
            inverseJoinColumns = @JoinColumn(name = "APPLICATION_ID"))
    private Set<Application> applications;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "PERSON_PROFILE", joinColumns = @JoinColumn(name = "PERSON_ID"),
            inverseJoinColumns = @JoinColumn(name = "PROFILE_CODE"))
    private Set<Profile> profiles;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "ROLE")
    private Role role;

    public static Person newInstance() {
        return new Person();
    }

    public long getId() {
        return id;
    }

    public Person setId(final long id) {
        this.id = id;
        return this;
    }

    public String getExternalId() {
        return externalId;
    }

    public Person setExternalId(final String externalId) {
        this.externalId = externalId;
        return this;
    }

    public Role getRole() {
        return role;
    }

    public Person setRole(final Role role) {
        this.role = role;
        return this;
    }

    public String getFirstname() {
        return firstname;
    }

    public Person setFirstname(final String firstname) {
        this.firstname = firstname;
        return this;
    }

    public String getLastname() {
        return lastname;
    }

    public Person setLastname(final String lastname) {
        this.lastname = lastname;
        return this;
    }

    public Date getBirthdate() {
        return birthdate;
    }

    public Person setBirthdate(final Date birthdate) {
        this.birthdate = birthdate;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public Person setEmail(final String email) {
        this.email = email;
        return this;
    }

    public AuthenticationInfo getAuthenticationInfo() {
        return authenticationInfo;
    }

    public Person setAuthenticationInfo(final AuthenticationInfo authenticationInfo) {
        this.authenticationInfo = authenticationInfo;
        this.authenticationInfo.setPerson(this);
        return this;
    }

    public Set<Application> getApplications() {
        if (applications == null) applications = Sets.newHashSet();
        return applications;
    }

    public Person setApplications(final Set<Application> applications) {
        this.applications = applications;
        return this;
    }

    public Set<Profile> getProfiles() {
        return profiles;
    }

    public void setProfiles(final Set<Profile> profiles) {
        this.profiles = profiles;
    }
}
