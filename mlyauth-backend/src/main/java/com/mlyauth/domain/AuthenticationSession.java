package com.mlyauth.domain;

import com.mlyauth.constants.AuthenticationSessionStatus;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "AUTHENTICATION_SESSION")
public class AuthenticationSession {

    @Id
    @Column(name = "ID", nullable = false)
    @TableGenerator(name = "AUTHENTICATION_SESSION_ID", table = "IDS_SEQUENCES", pkColumnName = "SEQUENCENAME", valueColumnName = "SEQUENCEVALUE", pkColumnValue = "AUTHENTICATION_SESSION_ID", initialValue = 9999, allocationSize = 1)
    @GeneratedValue(generator = "AUTHENTICATION_SESSION_ID", strategy = GenerationType.TABLE)
    private long id;

    @Column(name = "CREATED_AT", nullable = false)
    private java.util.Date createdAt;

    @Column(name = "STATUS", nullable = false)
    @Enumerated(EnumType.STRING)
    private AuthenticationSessionStatus status;

    @Column(name = "CONTEXT_ID", nullable = false)
    private String contextId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "AUTHENTICATION_INFO_ID", nullable = false, updatable = false)
    private AuthenticationInfo authenticationInfo;

    @Column(name = "CLOSED_AT")
    private java.util.Date closedAt;

    public static AuthenticationSession newInstance() {
        return new AuthenticationSession();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public AuthenticationSession setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public AuthenticationSessionStatus getStatus() {
        return status;
    }

    public AuthenticationSession setStatus(AuthenticationSessionStatus status) {
        this.status = status;
        return this;
    }

    public String getContextId() {
        return contextId;
    }

    public AuthenticationSession setContextId(String contextId) {
        this.contextId = contextId;
        return this;
    }

    public AuthenticationInfo getAuthenticationInfo() {
        return authenticationInfo;
    }

    public AuthenticationSession setAuthenticationInfo(AuthenticationInfo authenticationInfo) {
        this.authenticationInfo = authenticationInfo;
        return this;
    }

    public Date getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(Date closedAt) {
        this.closedAt = closedAt;
    }
}
