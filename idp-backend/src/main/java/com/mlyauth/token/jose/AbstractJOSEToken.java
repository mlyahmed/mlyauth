package com.mlyauth.token.jose;

import com.mlyauth.constants.TokenNorm;
import com.mlyauth.constants.TokenScope;
import com.mlyauth.constants.TokenStatus;
import com.mlyauth.exception.JOSEErrorException;
import com.mlyauth.exception.TokenNotCipheredException;
import com.mlyauth.token.AbstractToken;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.lang.StringUtils;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Collections;
import java.util.Set;

import static com.mlyauth.constants.TokenStatus.CYPHERED;
import static com.mlyauth.token.Claims.*;
import static com.nimbusds.jose.EncryptionMethod.A128GCM;
import static com.nimbusds.jose.JWEAlgorithm.RSA_OAEP_256;
import static com.nimbusds.jose.JWSAlgorithm.RS256;

public abstract class AbstractJOSEToken extends AbstractToken {

    protected TokenStatus status = TokenStatus.FRESH;

    protected PrivateKey privateKey;
    protected PublicKey publicKey;

    protected JWEObject token;
    protected JWTClaimsSet.Builder builder;

    @Override
    public String getStamp() {
        return builder.build().getJWTID();
    }

    @Override
    public void setStamp(String stamp) {
        checkUnmodifiable();
        builder = builder.jwtID(stamp);
        status = TokenStatus.FORGED;
    }

    @Override
    public TokenStatus getStatus() {
        return status;
    }

    @Override
    public TokenNorm getNorm() {
        return TokenNorm.JOSE;
    }

    @Override
    public void cypher() {
        try {
            signAndEncrypt();
            status = CYPHERED;
            committed = true;
        } catch (Exception e) {
            throw JOSEErrorException.newInstance(e);
        }
    }

    private void signAndEncrypt() throws JOSEException {
        SignedJWT tokenSigned = new SignedJWT(buildJWSHeader(), builder.build());
        tokenSigned.sign(new RSASSASigner(privateKey));
        token = new JWEObject(new JWEHeader.Builder(RSA_OAEP_256, A128GCM).keyID(getAudience()).build(), new Payload(tokenSigned));
        token.encrypt(new RSAEncrypter((RSAPublicKey) publicKey));
    }

    private JWSHeader buildJWSHeader() {
        JWSHeader.Builder headerBuilder = new JWSHeader.Builder(RS256).customParam(ISSUER.getValue(), getIssuer());
        return headerBuilder.build();
    }

    @Override
    public String serialize() {
        if (getStatus() != CYPHERED)
            throw TokenNotCipheredException.newInstance();
        return token.serialize();
    }

    @Override
    public String getSubject() {
        return builder.build().getSubject();
    }

    @Override
    public void setSubject(String subject) {
        checkUnmodifiable();
        builder = builder.subject(subject);
        status = TokenStatus.FORGED;
    }

    @Override
    public Set<TokenScope> getScopes() {
        final String scopes = (String) builder.build().getClaim(SCOPES.getValue());
        if (StringUtils.isBlank(scopes)) return Collections.emptySet();
        return splitScopes(scopes);
    }

    @Override
    public void setScopes(Set<TokenScope> scopes) {
        checkUnmodifiable();
        builder = builder.claim(SCOPES.getValue(), compactScopes(scopes));
        status = TokenStatus.FORGED;
    }

    @Override
    public String getBP() {
        return (String) builder.build().getClaim(BP.getValue());
    }

    @Override
    public void setBP(String bp) {
        checkUnmodifiable();
        builder = builder.claim(BP.getValue(), bp);
        status = TokenStatus.FORGED;
    }

    @Override
    public String getState() {
        return (String) builder.build().getClaim(STATE.getValue());
    }

    @Override
    public void setState(String state) {
        checkUnmodifiable();
        builder = builder.claim(STATE.getValue(), state);
        status = TokenStatus.FORGED;
    }

    @Override
    public String getIssuer() {
        return builder.build().getIssuer();
    }

    @Override
    public void setIssuer(String issuerURI) {
        checkUnmodifiable();
        builder = builder.issuer(issuerURI);
        status = TokenStatus.FORGED;
    }

    @Override
    public String getAudience() {
        return builder.build().getAudience().stream().findFirst().orElse(null);
    }

    @Override
    public void setAudience(String audienceURI) {
        checkUnmodifiable();
        builder = builder.audience(audienceURI);
        status = TokenStatus.FORGED;
    }
}
