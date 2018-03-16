package com.mlyauth.token.jose;

import com.mlyauth.constants.TokenScope;
import com.mlyauth.constants.TokenStatus;
import com.mlyauth.constants.TokenType;
import com.mlyauth.constants.TokenVerdict;
import com.mlyauth.exception.InvalidTokenException;
import com.mlyauth.exception.JOSEErrorException;
import com.mlyauth.token.Claims;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.lang.StringUtils;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

import static com.mlyauth.constants.TokenStatus.DECIPHERED;
import static com.mlyauth.token.Claims.*;
import static org.springframework.util.Assert.notNull;

public class JOSEAccessToken extends AbstractJOSEToken {



    public JOSEAccessToken(PrivateKey privateKey, PublicKey publicKey) {
        notNull(privateKey, "The private key is mandatory");
        notNull(publicKey, "The public key is mandatory");
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        builder = new JWTClaimsSet.Builder();
        initTimes();
    }

    private void initTimes() {
        Instant threeMinutesAfter = LocalDateTime.now().plusSeconds(179).atZone(ZoneId.systemDefault()).toInstant();
        Instant aSecondAgo = LocalDateTime.now().minusSeconds(1).atZone(ZoneId.systemDefault()).toInstant();
        builder = builder.expirationTime(Date.from(threeMinutesAfter))
                .notBeforeTime(Date.from(aSecondAgo))
                .issueTime(Date.from(aSecondAgo));
    }

    public JOSEAccessToken(String serialize, PrivateKey privateKey, PublicKey publicKey) {
        notNull(serialize, "The cyphered token is mandatory");
        notNull(privateKey, "The private key is mandatory");
        notNull(publicKey, "The public key is mandatory");
        parseCipheredToken(serialize);
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        status = TokenStatus.CYPHERED;
        locked = true;
    }

    private void parseCipheredToken(String serialize) {
        try {
            token = JWEObject.parse(serialize);
        } catch (ParseException e) {
            throw JOSEErrorException.newInstance(e);
        }
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

    @Override
    public String getTargetURL() {
        return (String) builder.build().getClaim(TARGET_URL.getValue());
    }

    @Override
    public void setTargetURL(String url) {
        checkUnmodifiable();
        builder = builder.claim(TARGET_URL.getValue(), url);
        status = TokenStatus.FORGED;
    }

    @Override
    public String getDelegator() {
        return (String) builder.build().getClaim(DELEGATOR.getValue());
    }

    @Override
    public void setDelegator(String delegatorID) {
        checkUnmodifiable();
        builder = builder.claim(DELEGATOR.getValue(), delegatorID);
        status = TokenStatus.FORGED;
    }

    @Override
    public String getDelegate() {
        return (String) builder.build().getClaim(DELEGATE.getValue());
    }

    @Override
    public void setDelegate(String delegateURI) {
        checkUnmodifiable();
        builder = builder.claim(DELEGATE.getValue(), delegateURI);
        status = TokenStatus.FORGED;
    }

    @Override
    public TokenVerdict getVerdict() {
        return builder.build().getClaim(VERDICT.getValue()) != null
                ? TokenVerdict.valueOf((String) builder.build().getClaim(VERDICT.getValue()))
                : null;
    }

    @Override
    public void setVerdict(TokenVerdict verdict) {
        checkUnmodifiable();
        builder = builder.claim(VERDICT.getValue(), verdict != null ? verdict.name() : null);
        status = TokenStatus.FORGED;
    }

    @Override
    public LocalDateTime getExpiryTime() {
        return LocalDateTime.ofInstant(builder.build().getExpirationTime().toInstant(), ZoneId.systemDefault());
    }

    @Override
    public LocalDateTime getEffectiveTime() {
        return LocalDateTime.ofInstant(builder.build().getNotBeforeTime().toInstant(), ZoneId.systemDefault());
    }

    @Override
    public LocalDateTime getIssuanceTime() {
        return LocalDateTime.ofInstant(builder.build().getIssueTime().toInstant(), ZoneId.systemDefault());
    }

    @Override
    public TokenType getType() {
        return TokenType.ACCESS;
    }

    @Override
    public void setClaim(String claimURI, String value) {
        checkCommitted();
        builder = builder.claim(claimURI, value);
        status = TokenStatus.FORGED;
    }

    @Override
    public String getClaim(String claimURI) {
        return (String) builder.build().getClaim(claimURI);
    }

    @Override
    public void decipher() {
        checkCommitted();
        try {
            builder = new JWTClaimsSet.Builder(decipherClaims().getJWTClaimsSet());
            status = DECIPHERED;
        } catch (JOSEException | ParseException e) {
            throw JOSEErrorException.newInstance(e);
        }
    }

    private SignedJWT decipherClaims() throws JOSEException, ParseException {
        token.decrypt(new RSADecrypter(privateKey));
        final SignedJWT signedJWT = token.getPayload().toSignedJWT();
        checkSignature(signedJWT);
        checkIssuerMatch(signedJWT);
        return signedJWT;
    }

    private void checkIssuerMatch(SignedJWT signedJWT) throws ParseException {
        if (!String.valueOf(signedJWT.getHeader().getCustomParam(Claims.ISSUER.getValue())).equals(signedJWT.getJWTClaimsSet().getIssuer()))
            throw InvalidTokenException.newInstance("Issuer mismatch");
    }

    private void checkSignature(SignedJWT signedJWT) throws JOSEException {
        if (signedJWT == null || !signedJWT.verify(new RSASSAVerifier((RSAPublicKey) publicKey)))
            throw JOSEErrorException.newInstance(new JOSEException("Failed to verify signature"));
    }

}
