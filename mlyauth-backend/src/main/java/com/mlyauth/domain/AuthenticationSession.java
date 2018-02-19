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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public AuthenticationSessionStatus getStatus() {
        return status;
    }

    public void setStatus(AuthenticationSessionStatus status) {
        this.status = status;
    }

    public String getContextId() {
        return contextId;
    }

    public void setContextId(String contextId) {
        this.contextId = contextId;
    }

    public AuthenticationInfo getAuthenticationInfo() {
        return authenticationInfo;
    }

    public void setAuthenticationInfo(AuthenticationInfo authenticationInfo) {
        this.authenticationInfo = authenticationInfo;
    }

    public Date getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(Date closedAt) {
        this.closedAt = closedAt;
    }
}
