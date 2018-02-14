package com.mlyauth.domain;

import com.mlyauth.constants.AuthenticationInfoStatus;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "AUTH_INFORMATION")
public class AuthenticationInfo {

    @Id
    @Column(name = "ID", nullable = false)
    @TableGenerator(name = "AUTH_INFORMATION_ID", table = "IDS_SEQUENCES", pkColumnName = "SEQUENCENAME", valueColumnName = "SEQUENCEVALUE", pkColumnValue = "AUTH_INFORMATION_ID", initialValue = 9999, allocationSize = 1)
    @GeneratedValue(generator = "AUTH_INFORMATION_ID", strategy = GenerationType.TABLE)
    private long id;

    @Column(name = "LOGIN", nullable = false, unique = true)
    private String login;

    @Column(name = "PASSWORD", nullable = false)
    private String password;

    @Column(name = "STATUS", nullable = false)
    @Enumerated(EnumType.STRING)
    private AuthenticationInfoStatus status;

    @Column(name = "EFFECTIVE_ON", nullable = false)
    private java.util.Date effectiveOn;

    @Column(name = "EXPIRE_ON", nullable = false)
    private java.util.Date expireOn;

    @OneToOne(mappedBy = "authenticationInfo", fetch = FetchType.EAGER)
    private Person person;


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

    public Date getEffectiveOn() {
        return effectiveOn;
    }

    public AuthenticationInfo setEffectiveOn(Date effectiveOn) {
        this.effectiveOn = effectiveOn;
        return this;
    }

    public Date getExpireOn() {
        return expireOn;
    }

    public AuthenticationInfo setExpireOn(Date expireOn) {
        this.expireOn = expireOn;
        return this;
    }

    public Person getPerson() {
        return person;
    }

    public AuthenticationInfo setPerson(Person person) {
        this.person = person;
        return this;
    }
}
