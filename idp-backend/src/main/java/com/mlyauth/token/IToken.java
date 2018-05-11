package com.mlyauth.token;

import com.mlyauth.constants.TokenNorm;
import com.mlyauth.constants.TokenProcessingStatus;
import com.mlyauth.constants.TokenRefreshMode;
import com.mlyauth.constants.TokenScope;
import com.mlyauth.constants.TokenType;
import com.mlyauth.constants.TokenValidationMode;
import com.mlyauth.constants.TokenVerdict;

import java.time.LocalDateTime;
import java.util.Set;

public interface IToken {

    TokenRefreshMode getRefreshMode();

    void setRefreshMode(TokenRefreshMode mode);

    TokenValidationMode getValidationMode();

    void setValidationMode(TokenValidationMode mode);

    String getStamp();

    void setStamp(String stamp);

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

    String getTargetURL();

    void setTargetURL(String url);

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

    TokenProcessingStatus getStatus();

    void setClaim(String claimURI, String value);

    String getClaim(String claimURI);

    void cypher();

    void decipher();

    String serialize();
}
