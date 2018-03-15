package com.mlyauth.domain;

import com.mlyauth.constants.AuthenticationInfoStatus;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "AUTHENTICATION_INFO")
public class AuthenticationInfo {

    @Id
    @Column(name = "ID", nullable = false)
    @TableGenerator(name = "AUTHENTICATION_INFO_ID", table = "IDS_SEQUENCES", pkColumnName = "SEQUENCENAME", valueColumnName = "SEQUENCEVALUE", pkColumnValue = "AUTHENTICATION_INFO_ID", initialValue = 9999, allocationSize = 1)
    @GeneratedValue(generator = "AUTHENTICATION_INFO_ID", strategy = GenerationType.TABLE)
    private long id;

    @Column(name = "LOGIN", nullable = false, unique = true)
    private String login;

    @Column(name = "PASSWORD", nullable = false)
    private String password;

    @Column(name = "STATUS", nullable = false)
    @Enumerated(EnumType.STRING)
    private AuthenticationInfoStatus status;

    @Column(name = "EFFECTIVE_AT", nullable = false)
    private java.util.Date effectiveAt;

    @Column(name = "EXPIRES_AT", nullable = false)
    private java.util.Date expireAt;

    @OneToOne(mappedBy = "authenticationInfo", fetch = FetchType.EAGER)
    private Person person;

    @OneToOne(mappedBy = "authenticationInfo", fetch = FetchType.EAGER)
    private Application application;

    public static AuthenticationInfo newInstance() {
        return new AuthenticationInfo();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public AuthenticationInfo setLogin(String login) {
        this.login = login;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public AuthenticationInfo setPassword(String password) {
        this.password = password;
        return this;
    }

    public AuthenticationInfoStatus getStatus() {
        return status;
    }

    public AuthenticationInfo setStatus(AuthenticationInfoStatus status) {
        this.status = status;
        return this;
    }

    public Date getEffectiveAt() {
        return effectiveAt;
    }

    public AuthenticationInfo setEffectiveAt(Date effectiveAt) {
        this.effectiveAt = effectiveAt;
        return this;
    }

    public Date getExpireAt() {
        return expireAt;
    }

    public AuthenticationInfo setExpireAt(Date expireAt) {
        this.expireAt = expireAt;
        return this;
    }

    public Person getPerson() {
        return person;
    }

    public AuthenticationInfo setPerson(Person person) {
        this.person = person;
        return this;
    }

    public Application getApplication() {
        return application;
    }

    public AuthenticationInfo setApplication(Application application) {
        this.application = application;
        return this;
    }

    @Transient
    public boolean isApplication() {
        return this.getApplication() != null;
    }

    @Transient
    public boolean isPerson() {
        return this.getPerson() != null;
    }
}
