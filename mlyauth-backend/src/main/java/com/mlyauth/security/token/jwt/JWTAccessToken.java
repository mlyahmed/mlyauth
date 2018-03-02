package com.mlyauth.security.token.jwt;

import com.mlyauth.constants.*;
import com.mlyauth.security.token.IDPToken;
import org.springframework.util.Assert;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;

public class JWTAccessToken implements IDPToken {

    private TokenStatus status = TokenStatus.FRESH;

    public JWTAccessToken(PrivateKey privateKey, PublicKey publicKey) {
        Assert.notNull(privateKey, "The private key is mandatory");
        Assert.notNull(publicKey, "The public key is mandatory");
    }

    private String id;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
        status = TokenStatus.FORGED;
    }

    @Override
    public String getSubject() {
        return null;
    }

    @Override
    public void setSubject(String subject) {

    }

    @Override
    public Set<TokenScope> getScopes() {
        return Collections.emptySet();
    }

    @Override
    public void setScopes(Set<TokenScope> scopes) {

    }

    @Override
    public String getBP() {
        return null;
    }

    @Override
    public void setBP(String bp) {

    }

    @Override
    public String getState() {
        return null;
    }

    @Override
    public void setState(String state) {

    }

    @Override
    public String getIssuer() {
        return null;
    }

    @Override
    public void setIssuer(String issuerURI) {

    }

    @Override
    public String getAudience() {
        return null;
    }

    @Override
    public void setAudience(String audienceURI) {

    }

    @Override
    public String getTargetURL() {
        return null;
    }

    @Override
    public void setTargetURL(String url) {

    }

    @Override
    public String getDelegator() {
        return null;
    }

    @Override
    public void setDelegator(String delegatorID) {

    }

    @Override
    public String getDelegate() {
        return null;
    }

    @Override
    public void setDelegate(String delegateURI) {

    }

    @Override
    public TokenVerdict getVerdict() {
        return null;
    }

    @Override
    public void setVerdict(TokenVerdict verdict) {

    }

    @Override
    public LocalDateTime getExpiryTime() {
        return null;
    }

    @Override
    public LocalDateTime getEffectiveTime() {
        return null;
    }

    @Override
    public LocalDateTime getIssuanceTime() {
        return null;
    }

    @Override
    public TokenNorm getNorm() {
        return TokenNorm.JWT;
    }

    @Override
    public TokenType getType() {
        return TokenType.ACCESS;
    }

    @Override
    public TokenStatus getStatus() {
        return status;
    }

    @Override
    public void setClaim(String claimURI, String value) {

    }

    @Override
    public String getClaim(String claimURI) {
        return null;
    }

    @Override
    public void cypher() {

    }

    @Override
    public void decipher() {

    }

    @Override
    public String serialize() {
        return null;
    }
}
