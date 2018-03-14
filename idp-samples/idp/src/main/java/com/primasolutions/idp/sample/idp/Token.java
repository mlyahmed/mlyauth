package com.primasolutions.idp.sample.idp;

public class Token {

    private String id;
    private String subject;
    private String scopes;
    private String bp;
    private String state;
    private String issuer;
    private String audience;
    private String targetURL;
    private String delegator;
    private String delegate;
    private String verdict;
    private String clientId;
    private String clientProfile;
    private String entityId;
    private String action;
    private String application;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getScopes() {
        return scopes;
    }

    public void setScopes(String scopes) {
        this.scopes = scopes;
    }

    public String getBp() {
        return bp;
    }

    public void setBp(String bp) {
        this.bp = bp;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public String getTargetURL() {
        return targetURL;
    }

    public void setTargetURL(String targetURL) {
        this.targetURL = targetURL;
    }

    public String getDelegator() {
        return delegator;
    }

    public void setDelegator(String delegator) {
        this.delegator = delegator;
    }

    public String getDelegate() {
        return delegate;
    }

    public void setDelegate(String delegate) {
        this.delegate = delegate;
    }

    public String getVerdict() {
        return verdict;
    }

    public void setVerdict(String verdict) {
        this.verdict = verdict;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientProfile() {
        return clientProfile;
    }

    public void setClientProfile(String clientProfile) {
        this.clientProfile = clientProfile;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }
}
