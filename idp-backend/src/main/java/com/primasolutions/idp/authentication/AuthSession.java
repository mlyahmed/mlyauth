package com.primasolutions.idp.authentication;

import com.primasolutions.idp.constants.AuthenticationSessionStatus;
import com.primasolutions.idp.exception.IDPException;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "AUTHENTICATION_SESSION")
public class AuthSession implements Cloneable, Serializable {

    public static final int ID_INIT_VALUE = 9999;
    public static final int ID_INC_STEP = 1;

    @Id
    @Column(name = "ID", nullable = false)
    @TableGenerator(name = "AUTHENTICATION_SESSION_ID", table = "IDS_SEQUENCES", pkColumnName = "SEQUENCENAME",
            valueColumnName = "SEQUENCEVALUE", pkColumnValue = "AUTHENTICATION_SESSION_ID",
            initialValue = ID_INIT_VALUE, allocationSize = ID_INC_STEP)
    @GeneratedValue(generator = "AUTHENTICATION_SESSION_ID", strategy = GenerationType.TABLE)
    private long id;

    @Column(name = "CREATED_AT", nullable = false)
    private java.util.Date createdAt;

    @Column(name = "STATUS", nullable = false)
    @Enumerated(EnumType.STRING)
    private AuthenticationSessionStatus status;

    @Column(name = "CONTEXT_ID", nullable = false)
    private String contextId;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "AUTHENTICATION_INFO_ID", nullable = false, updatable = false)
    private AuthInfo authenticationInfo;

    @Column(name = "CLOSED_AT")
    private java.util.Date closedAt;

    public static AuthSession newInstance() {
        return new AuthSession();
    }

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public AuthSession setCreatedAt(final Date createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public AuthenticationSessionStatus getStatus() {
        return status;
    }

    public AuthSession setStatus(final AuthenticationSessionStatus status) {
        this.status = status;
        return this;
    }

    public String getContextId() {
        return contextId;
    }

    public AuthSession setContextId(final String contextId) {
        this.contextId = contextId;
        return this;
    }

    public AuthInfo getAuthenticationInfo() {
        return authenticationInfo;
    }

    public AuthSession setAuthenticationInfo(final AuthInfo authenticationInfo) {
        this.authenticationInfo = authenticationInfo;
        return this;
    }

    public Date getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(final Date closedAt) {
        this.closedAt = closedAt;
    }

    public AuthSession clone() {
        try {
            return (AuthSession) super.clone();
        } catch (CloneNotSupportedException e) {
            throw IDPException.newInstance(e);
        }
    }
}
