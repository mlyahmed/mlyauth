package com.mlyauth.security.token.jwt;

import com.mlyauth.constants.*;
import com.mlyauth.exception.JOSEErrorException;
import com.mlyauth.exception.TokenAlreadyCommitedException;
import com.mlyauth.security.token.IDPToken;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.util.Assert;

import java.security.PrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;

import static com.nimbusds.jose.EncryptionMethod.A128GCM;
import static com.nimbusds.jose.JWEAlgorithm.RSA_OAEP_256;
import static com.nimbusds.jose.JWSAlgorithm.RS256;

public class JOSEAccessToken implements IDPToken {

    private TokenStatus status = TokenStatus.FRESH;
    private boolean committed = false;

    private final PrivateKey privateKey;
    private final RSAPublicKey publicKey;

    private JWEObject token;
    private JWTClaimsSet.Builder builder;

    public JOSEAccessToken(PrivateKey privateKey, RSAPublicKey publicKey) {
        Assert.notNull(privateKey, "The private key is mandatory");
        Assert.notNull(publicKey, "The public key is mandatory");
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        builder = new JWTClaimsSet.Builder();
    }

    @Override
    public String getId() {
        return builder.build().getJWTID();
    }

    @Override
    public void setId(String id) {
        builder = builder.jwtID(id);
        status = TokenStatus.FORGED;
    }

    @Override
    public String getSubject() {
        return builder.build().getSubject();
    }

    @Override
    public void setSubject(String subject) {
        builder = builder.subject(subject);
        status = TokenStatus.FORGED;
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
        try {
            SignedJWT tokenSigned = new SignedJWT(new JWSHeader(RS256), builder.build());
            tokenSigned.sign(new RSASSASigner(privateKey));
            final JWEHeader header = new JWEHeader.Builder(RSA_OAEP_256, A128GCM).build();
            token = new JWEObject(header, new Payload(tokenSigned));
            token.encrypt(new RSAEncrypter(publicKey));
            status = TokenStatus.CYPHERED;
            committed = true;
        } catch (Exception e) {
            throw JOSEErrorException.newInstance(e);
        }
    }

    @Override
    public void decipher() {
        if (committed)
            throw TokenAlreadyCommitedException.newInstance();
    }

    @Override
    public String serialize() {
        return token.serialize();
    }
}
