package com.primasolutions.idp.authentication;

import com.primasolutions.idp.application.Application;
import com.primasolutions.idp.constants.AuthenticationInfoStatus;
import com.primasolutions.idp.person.Person;
import com.primasolutions.idp.security.sensitive.domain.EncryptedDomain;
import org.hibernate.annotations.Type;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;
import java.util.Date;

@Entity
@Table(name = "AUTHENTICATION_INFO")
public class AuthenticationInfo implements EncryptedDomain {

    private static final int ID_INIT_VALUE = 9999;
    private static final int ID_INC_STEP = 1;

    @Id
    @Column(name = "ID", nullable = false)
    @TableGenerator(name = "AUTHENTICATION_INFO_ID", table = "IDS_SEQUENCES", pkColumnName = "SEQUENCENAME",
            valueColumnName = "SEQUENCEVALUE", pkColumnValue = "AUTHENTICATION_INFO_ID",
            initialValue = ID_INIT_VALUE, allocationSize = ID_INC_STEP)
    @GeneratedValue(generator = "AUTHENTICATION_INFO_ID", strategy = GenerationType.TABLE)
    private long id;

    @Column(name = "LOGIN", nullable = false, unique = true)
    @Type(type = ENCRYPTED_STRING)
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

    @OneToOne(mappedBy = "authenticationInfo", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Person person;

    @OneToOne(mappedBy = "authenticationInfo", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Application application;

    public static AuthenticationInfo newInstance() {
        return new AuthenticationInfo();
    }

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public AuthenticationInfo setLogin(final String login) {
        this.login = login;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public AuthenticationInfo setPassword(final String password) {
        this.password = password;
        return this;
    }

    public AuthenticationInfoStatus getStatus() {
        return status;
    }

    public AuthenticationInfo setStatus(final AuthenticationInfoStatus status) {
        this.status = status;
        return this;
    }

    public Date getEffectiveAt() {
        return effectiveAt;
    }

    public AuthenticationInfo setEffectiveAt(final Date effectiveAt) {
        this.effectiveAt = effectiveAt;
        return this;
    }

    public Date getExpireAt() {
        return expireAt;
    }

    public AuthenticationInfo setExpireAt(final Date expireAt) {
        this.expireAt = expireAt;
        return this;
    }

    public Person getPerson() {
        return person;
    }

    public AuthenticationInfo setPerson(final Person person) {
        this.person = person;
        return this;
    }

    public Application getApplication() {
        return application;
    }

    public AuthenticationInfo setApplication(final Application application) {
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
