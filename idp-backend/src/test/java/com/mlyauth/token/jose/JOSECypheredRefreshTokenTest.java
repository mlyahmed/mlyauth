package com.mlyauth.token.jose;

import com.mlyauth.constants.TokenProcessingStatus;
import com.mlyauth.constants.TokenScope;
import com.mlyauth.constants.TokenVerdict;
import com.mlyauth.exception.InvalidTokenException;
import com.mlyauth.exception.JOSEErrorException;
import com.mlyauth.exception.TokenUnmodifiableException;
import com.mlyauth.token.Claims;
import com.mlyauth.tools.KeysForTests;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import javafx.util.Pair;
import org.junit.Before;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.mlyauth.token.Claims.*;
import static com.mlyauth.tools.RandomForTests.randomString;
import static org.exparity.hamcrest.date.LocalDateTimeMatchers.within;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class JOSECypheredRefreshTokenTest {

    private Pair<PrivateKey, PublicKey> cypherCred;
    private Pair<PrivateKey, PublicKey> decipherCred;
    private JOSERefreshToken refreshToken;
    private JWTClaimsSet refreshClaims;
    private JWEObject cyphered;

    @Before
    public void setup() throws JOSEException {
        final Pair<PrivateKey, X509Certificate> peerCred = KeysForTests.generateRSACredential();
        final Pair<PrivateKey, X509Certificate> localCred = KeysForTests.generateRSACredential();
        cypherCred = new Pair<>(localCred.getKey(), peerCred.getValue().getPublicKey());
        decipherCred = new Pair<>(peerCred.getKey(), localCred.getValue().getPublicKey());
        given_expected_refresh_claims();
        given_the_refresh_claims_are_cyphered();
    }

    @Test
    public void the_refresh_token_status_must_be_cyphered() {
        refreshToken = new JOSERefreshToken(cyphered.serialize(), decipherCred.getKey(), decipherCred.getValue());
        assertThat(refreshToken.getStatus(), equalTo(TokenProcessingStatus.CYPHERED));
    }

    @Test
    public void when_decipher_the_refresh_token_then_it_must_be_deciphered() {
        when_decipher_the_refresh_token();
        assertThat(refreshToken.getStatus(), equalTo(TokenProcessingStatus.DECIPHERED));
    }

    @Test
    public void when_decipher_the_refresh_token_then_the_stamp_is_loaded() {
        when_decipher_the_refresh_token();
        assertThat(refreshToken.getStamp(), equalTo(refreshClaims.getJWTID()));
    }

    @Test
    public void when_decipher_the_refresh_token_then_the_subject_is_loaded() {
        when_decipher_the_refresh_token();
        assertThat(refreshToken.getSubject(), equalTo(refreshClaims.getSubject()));
    }

    @Test
    public void when_decipher_the_refresh_token_then_the_bp_is_loaded() {
        when_decipher_the_refresh_token();
        assertThat(refreshToken.getBP(), equalTo(refreshClaims.getClaim(BP.getValue())));
    }

    @Test
    public void when_decipher_the_refresh_token_then_the_scopes_are_loaded() {
        when_decipher_the_refresh_token();
        assertThat(refreshClaims.getClaim(SCOPES.getValue()), notNullValue());
        assertThat(refreshToken.getScopes(), equalTo(Arrays.stream(refreshClaims.getClaim(SCOPES.getValue())
                .toString().split("\\|")).map(TokenScope::valueOf).collect(Collectors.toSet())));
    }

    @Test
    public void when_decipher_the_access_token_then_the_issuer_is_loaded() {
        when_decipher_the_refresh_token();
        assertThat(refreshToken.getIssuer(), equalTo(refreshClaims.getIssuer()));
    }

    @Test(expected = InvalidTokenException.class)
    public void when_the_refresh_issuer_in_header_and_in_claims_are_different_then_error() throws JOSEException {
        given_the_refresh_claims_are_cyphered_with_mismatch_issuer_in_header();
        when_decipher_the_refresh_token();
    }

    @Test
    public void when_decipher_the_refresh_token_then_the_state_is_loaded() {
        when_decipher_the_refresh_token();
        assertThat(refreshToken.getState(), equalTo(refreshClaims.getClaim(STATE.getValue())));
    }

    @Test
    public void when_decipher_the_refresh_token_then_the_audience_is_loaded() {
        when_decipher_the_refresh_token();
        assertThat(refreshClaims.getAudience(), hasSize(1));
        assertThat(refreshToken.getAudience(), equalTo(refreshClaims.getAudience().get(0)));
    }

    @Test
    public void when_decipher_the_refresh_token_then_the_target_url_is_loaded() {
        when_decipher_the_refresh_token();
        assertThat(refreshToken.getTargetURL(), equalTo(refreshClaims.getClaim(TARGET_URL.getValue())));
    }

    @Test
    public void when_decipher_the_refresh_token_then_the_delegator_is_loaded() {
        when_decipher_the_refresh_token();
        assertThat(refreshToken.getDelegator(), equalTo(refreshClaims.getClaim(DELEGATOR.getValue())));
    }

    @Test
    public void when_decipher_the_refresh_token_then_the_delegate_is_loaded() {
        when_decipher_the_refresh_token();
        assertThat(refreshToken.getDelegate(), equalTo(refreshClaims.getClaim(DELEGATE.getValue())));
    }

    @Test
    public void when_decipher_the_refresh_token_then_the_verdict_is_loaded() {
        when_decipher_the_refresh_token();
        assertThat(refreshToken.getVerdict(), equalTo(refreshClaims.getClaim(VERDICT.getValue())));
    }

    @Test
    public void when_decipher_the_refresh_token_then_the_expiry_time_is_loaded() {
        when_decipher_the_refresh_token();
        assertThat(refreshToken.getExpiryTime(), notNullValue());
        assertThat(refreshToken.getExpiryTime(), within(1, ChronoUnit.SECONDS,
                LocalDateTime.ofInstant(refreshClaims.getExpirationTime().toInstant(), ZoneId.systemDefault())));
    }

    @Test
    public void when_decipher_the_refresh_token_then_the_effective_time_is_loaded() {
        when_decipher_the_refresh_token();
        assertThat(refreshToken.getEffectiveTime(), notNullValue());
        assertThat(refreshToken.getEffectiveTime(), within(1, ChronoUnit.SECONDS,
                LocalDateTime.ofInstant(refreshClaims.getNotBeforeTime().toInstant(), ZoneId.systemDefault())));
    }

    @Test
    public void when_decipher_the_refresh_token_then_the_issuance_time_is_loaded() {
        when_decipher_the_refresh_token();
        assertThat(refreshToken.getIssuanceTime(), notNullValue());
        assertThat(refreshToken.getIssuanceTime(), within(1, ChronoUnit.SECONDS,
                LocalDateTime.ofInstant(refreshClaims.getIssueTime().toInstant(), ZoneId.systemDefault())));
    }

    @Test(expected = JOSEErrorException.class)
    public void when_the_refresh_decryption_key_does_not_match_then_error() {
        Pair<PrivateKey, RSAPublicKey> wrongCred = given_wrong_Credential();
        refreshToken = new JOSERefreshToken(cyphered.serialize(), wrongCred.getKey(), decipherCred.getValue());
        refreshToken.decipher();
    }

    @Test(expected = JOSEErrorException.class)
    public void when_the_refresh_signature_key_does_not_match_then_error() {
        Pair<PrivateKey, RSAPublicKey> wrongCred = given_wrong_Credential();
        refreshToken = new JOSERefreshToken(cyphered.serialize(), decipherCred.getKey(), wrongCred.getValue());
        refreshToken.decipher();
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_the_cyphered_refresh_token_is_null_then_error() {
        new JOSERefreshToken(null, decipherCred.getKey(), decipherCred.getValue());
    }

    @Test(expected = JOSEErrorException.class)
    public void when_the_cyphered_refresh_token_is_not_well_formatted_then_error() {
        new JOSERefreshToken(randomString(), decipherCred.getKey(), decipherCred.getValue());
    }

    @Test(expected = JOSEErrorException.class)
    public void when_the_refresh_token_is_not_signed_then_error() throws JOSEException {
        final JWEHeader tokenHeader = new JWEHeader(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A128GCM);
        EncryptedJWT tokenHolder = new EncryptedJWT(tokenHeader, refreshClaims);
        tokenHolder.encrypt(new RSAEncrypter((RSAPublicKey) cypherCred.getValue()));
        refreshToken = new JOSERefreshToken(tokenHolder.serialize(), decipherCred.getKey(), decipherCred.getValue());
        refreshToken.decipher();
    }

    @Test(expected = JOSEErrorException.class)
    public void when_the_refresh_token_is_signed_but_not_encrypted_then_error() throws JOSEException {
        SignedJWT tokenSigned = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), refreshClaims);
        tokenSigned.sign(new RSASSASigner(cypherCred.getKey()));
        refreshToken = new JOSERefreshToken(tokenSigned.serialize(), decipherCred.getKey(), decipherCred.getValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_the_refresh_private_key_is_null_then_error() {
        new JOSERefreshToken(cyphered.serialize(), null, decipherCred.getValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_the_refresh_public_key_is_null_then_error() {
        new JOSERefreshToken(cyphered.serialize(), decipherCred.getKey(), null);
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_stamp_is_not_modifiable_before_deciphering_the_refresh() {
        refreshToken = new JOSERefreshToken(cyphered.serialize(), decipherCred.getKey(), decipherCred.getValue());
        refreshToken.setStamp(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_stamp_is_not_modifiable_after_deciphering_the_refresh() {
        when_decipher_the_refresh_token();
        refreshToken.setStamp(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_subject_is_not_modifiable_before_deciphing_the_refresh() {
        refreshToken = new JOSERefreshToken(cyphered.serialize(), decipherCred.getKey(), decipherCred.getValue());
        refreshToken.setSubject(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_subject_is_not_modifiable_after_deciphering_the_refresh() {
        when_decipher_the_refresh_token();
        refreshToken.setSubject(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_scopes_are_not_modifiable_before_deciphing_the_refresh() {
        refreshToken = new JOSERefreshToken(cyphered.serialize(), decipherCred.getKey(), decipherCred.getValue());
        refreshToken.setScopes(new HashSet<>(Arrays.asList(TokenScope.values())));
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_scopes_are_not_modifiable_after_deciphering_the_refresh() {
        when_decipher_the_refresh_token();
        refreshToken.setScopes(new HashSet<>(Arrays.asList(TokenScope.values())));
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_bp_is_not_modifiable_before_deciphering_the_refresh() {
        refreshToken = new JOSERefreshToken(cyphered.serialize(), decipherCred.getKey(), decipherCred.getValue());
        refreshToken.setBP(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_bp_is_not_modifiable_after_deciphering_the_refresh() {
        when_decipher_the_refresh_token();
        refreshToken.setBP(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_state_is_not_modifiable_before_deciphering_the_refresh() {
        refreshToken = new JOSERefreshToken(cyphered.serialize(), decipherCred.getKey(), decipherCred.getValue());
        refreshToken.setState(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_state_is_not_modifiable_after_deciphering_the_refresh() {
        when_decipher_the_refresh_token();
        refreshToken.setState(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_issuer_is_not_modifiable_before_deciphering_the_refresh() {
        refreshToken = new JOSERefreshToken(cyphered.serialize(), decipherCred.getKey(), decipherCred.getValue());
        refreshToken.setIssuer(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_issuer_is_not_modifiable_after_deciphering_the_refresh() {
        when_decipher_the_refresh_token();
        refreshToken.setIssuer(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_audience_is_not_modifiable_before_deciphering_the_refresh() {
        refreshToken = new JOSERefreshToken(cyphered.serialize(), decipherCred.getKey(), decipherCred.getValue());
        refreshToken.setAudience(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_audience_is_not_modifiable_after_deciphering_the_refresh() {
        when_decipher_the_refresh_token();
        refreshToken.setAudience(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_target_url_is_not_modifiable_before_deciphering_the_refresh() {
        refreshToken = new JOSERefreshToken(cyphered.serialize(), decipherCred.getKey(), decipherCred.getValue());
        refreshToken.setTargetURL(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_target_url_is_not_modifiable_after_deciphering_the_refresh() {
        when_decipher_the_refresh_token();
        refreshToken.setTargetURL(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_delegator_is_not_modifiable_before_deciphering_the_refresh() {
        refreshToken = new JOSERefreshToken(cyphered.serialize(), decipherCred.getKey(), decipherCred.getValue());
        refreshToken.setDelegator(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_delegator_is_not_modifiable_after_deciphering_the_refresh() {
        when_decipher_the_refresh_token();
        refreshToken.setDelegator(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_delegate_is_not_modifiable_before_deciphering_the_refresh() {
        refreshToken = new JOSERefreshToken(cyphered.serialize(), decipherCred.getKey(), decipherCred.getValue());
        refreshToken.setDelegate(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_delegate_is_not_modifiable_after_deciphering_the_refresh() {
        when_decipher_the_refresh_token();
        refreshToken.setDelegate(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_verdict_is_not_modifiable_before_deciphering_the_refresh() {
        refreshToken = new JOSERefreshToken(cyphered.serialize(), decipherCred.getKey(), decipherCred.getValue());
        refreshToken.setVerdict(TokenVerdict.SUCCESS);
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_verdict_is_not_modifiable_after_deciphering_the_refresh() {
        when_decipher_the_refresh_token();
        refreshToken.setVerdict(TokenVerdict.SUCCESS);
    }

    private void given_expected_refresh_claims() {
        refreshClaims = new JWTClaimsSet.Builder()
                .jwtID(UUID.randomUUID().toString())
                .subject(randomString())
                .claim(SCOPES.getValue(), Arrays.stream(TokenScope.values()).map(TokenScope::name)
                        .collect(Collectors.joining("|")))
                .claim(Claims.BP.getValue(), randomString())
                .claim(STATE.getValue(), randomString())
                .issuer(randomString())
                .audience(randomString())
                .claim(Claims.TARGET_URL.getValue(), randomString())
                .claim(Claims.DELEGATOR.getValue(), randomString())
                .claim(Claims.DELEGATE.getValue(), randomString())
                .claim(Claims.VERDICT.getValue(), TokenVerdict.SUCCESS)
                .expirationTime(new Date(System.currentTimeMillis() + 1000 * 60 * 3))
                .notBeforeTime(new Date())
                .issueTime(new Date())
                .build();
    }

    @SuppressWarnings("Duplicates")
    private void given_the_refresh_claims_are_cyphered() throws JOSEException {
        final JWSHeader signatureHeader = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .customParam(ISSUER.getValue(), refreshClaims.getIssuer())
                .build();
        SignedJWT signature = new SignedJWT(signatureHeader, refreshClaims);
        signature.sign(new RSASSASigner(cypherCred.getKey()));
        final JWEHeader header = new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A128GCM).build();
        cyphered = new JWEObject(header, new Payload(signature));
        cyphered.encrypt(new RSAEncrypter((RSAPublicKey) cypherCred.getValue()));
    }

    @SuppressWarnings("Duplicates")
    private void given_the_refresh_claims_are_cyphered_with_mismatch_issuer_in_header() throws JOSEException {
        SignedJWT signature = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).
                customParam(ISSUER.getValue(), randomString())
                .build(), refreshClaims);
        signature.sign(new RSASSASigner(cypherCred.getKey()));
        final JWEHeader header = new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A128GCM).build();
        cyphered = new JWEObject(header, new Payload(signature));
        cyphered.encrypt(new RSAEncrypter((RSAPublicKey) cypherCred.getValue()));
    }

    private Pair<PrivateKey, RSAPublicKey> given_wrong_Credential() {
        final Pair<PrivateKey, X509Certificate> credential = KeysForTests.generateRSACredential();
        return new Pair<>(credential.getKey(), (RSAPublicKey) credential.getValue().getPublicKey());
    }

    private void when_decipher_the_refresh_token() {
        refreshToken = new JOSERefreshToken(cyphered.serialize(), decipherCred.getKey(), decipherCred.getValue());
        refreshToken.decipher();
    }

}
