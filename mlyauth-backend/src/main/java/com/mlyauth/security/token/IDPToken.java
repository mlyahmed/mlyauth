package com.mlyauth.security.token;

import com.mlyauth.constants.*;
import com.mlyauth.domain.Application;

import java.time.LocalDateTime;
import java.util.Set;

public interface IDPToken<N> {

    String getId();

    void setId(String id);

    String getSubject();

    void setSubject(String subject);

    Set<TokenScope> getScopes();

    void setScopes(Set<TokenScope> scopes);

    String getBP();

    void setBP(String bp);

    String getState();

    void setState(String state);

    String getIssuer();

    void setIssuer(String issuerURI);

    String getAudience();

    void setAudience(String audienceURI);

    String getDelegator();

    void setDelegator(String delegatorID);

    String getDelegate();

    void setDelegate(String delegateURI);

    TokenVerdict getVerdict();

    void setVerdict(TokenVerdict verdict);

    LocalDateTime getExpiryTime();

    LocalDateTime getEffectiveTime();

    LocalDateTime getIssuanceTime();

    TokenNorm getNorm();

    TokenType getType();

    TokenStatus getStatus();

    N getNative();

    Application getApplication();

    void cypher();

    void decipher();

    String serialize();
}
