package com.hohou.federation.idp.token.jose;

import com.hohou.federation.idp.constants.TokenNorm;
import com.hohou.federation.idp.constants.TokenProcessingStatus;
import com.hohou.federation.idp.constants.TokenRefreshMode;
import com.hohou.federation.idp.constants.TokenScope;
import com.hohou.federation.idp.constants.TokenValidationMode;
import com.hohou.federation.idp.constants.TokenVerdict;
import com.hohou.federation.idp.exception.InvalidTokenExc;
import com.hohou.federation.idp.exception.JOSEErrorExc;
import com.hohou.federation.idp.exception.TokenNotCipheredExc;
import com.hohou.federation.idp.token.AbstractToken;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.lang.StringUtils;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Set;

import static com.hohou.federation.idp.token.Claims.BP;
import static com.hohou.federation.idp.token.Claims.DELEGATE;
import static com.hohou.federation.idp.token.Claims.DELEGATOR;
import static com.hohou.federation.idp.token.Claims.ISSUER;
import static com.hohou.federation.idp.token.Claims.REFRESH_MODE;
import static com.hohou.federation.idp.token.Claims.SCOPES;
import static com.hohou.federation.idp.token.Claims.STATE;
import static com.hohou.federation.idp.token.Claims.TARGET_URL;
import static com.hohou.federation.idp.token.Claims.VALIDATION_MODE;
import static com.hohou.federation.idp.token.Claims.VERDICT;
import static com.nimbusds.jose.EncryptionMethod.A128GCM;
import static com.nimbusds.jose.JWEAlgorithm.RSA_OAEP_256;
import static com.nimbusds.jose.JWSAlgorithm.RS256;
import static org.springframework.util.Assert.notNull;

public abstract class AbstractJOSEToken extends AbstractToken {

    protected TokenProcessingStatus status = TokenProcessingStatus.FRESH;

    protected PrivateKey privateKey;
    protected PublicKey publicKey;

    protected JWEObject token;
    protected JWTClaimsSet.Builder builder;

    public AbstractJOSEToken(final PrivateKey privateKey, final PublicKey publicKey) {
        notNull(privateKey, "The private key is mandatory");
        notNull(publicKey, "The public key is mandatory");
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        builder = new JWTClaimsSet.Builder();
    }

    public AbstractJOSEToken(final String serialize, final PrivateKey privateKey, final PublicKey publicKey) {
        notNull(serialize, "The cyphered token is mandatory");
        notNull(privateKey, "The private key is mandatory");
        notNull(publicKey, "The public key is mandatory");
        parseCipheredToken(serialize);
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        status = TokenProcessingStatus.CYPHERED;
        locked = true;
    }

    private void parseCipheredToken(final String serialize) {
        try {
            token = JWEObject.parse(serialize);
        } catch (ParseException e) {
            throw JOSEErrorExc.newInstance(e);
        }
    }

    @Override
    public String getStamp() {
        return builder.build().getJWTID();
    }

    @Override
    public void setStamp(final String stamp) {
        checkUnmodifiable();
        builder = builder.jwtID(stamp);
        status = TokenProcessingStatus.FORGED;
    }

    @Override
    public TokenProcessingStatus getStatus() {
        return status;
    }

    @Override
    public TokenNorm getNorm() {
        return TokenNorm.JOSE;
    }

    @Override
    public TokenRefreshMode getRefreshMode() {
        if (builder.build().getClaim(REFRESH_MODE.getValue()) == null) return null;
        return TokenRefreshMode.valueOf((String) builder.build().getClaim(REFRESH_MODE.getValue()));
    }

    @Override
    public void setRefreshMode(final TokenRefreshMode mode) {
        notNull(mode, "Refresh Mode is null.");
        checkUnmodifiable();
        builder = builder.claim(REFRESH_MODE.getValue(), mode.name());
        status = TokenProcessingStatus.FORGED;
    }

    @Override
    public TokenValidationMode getValidationMode() {
        if (builder.build().getClaim(VALIDATION_MODE.getValue()) == null) return null;
        return TokenValidationMode.valueOf((String) builder.build().getClaim(VALIDATION_MODE.getValue()));
    }

    @Override
    public void setValidationMode(final TokenValidationMode mode) {
        notNull(mode, "Refresh Mode is null.");
        checkUnmodifiable();
        builder = builder.claim(VALIDATION_MODE.getValue(), mode.name());
        status = TokenProcessingStatus.FORGED;
    }

    @Override
    public void cypher() {
        try {
            signAndEncrypt();
            status = TokenProcessingStatus.CYPHERED;
            committed = true;
        } catch (Exception e) {
            throw JOSEErrorExc.newInstance(e);
        }
    }

    private void signAndEncrypt() throws JOSEException {
        SignedJWT tokenSigned = new SignedJWT(buildJWSHeader(), builder.build());
        tokenSigned.sign(new RSASSASigner(privateKey));
        token = new JWEObject(new JWEHeader.Builder(RSA_OAEP_256, A128GCM).keyID(getAudience()).build(),
                new Payload(tokenSigned)
        );
        token.encrypt(new RSAEncrypter((RSAPublicKey) publicKey));
    }

    private JWSHeader buildJWSHeader() {
        JWSHeader.Builder headerBuilder = new JWSHeader.Builder(RS256).customParam(ISSUER.getValue(), getIssuer());
        return headerBuilder.build();
    }

    @Override
    public String serialize() {
        if (getStatus() != TokenProcessingStatus.CYPHERED)
            throw TokenNotCipheredExc.newInstance();
        return token.serialize();
    }

    @Override
    public String getSubject() {
        return builder.build().getSubject();
    }

    @Override
    public void setSubject(final String subject) {
        checkUnmodifiable();
        builder = builder.subject(subject);
        status = TokenProcessingStatus.FORGED;
    }

    @Override
    public Set<TokenScope> getScopes() {
        final String scopes = (String) builder.build().getClaim(SCOPES.getValue());
        if (StringUtils.isBlank(scopes)) return Collections.emptySet();
        return splitScopes(scopes);
    }

    @Override
    public void setScopes(final Set<TokenScope> scopes) {
        checkUnmodifiable();
        builder = builder.claim(SCOPES.getValue(), compactScopes(scopes));
        status = TokenProcessingStatus.FORGED;
    }

    @Override
    public String getBP() {
        return (String) builder.build().getClaim(BP.getValue());
    }

    @Override
    public void setBP(final String bp) {
        checkUnmodifiable();
        builder = builder.claim(BP.getValue(), bp);
        status = TokenProcessingStatus.FORGED;
    }

    @Override
    public String getState() {
        return (String) builder.build().getClaim(STATE.getValue());
    }

    @Override
    public void setState(final String state) {
        checkUnmodifiable();
        builder = builder.claim(STATE.getValue(), state);
        status = TokenProcessingStatus.FORGED;
    }

    @Override
    public String getIssuer() {
        return builder.build().getIssuer();
    }

    @Override
    public void setIssuer(final String issuerURI) {
        checkUnmodifiable();
        builder = builder.issuer(issuerURI);
        status = TokenProcessingStatus.FORGED;
    }

    @Override
    public String getAudience() {
        return builder.build().getAudience().stream().findFirst().orElse(null);
    }

    @Override
    public void setAudience(final String audienceURI) {
        checkUnmodifiable();
        builder = builder.audience(audienceURI);
        status = TokenProcessingStatus.FORGED;
    }

    @Override
    public String getTargetURL() {
        return (String) builder.build().getClaim(TARGET_URL.getValue());
    }

    @Override
    public void setTargetURL(final String url) {
        checkUnmodifiable();
        builder = builder.claim(TARGET_URL.getValue(), url);
        status = TokenProcessingStatus.FORGED;
    }

    @Override
    public String getDelegator() {
        return (String) builder.build().getClaim(DELEGATOR.getValue());
    }

    @Override
    public void setDelegator(final String delegatorID) {
        checkUnmodifiable();
        builder = builder.claim(DELEGATOR.getValue(), delegatorID);
        status = TokenProcessingStatus.FORGED;
    }

    @Override
    public String getDelegate() {
        return (String) builder.build().getClaim(DELEGATE.getValue());
    }

    @Override
    public void setDelegate(final String delegateURI) {
        checkUnmodifiable();
        builder = builder.claim(DELEGATE.getValue(), delegateURI);
        status = TokenProcessingStatus.FORGED;
    }

    @Override
    public TokenVerdict getVerdict() {
        return builder.build().getClaim(VERDICT.getValue()) != null
                ? TokenVerdict.valueOf((String) builder.build().getClaim(VERDICT.getValue()))
                : null;
    }

    @Override
    public void setVerdict(final TokenVerdict verdict) {
        checkUnmodifiable();
        builder = builder.claim(VERDICT.getValue(), verdict != null ? verdict.name() : null);
        status = TokenProcessingStatus.FORGED;
    }

    @Override
    public void setClaim(final String claimURI, final String value) {
        checkCommitted();
        builder = builder.claim(claimURI, value);
        status = TokenProcessingStatus.FORGED;
    }

    @Override
    public String getClaim(final String claimURI) {
        return (String) builder.build().getClaim(claimURI);
    }

    @Override
    public LocalDateTime getExpiryTime() {
        if (builder.build().getExpirationTime() == null) return null;
        return LocalDateTime.ofInstant(builder.build().getExpirationTime().toInstant(), ZoneId.systemDefault());
    }

    @Override
    public LocalDateTime getEffectiveTime() {
        if (builder.build().getNotBeforeTime() == null) return null;
        return LocalDateTime.ofInstant(builder.build().getNotBeforeTime().toInstant(), ZoneId.systemDefault());
    }

    @Override
    public LocalDateTime getIssuanceTime() {
        if (builder.build().getIssueTime() == null) return null;
        return LocalDateTime.ofInstant(builder.build().getIssueTime().toInstant(), ZoneId.systemDefault());
    }

    @Override
    public void decipher() {
        checkCommitted();
        try {
            builder = new JWTClaimsSet.Builder(decipherClaims().getJWTClaimsSet());
            status = TokenProcessingStatus.DECIPHERED;
        } catch (JOSEException | ParseException e) {
            throw JOSEErrorExc.newInstance(e);
        }
    }

    private SignedJWT decipherClaims() throws JOSEException, ParseException {
        token.decrypt(new RSADecrypter(privateKey));
        final SignedJWT signedJWT = token.getPayload().toSignedJWT();
        checkSignature(signedJWT);
        checkIssuerMatch(signedJWT);
        return signedJWT;
    }

    private void checkIssuerMatch(final SignedJWT signedJWT) throws ParseException {
        if (!String.valueOf(signedJWT.getHeader().getCustomParam(ISSUER.getValue()))
                .equals(signedJWT.getJWTClaimsSet().getIssuer()))
            throw InvalidTokenExc.newInstance("Issuer mismatch");
    }

    private void checkSignature(final SignedJWT signedJWT) throws JOSEException {
        if (signedJWT == null || !signedJWT.verify(new RSASSAVerifier((RSAPublicKey) publicKey)))
            throw JOSEErrorExc.newInstance(new JOSEException("Failed to verify signature"));
    }
}
