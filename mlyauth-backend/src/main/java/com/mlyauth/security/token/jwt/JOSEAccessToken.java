package com.mlyauth.security.token.jwt;

import com.mlyauth.constants.*;
import com.mlyauth.exception.JOSEErrorException;
import com.mlyauth.exception.TokenNotCipheredException;
import com.mlyauth.security.token.AbstractToken;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;

import java.security.PrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

import static com.mlyauth.constants.TokenStatus.CYPHERED;
import static com.mlyauth.constants.TokenStatus.DECIPHERED;
import static com.mlyauth.security.token.ExtraClaims.*;
import static com.nimbusds.jose.EncryptionMethod.A128GCM;
import static com.nimbusds.jose.JWEAlgorithm.RSA_OAEP_256;
import static com.nimbusds.jose.JWSAlgorithm.RS256;

public class JOSEAccessToken extends AbstractToken {

    private TokenStatus status = TokenStatus.FRESH;

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
        initTimes();
    }

    private void initTimes() {
        Instant threeMinutesAfter = LocalDateTime.now().plusSeconds(179).atZone(ZoneId.systemDefault()).toInstant();
        Instant aSecondAgo = LocalDateTime.now().plusSeconds(1).atZone(ZoneId.systemDefault()).toInstant();
        builder = builder.expirationTime(Date.from(threeMinutesAfter))
                .notBeforeTime(Date.from(aSecondAgo))
                .issueTime(Date.from(aSecondAgo));
    }

    public JOSEAccessToken(String serialize, PrivateKey privateKey, RSAPublicKey publicKey) {
        Assert.notNull(serialize, "The cyphered token is mandatory");
        Assert.notNull(privateKey, "The private key is mandatory");
        Assert.notNull(publicKey, "The public key is mandatory");
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        parseCipheredToken(serialize);
        status = TokenStatus.CYPHERED;
    }

    private void parseCipheredToken(String serialize) {
        try {
            token = JWEObject.parse(serialize);
        } catch (ParseException e) {
            throw JOSEErrorException.newInstance(e);
        }
    }

    @Override
    public String getId() {
        return builder.build().getJWTID();
    }

    @Override
    public void setId(String id) {
        checkCommitted();
        builder = builder.jwtID(id);
        status = TokenStatus.FORGED;
    }

    @Override
    public String getSubject() {
        return builder.build().getSubject();
    }

    @Override
    public void setSubject(String subject) {
        checkCommitted();
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
        checkCommitted();
        builder = builder.claim(SCOPES.getValue(), compactScopes(scopes));
        status = TokenStatus.FORGED;
    }

    @Override
    public String getBP() {
        return (String) builder.build().getClaim(BP.getValue());
    }

    @Override
    public void setBP(String bp) {
        checkCommitted();
        builder = builder.claim(BP.getValue(), bp);
        status = TokenStatus.FORGED;
    }

    @Override
    public String getState() {
        return (String) builder.build().getClaim(STATE.getValue());
    }

    @Override
    public void setState(String state) {
        checkCommitted();
        builder = builder.claim(STATE.getValue(), state);
        status = TokenStatus.FORGED;
    }

    @Override
    public String getIssuer() {
        return builder.build().getIssuer();
    }

    @Override
    public void setIssuer(String issuerURI) {
        checkCommitted();
        builder = builder.issuer(issuerURI);
        status = TokenStatus.FORGED;
    }

    @Override
    public String getAudience() {
        return builder.build().getAudience().stream().findFirst().orElse(null);
    }

    @Override
    public void setAudience(String audienceURI) {
        checkCommitted();
        builder = builder.audience(audienceURI);
        status = TokenStatus.FORGED;
    }

    @Override
    public String getTargetURL() {
        return (String) builder.build().getClaim(TARGET_URL.getValue());
    }

    @Override
    public void setTargetURL(String url) {
        checkCommitted();
        builder = builder.claim(TARGET_URL.getValue(), url);
        status = TokenStatus.FORGED;
    }

    @Override
    public String getDelegator() {
        return (String) builder.build().getClaim(DELEGATOR.getValue());
    }

    @Override
    public void setDelegator(String delegatorID) {
        checkCommitted();
        builder = builder.claim(DELEGATOR.getValue(), delegatorID);
        status = TokenStatus.FORGED;
    }

    @Override
    public String getDelegate() {
        return (String) builder.build().getClaim(DELEGATE.getValue());
    }

    @Override
    public void setDelegate(String delegateURI) {
        checkCommitted();
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
        checkCommitted();
        builder = builder.claim(VERDICT.getValue(), verdict.name());
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
        checkCommitted();
        builder = builder.claim(claimURI, value);
        status = TokenStatus.FORGED;
    }

    @Override
    public String getClaim(String claimURI) {
        return (String) builder.build().getClaim(claimURI);
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
        SignedJWT tokenSigned = new SignedJWT(new JWSHeader(RS256), builder.build());
        tokenSigned.sign(new RSASSASigner(privateKey));
        final JWEHeader header = new JWEHeader.Builder(RSA_OAEP_256, A128GCM).build();
        token = new JWEObject(header, new Payload(tokenSigned));
        token.encrypt(new RSAEncrypter(publicKey));
    }

    @Override
    public void decipher() {
        checkCommitted();
        try {
            builder = new JWTClaimsSet.Builder(loadCipheredClaims().getJWTClaimsSet());
            status = DECIPHERED;
        } catch (JOSEException | ParseException e) {
            throw JOSEErrorException.newInstance(e);
        }
    }

    private SignedJWT loadCipheredClaims() throws JOSEException {
        token.decrypt(new RSADecrypter(privateKey));
        final SignedJWT signedJWT = token.getPayload().toSignedJWT();
        checkSignature(signedJWT);
        return signedJWT;
    }

    private void checkSignature(SignedJWT signedJWT) throws JOSEException {
        if (signedJWT == null || !signedJWT.verify(new RSASSAVerifier(publicKey)))
            throw JOSEErrorException.newInstance(new JOSEException("Failed to verify signature"));
    }

    @Override
    public String serialize() {
        if (status != CYPHERED)
            throw TokenNotCipheredException.newInstance();
        return token.serialize();
    }

}
