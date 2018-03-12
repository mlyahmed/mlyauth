package com.mlyauth.token.jose;

import com.mlyauth.constants.TokenScope;
import com.mlyauth.constants.TokenStatus;
import com.mlyauth.constants.TokenVerdict;
import com.mlyauth.exception.InvalidTokenException;
import com.mlyauth.exception.JOSEErrorException;
import com.mlyauth.exception.TokenUnmodifiableException;
import com.mlyauth.token.IDPClaims;
import com.mlyauth.tools.KeysForTests;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import javafx.util.Pair;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.mlyauth.token.IDPClaims.*;
import static org.exparity.hamcrest.date.LocalDateTimeMatchers.within;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class JOSECypheredAccessTokenTest {

    private Pair<PrivateKey, PublicKey> cypherCred;
    private Pair<PrivateKey, PublicKey> decipherCred;
    private JOSEAccessToken token;
    private JWTClaimsSet expectedClaims;
    private JWEObject tokenEncrypted;

    @Before
    public void setup() throws JOSEException {
        final Pair<PrivateKey, X509Certificate> peerCred = KeysForTests.generateRSACredential();
        final Pair<PrivateKey, X509Certificate> localCred = KeysForTests.generateRSACredential();
        cypherCred = new Pair<>(localCred.getKey(), peerCred.getValue().getPublicKey());
        decipherCred = new Pair<>(peerCred.getKey(), localCred.getValue().getPublicKey());
        given_expected_claims();
        given_the_claims_are_cyphered();
    }

    @Test
    public void the_token_status_must_be_cyphered() {
        token = new JOSEAccessToken(tokenEncrypted.serialize(), decipherCred.getKey(), decipherCred.getValue());
        assertThat(token.getStatus(), equalTo(TokenStatus.CYPHERED));
    }

    @Test
    public void when_decipher_then_it_must_be_deciphered() {
        token = new JOSEAccessToken(tokenEncrypted.serialize(), decipherCred.getKey(), decipherCred.getValue());
        token.decipher();
        assertThat(token.getStatus(), equalTo(TokenStatus.DECIPHERED));
    }

    @Test
    public void when_given_cyphered_token_then_the_stamp_is_loaded() {
        when_decipher_the_token();
        assertThat(token.getStamp(), equalTo(expectedClaims.getJWTID()));
    }

    @Test
    public void when_given_cyphered_token_then_the_subject_is_loaded() {
        when_decipher_the_token();
        assertThat(token.getSubject(), equalTo(expectedClaims.getSubject()));
    }

    @Test
    public void when_given_cyphered_token_then_the_bp_is_loaded() {
        when_decipher_the_token();
        assertThat(token.getBP(), equalTo(expectedClaims.getClaim(BP.getValue())));
    }

    @Test
    public void when_given_cyphered_token_then_the_copes_are_loaded() {
        when_decipher_the_token();
        assertThat(expectedClaims.getClaim(SCOPES.getValue()), notNullValue());
        assertThat(token.getScopes(), equalTo(Arrays.stream(expectedClaims.getClaim(SCOPES.getValue())
                .toString().split("\\|")).map(TokenScope::valueOf).collect(Collectors.toSet())));
    }

    @Test
    public void when_given_cyphered_token_then_the_issuer_is_loaded() {
        when_decipher_the_token();
        assertThat(token.getIssuer(), equalTo(expectedClaims.getIssuer()));
    }

    @Test(expected = InvalidTokenException.class)
    public void when_the_issuer_in_header_and_as_claim_are_different_then_error() throws JOSEException {
        given_the_claims_are_cyphered_with_mismatch_issue_in_header();
        when_decipher_the_token();
    }

    @Test
    public void when_given_cyphered_token_then_the_state_is_loaded() {
        when_decipher_the_token();
        assertThat(token.getState(), equalTo(expectedClaims.getClaim(STATE.getValue())));
    }

    @Test
    public void when_given_cyphered_token_then_the_audience_is_loaded() {
        when_decipher_the_token();
        assertThat(token.getAudience(), equalTo(expectedClaims.getAudience().get(0)));
    }

    @Test
    public void when_given_cyphered_token_then_the_target_url_is_loaded() {
        when_decipher_the_token();
        assertThat(token.getTargetURL(), equalTo(expectedClaims.getClaim(TARGET_URL.getValue())));
    }

    @Test
    public void when_given_cyphered_token_then_the_delegator_is_loaded() {
        when_decipher_the_token();
        assertThat(token.getDelegator(), equalTo(expectedClaims.getClaim(DELEGATOR.getValue())));
    }

    @Test
    public void when_given_cyphered_token_then_the_delegate_is_loaded() {
        when_decipher_the_token();
        assertThat(token.getDelegate(), equalTo(expectedClaims.getClaim(DELEGATE.getValue())));
    }

    @Test
    public void when_given_cyphered_token_then_the_verdict_is_loaded() {
        when_decipher_the_token();
        assertThat(token.getVerdict(), equalTo(expectedClaims.getClaim(VERDICT.getValue())));
    }

    @Test
    public void when_given_cyphered_token_then_the_expiry_time_is_loaded() {
        when_decipher_the_token();
        assertThat(token.getExpiryTime(), notNullValue());
        assertThat(token.getExpiryTime(), within(1, ChronoUnit.SECONDS,
                LocalDateTime.ofInstant(expectedClaims.getExpirationTime().toInstant(), ZoneId.systemDefault())));
    }

    @Test
    public void when_given_cyphered_token_then_the_effective_time_is_loaded() {
        when_decipher_the_token();
        assertThat(token.getEffectiveTime(), notNullValue());
        assertThat(token.getEffectiveTime(), within(1, ChronoUnit.SECONDS,
                LocalDateTime.ofInstant(expectedClaims.getNotBeforeTime().toInstant(), ZoneId.systemDefault())));
    }

    @Test
    public void when_given_cyphered_token_then_the_issuance_time_is_loaded() {
        when_decipher_the_token();
        assertThat(token.getIssuanceTime(), notNullValue());
        assertThat(token.getIssuanceTime(), within(1, ChronoUnit.SECONDS,
                LocalDateTime.ofInstant(expectedClaims.getIssueTime().toInstant(), ZoneId.systemDefault())));
    }

    @Test(expected = JOSEErrorException.class)
    public void when_the_decryption_key_does_not_match_then_error() {
        final Pair<PrivateKey, X509Certificate> rsaCred = KeysForTests.generateRSACredential();
        Pair<PrivateKey, RSAPublicKey> wrongCred = new Pair<>(rsaCred.getKey(), (RSAPublicKey) rsaCred.getValue().getPublicKey());
        token = new JOSEAccessToken(tokenEncrypted.serialize(), wrongCred.getKey(), decipherCred.getValue());
        token.decipher();
    }

    @Test(expected = JOSEErrorException.class)
    public void when_the_signature_key_does_not_match_then_error() {
        final Pair<PrivateKey, X509Certificate> rsaCred = KeysForTests.generateRSACredential();
        Pair<PrivateKey, RSAPublicKey> wrongCred = new Pair<>(rsaCred.getKey(), (RSAPublicKey) rsaCred.getValue().getPublicKey());
        token = new JOSEAccessToken(tokenEncrypted.serialize(), decipherCred.getKey(), wrongCred.getValue());
        token.decipher();
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_the_cyphered_token_is_null_then_erro() {
        new JOSEAccessToken(null, decipherCred.getKey(), decipherCred.getValue());
    }

    @Test(expected = JOSEErrorException.class)
    public void when_the_cyphered_token_is_not_well_formatted_then_error() {
        new JOSEAccessToken(randomString(), decipherCred.getKey(), decipherCred.getValue());
    }

    @Test(expected = JOSEErrorException.class)
    public void when_the_token_is_not_signed_then_error() throws JOSEException {
        final JWEHeader header = new JWEHeader(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A128GCM);
        EncryptedJWT tokenHolder = new EncryptedJWT(header, expectedClaims);
        tokenHolder.encrypt(new RSAEncrypter((RSAPublicKey) cypherCred.getValue()));
        token = new JOSEAccessToken(tokenHolder.serialize(), decipherCred.getKey(), decipherCred.getValue());
        token.decipher();
    }

    @Test(expected = JOSEErrorException.class)
    public void when_the_token_is_signed_but_not_encrypted_then_error() throws JOSEException {
        SignedJWT tokenSigned = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), expectedClaims);
        tokenSigned.sign(new RSASSASigner(cypherCred.getKey()));
        token = new JOSEAccessToken(tokenSigned.serialize(), decipherCred.getKey(), decipherCred.getValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_the_private_key_is_null_then_erro() {
        new JOSEAccessToken(tokenEncrypted.serialize(), null, decipherCred.getValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_the_public_key_is_null_then_error() {
        new JOSEAccessToken(tokenEncrypted.serialize(), decipherCred.getKey(), null);
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_stamp_is_not_modifiable_before_decipher() {
        token = new JOSEAccessToken(tokenEncrypted.serialize(), decipherCred.getKey(), decipherCred.getValue());
        token.setStamp(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_stamp_is_not_modifiable_after_decipher() {
        when_decipher_the_token();
        token.setStamp(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_subject_is_not_modifiable_before_decipher() {
        token = new JOSEAccessToken(tokenEncrypted.serialize(), decipherCred.getKey(), decipherCred.getValue());
        token.setSubject(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_subject_is_not_modifiable_after_decipher() {
        when_decipher_the_token();
        token.setSubject(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_scopes_are_not_modifiable_before_decipher() {
        token = new JOSEAccessToken(tokenEncrypted.serialize(), decipherCred.getKey(), decipherCred.getValue());
        token.setScopes(new HashSet<>(Arrays.asList(TokenScope.values())));
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_scopes_are_not_modifiable_after_decipher() {
        when_decipher_the_token();
        token.setScopes(new HashSet<>(Arrays.asList(TokenScope.values())));
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_bp_is_not_modifiable_before_decipher() {
        token = new JOSEAccessToken(tokenEncrypted.serialize(), decipherCred.getKey(), decipherCred.getValue());
        token.setBP(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_bp_is_not_modifiable_after_decipher() {
        when_decipher_the_token();
        token.setBP(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_state_is_not_modifiable_before_decipher() {
        token = new JOSEAccessToken(tokenEncrypted.serialize(), decipherCred.getKey(), decipherCred.getValue());
        token.setState(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_state_is_not_modifiable_after_decipher() {
        when_decipher_the_token();
        token.setState(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_issuer_is_not_modifiable_before_decipher() {
        token = new JOSEAccessToken(tokenEncrypted.serialize(), decipherCred.getKey(), decipherCred.getValue());
        token.setIssuer(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_issuer_is_not_modifiable_after_decipher() {
        when_decipher_the_token();
        token.setIssuer(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_audience_is_not_modifiable_before_decipher() {
        token = new JOSEAccessToken(tokenEncrypted.serialize(), decipherCred.getKey(), decipherCred.getValue());
        token.setAudience(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_audience_is_not_modifiable_after_decipher() {
        when_decipher_the_token();
        token.setAudience(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_target_url_is_not_modifiable_before_decipher() {
        token = new JOSEAccessToken(tokenEncrypted.serialize(), decipherCred.getKey(), decipherCred.getValue());
        token.setTargetURL(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_target_url_is_not_modifiable_after_decipher() {
        when_decipher_the_token();
        token.setTargetURL(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_delegator_is_not_modifiable_before_decipher() {
        token = new JOSEAccessToken(tokenEncrypted.serialize(), decipherCred.getKey(), decipherCred.getValue());
        token.setDelegator(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_delegator_is_not_modifiable_after_decipher() {
        when_decipher_the_token();
        token.setDelegator(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_delegate_is_not_modifiable_before_decipher() {
        token = new JOSEAccessToken(tokenEncrypted.serialize(), decipherCred.getKey(), decipherCred.getValue());
        token.setDelegate(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_delegate_is_not_modifiable_after_decipher() {
        when_decipher_the_token();
        token.setDelegate(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_verdict_is_not_modifiable_before_decipher() {
        token = new JOSEAccessToken(tokenEncrypted.serialize(), decipherCred.getKey(), decipherCred.getValue());
        token.setVerdict(TokenVerdict.SUCCESS);
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_verdict_is_not_modifiable_after_decipher() {
        when_decipher_the_token();
        token.setVerdict(TokenVerdict.SUCCESS);
    }

    private void given_expected_claims() {
        expectedClaims = new JWTClaimsSet.Builder()
                .jwtID(UUID.randomUUID().toString())
                .subject(randomString())
                .claim(SCOPES.getValue(), Arrays.stream(TokenScope.values()).map(TokenScope::name)
                        .collect(Collectors.joining("|")))
                .claim(IDPClaims.BP.getValue(), randomString())
                .claim(STATE.getValue(), randomString())
                .issuer(randomString())
                .audience(randomString())
                .claim(IDPClaims.TARGET_URL.getValue(), randomString())
                .claim(IDPClaims.DELEGATOR.getValue(), randomString())
                .claim(IDPClaims.DELEGATE.getValue(), randomString())
                .claim(IDPClaims.VERDICT.getValue(), TokenVerdict.SUCCESS)
                .expirationTime(new Date(System.currentTimeMillis() + 1000 * 60 * 3))
                .notBeforeTime(new Date())
                .issueTime(new Date())
                .build();
    }

    private void when_decipher_the_token() {
        token = new JOSEAccessToken(tokenEncrypted.serialize(), decipherCred.getKey(), decipherCred.getValue());
        token.decipher();
    }

    private void given_the_claims_are_cyphered() throws JOSEException {
        final JWSHeader sHeader = new JWSHeader.Builder(JWSAlgorithm.RS256).customParam(ISSUER.getValue(),
                expectedClaims.getIssuer()).build();
        SignedJWT tokenSigned = new SignedJWT(sHeader, expectedClaims);

        tokenSigned.sign(new RSASSASigner(cypherCred.getKey()));
        final JWEHeader header = new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A128GCM).build();
        tokenEncrypted = new JWEObject(header, new Payload(tokenSigned));
        tokenEncrypted.encrypt(new RSAEncrypter((RSAPublicKey) cypherCred.getValue()));
    }

    private void given_the_claims_are_cyphered_with_mismatch_issue_in_header() throws JOSEException {
        SignedJWT tokenSigned = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).
                customParam(ISSUER.getValue(), randomString())
                .build(), expectedClaims);
        tokenSigned.sign(new RSASSASigner(cypherCred.getKey()));
        final JWEHeader header = new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A128GCM).build();
        tokenEncrypted = new JWEObject(header, new Payload(tokenSigned));
        tokenEncrypted.encrypt(new RSAEncrypter((RSAPublicKey) cypherCred.getValue()));
    }

    private static String randomString() {
        final int length = (new Random()).nextInt(50);
        return RandomStringUtils.random(length > 0 ? length : 50, true, true);
    }
}
