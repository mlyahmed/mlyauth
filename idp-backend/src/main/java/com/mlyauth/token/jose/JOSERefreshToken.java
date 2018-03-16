package com.mlyauth.token.jose;

import com.mlyauth.constants.TokenScope;
import com.mlyauth.constants.TokenType;
import com.mlyauth.constants.TokenVerdict;
import com.nimbusds.jwt.JWTClaimsSet;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;

import static org.springframework.util.Assert.notNull;

public class JOSERefreshToken extends AbstractJOSEToken {

    public JOSERefreshToken(PrivateKey privateKey, PublicKey publicKey) {
        notNull(privateKey, "The private key is mandatory");
        notNull(publicKey, "The public key is mandatory");

        builder = new JWTClaimsSet.Builder();
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
    public TokenType getType() {
        return TokenType.REFRESH;
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