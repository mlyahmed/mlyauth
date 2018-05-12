package com.mlyauth.token.jose;

import com.mlyauth.constants.TokenProcessingStatus;
import com.mlyauth.constants.TokenRefreshMode;
import com.mlyauth.constants.TokenScope;
import com.mlyauth.constants.TokenValidationMode;
import com.mlyauth.exception.InvalidTokenException;
import com.mlyauth.exception.JOSEErrorException;
import com.mlyauth.exception.TokenUnmodifiableException;
import com.mlyauth.token.Claims;
import com.mlyauth.tools.KeysForTests;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.Payload;
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
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.mlyauth.constants.TokenRefreshMode.EACH_TIME;
import static com.mlyauth.constants.TokenRefreshMode.WHEN_EXPIRES;
import static com.mlyauth.constants.TokenValidationMode.STANDARD;
import static com.mlyauth.constants.TokenValidationMode.STRICT;
import static com.mlyauth.constants.TokenVerdict.FAIL;
import static com.mlyauth.constants.TokenVerdict.SUCCESS;
import static com.mlyauth.token.Claims.BP;
import static com.mlyauth.token.Claims.DELEGATE;
import static com.mlyauth.token.Claims.DELEGATOR;
import static com.mlyauth.token.Claims.ISSUER;
import static com.mlyauth.token.Claims.REFRESH_MODE;
import static com.mlyauth.token.Claims.SCOPES;
import static com.mlyauth.token.Claims.STATE;
import static com.mlyauth.token.Claims.TARGET_URL;
import static com.mlyauth.token.Claims.VALIDATION_MODE;
import static com.mlyauth.token.Claims.VERDICT;
import static com.mlyauth.tools.RandomForTests.randomString;
import static org.exparity.hamcrest.date.LocalDateTimeMatchers.within;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class JOSECypheredAccessTokenTest {

    public static final int THREE_MINUTES = 1000 * 60 * 3;
    private Pair<PrivateKey, PublicKey> cypherCred;
    private Pair<PrivateKey, PublicKey> decipherCred;
    private JOSEAccessToken accessToken;
    private JWTClaimsSet accessClaims;
    private JWEObject cyphered;

    @Before
    public void setup() throws JOSEException {
        final Pair<PrivateKey, X509Certificate> peerCred = KeysForTests.generateRSACredential();
        final Pair<PrivateKey, X509Certificate> localCred = KeysForTests.generateRSACredential();
        cypherCred = new Pair<>(localCred.getKey(), peerCred.getValue().getPublicKey());
        decipherCred = new Pair<>(peerCred.getKey(), localCred.getValue().getPublicKey());
        given_expected_access_claims();
        given_the_access_claims_are_cyphered();
    }

    @Test
    public void the_access_token_status_must_be_cyphered() {
        accessToken = new JOSEAccessToken(cyphered.serialize(), decipherCred.getKey(), decipherCred.getValue());
        assertThat(accessToken.getStatus(), equalTo(TokenProcessingStatus.CYPHERED));
    }

    @Test
    public void when_decipher_the_access_token_then_it_must_be_deciphered() {
        when_decipher_the_access_token();
        assertThat(accessToken.getStatus(), equalTo(TokenProcessingStatus.DECIPHERED));
    }

    @Test
    public void when_decipher_the_access_token_then_the_validation_mode_is_loaded() {
        when_decipher_the_access_token();
        assertThat(accessToken.getValidationMode(), equalTo(accessClaims.getClaim(VALIDATION_MODE.getValue())));
    }

    @Test
    public void when_decipher_the_access_token_then_the_refresh_mode_is_loaded() {
        when_decipher_the_access_token();
        assertThat(accessToken.getRefreshMode(), equalTo(accessClaims.getClaim(REFRESH_MODE.getValue())));
    }

    @Test
    public void when_decipher_the_access_token_then_the_stamp_is_loaded() {
        when_decipher_the_access_token();
        assertThat(accessToken.getStamp(), equalTo(accessClaims.getJWTID()));
    }

    @Test
    public void when_decipher_the_access_token_then_the_subject_is_loaded() {
        when_decipher_the_access_token();
        assertThat(accessToken.getSubject(), equalTo(accessClaims.getSubject()));
    }

    @Test
    public void when_decipher_the_access_token_then_the_bp_is_loaded() {
        when_decipher_the_access_token();
        assertThat(accessToken.getBP(), equalTo(accessClaims.getClaim(BP.getValue())));
    }

    @Test
    public void when_decipher_the_access_token_then_the_scopes_are_loaded() {
        when_decipher_the_access_token();
        assertThat(accessClaims.getClaim(SCOPES.getValue()), notNullValue());
        assertThat(accessToken.getScopes(), equalTo(Arrays.stream(accessClaims.getClaim(SCOPES.getValue())
                .toString().split("\\|")).map(TokenScope::valueOf).collect(Collectors.toSet())));
    }

    @Test
    public void when_decipher_the_access_token_then_the_issuer_is_loaded() {
        when_decipher_the_access_token();
        assertThat(accessToken.getIssuer(), equalTo(accessClaims.getIssuer()));
    }

    @Test(expected = InvalidTokenException.class)
    public void when_the_access_issuer_in_header_and_in_claims_are_different_then_error() throws JOSEException {
        given_the_access_claims_are_cyphered_with_mismatch_issuer_in_header();
        when_decipher_the_access_token();
    }

    @Test
    public void when_decipher_the_access_token_then_the_state_is_loaded() {
        when_decipher_the_access_token();
        assertThat(accessToken.getState(), equalTo(accessClaims.getClaim(STATE.getValue())));
    }

    @Test
    public void when_decipher_the_access_token_then_the_audience_is_loaded() {
        when_decipher_the_access_token();
        assertThat(accessClaims.getAudience(), hasSize(1));
        assertThat(accessToken.getAudience(), equalTo(accessClaims.getAudience().get(0)));
    }

    @Test
    public void when_decipher_the_access_token_then_the_target_url_is_loaded() {
        when_decipher_the_access_token();
        assertThat(accessToken.getTargetURL(), equalTo(accessClaims.getClaim(TARGET_URL.getValue())));
    }

    @Test
    public void when_decipher_the_access_token_then_the_delegator_is_loaded() {
        when_decipher_the_access_token();
        assertThat(accessToken.getDelegator(), equalTo(accessClaims.getClaim(DELEGATOR.getValue())));
    }

    @Test
    public void when_decipher_the_access_token_then_the_delegate_is_loaded() {
        when_decipher_the_access_token();
        assertThat(accessToken.getDelegate(), equalTo(accessClaims.getClaim(DELEGATE.getValue())));
    }

    @Test
    public void when_decipher_the_access_token_then_the_verdict_is_loaded() {
        when_decipher_the_access_token();
        assertThat(accessToken.getVerdict(), equalTo(accessClaims.getClaim(VERDICT.getValue())));
    }

    @Test
    public void when_decipher_the_access_token_then_the_expiry_time_is_loaded() {
        when_decipher_the_access_token();
        assertThat(accessToken.getExpiryTime(), notNullValue());
        assertThat(accessToken.getExpiryTime(), within(1, ChronoUnit.SECONDS,
                LocalDateTime.ofInstant(accessClaims.getExpirationTime().toInstant(), ZoneId.systemDefault())));
    }

    @Test
    public void when_decipher_the_access_token_then_the_effective_time_is_loaded() {
        when_decipher_the_access_token();
        assertThat(accessToken.getEffectiveTime(), notNullValue());
        assertThat(accessToken.getEffectiveTime(), within(1, ChronoUnit.SECONDS,
                LocalDateTime.ofInstant(accessClaims.getNotBeforeTime().toInstant(), ZoneId.systemDefault())));
    }

    @Test
    public void when_decipher_the_access_token_then_the_issuance_time_is_loaded() {
        when_decipher_the_access_token();
        assertThat(accessToken.getIssuanceTime(), notNullValue());
        assertThat(accessToken.getIssuanceTime(), within(1, ChronoUnit.SECONDS,
                LocalDateTime.ofInstant(accessClaims.getIssueTime().toInstant(), ZoneId.systemDefault())));
    }

    @Test(expected = JOSEErrorException.class)
    public void when_the_access_decryption_key_does_not_match_then_error() {
        Pair<PrivateKey, RSAPublicKey> wrongCred = given_wrong_Credential();
        accessToken = new JOSEAccessToken(cyphered.serialize(), wrongCred.getKey(), decipherCred.getValue());
        accessToken.decipher();
    }

    @Test(expected = JOSEErrorException.class)
    public void when_the_access_signature_key_does_not_match_then_error() {
        Pair<PrivateKey, RSAPublicKey> wrongCred = given_wrong_Credential();
        accessToken = new JOSEAccessToken(cyphered.serialize(), decipherCred.getKey(), wrongCred.getValue());
        accessToken.decipher();
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_the_cyphered_access_token_is_null_then_error() {
        new JOSEAccessToken(null, decipherCred.getKey(), decipherCred.getValue());
    }

    @Test(expected = JOSEErrorException.class)
    public void when_the_cyphered_access_token_is_not_well_formatted_then_error() {
        new JOSEAccessToken(randomString(), decipherCred.getKey(), decipherCred.getValue());
    }

    @Test(expected = JOSEErrorException.class)
    public void when_the_access_token_is_not_signed_then_error() throws JOSEException {
        final JWEHeader header = new JWEHeader(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A128GCM);
        EncryptedJWT tokenHolder = new EncryptedJWT(header, accessClaims);
        tokenHolder.encrypt(new RSAEncrypter((RSAPublicKey) cypherCred.getValue()));
        accessToken = new JOSEAccessToken(tokenHolder.serialize(), decipherCred.getKey(), decipherCred.getValue());
        accessToken.decipher();
    }

    @Test(expected = JOSEErrorException.class)
    public void when_the_access_token_is_signed_but_not_encrypted_then_error() throws JOSEException {
        SignedJWT tokenSigned = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), accessClaims);
        tokenSigned.sign(new RSASSASigner(cypherCred.getKey()));
        accessToken = new JOSEAccessToken(tokenSigned.serialize(), decipherCred.getKey(), decipherCred.getValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_the_access_private_key_is_null_then_error() {
        new JOSEAccessToken(cyphered.serialize(), null, decipherCred.getValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_the_access_public_key_is_null_then_error() {
        new JOSEAccessToken(cyphered.serialize(), decipherCred.getKey(), null);
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_validation_mode_is_not_modifiable_before_deciphering_the_access() {
        accessToken = new JOSEAccessToken(cyphered.serialize(), decipherCred.getKey(), decipherCred.getValue());
        accessToken.setValidationMode(TokenValidationMode.STANDARD);
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_validation_mode_is_not_modifiable_after_deciphering_the_access() {
        when_decipher_the_access_token();
        accessToken.setValidationMode(TokenValidationMode.STRICT);
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_refresh_mode_is_not_modifiable_before_deciphering_the_access() {
        accessToken = new JOSEAccessToken(cyphered.serialize(), decipherCred.getKey(), decipherCred.getValue());
        accessToken.setRefreshMode(TokenRefreshMode.EACH_TIME);
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_refresh_mode_is_not_modifiable_after_deciphering_the_access() {
        when_decipher_the_access_token();
        accessToken.setRefreshMode(TokenRefreshMode.WHEN_EXPIRES);
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_stamp_is_not_modifiable_before_deciphering_the_access() {
        accessToken = new JOSEAccessToken(cyphered.serialize(), decipherCred.getKey(), decipherCred.getValue());
        accessToken.setStamp(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_stamp_is_not_modifiable_after_deciphering_the_access() {
        when_decipher_the_access_token();
        accessToken.setStamp(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_subject_is_not_modifiable_before_deciphing_the_access() {
        accessToken = new JOSEAccessToken(cyphered.serialize(), decipherCred.getKey(), decipherCred.getValue());
        accessToken.setSubject(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_subject_is_not_modifiable_after_deciphering_the_access() {
        when_decipher_the_access_token();
        accessToken.setSubject(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_scopes_are_not_modifiable_before_deciphing_the_access() {
        accessToken = new JOSEAccessToken(cyphered.serialize(), decipherCred.getKey(), decipherCred.getValue());
        accessToken.setScopes(new HashSet<>(Arrays.asList(TokenScope.values())));
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_scopes_are_not_modifiable_after_deciphering_the_access() {
        when_decipher_the_access_token();
        accessToken.setScopes(new HashSet<>(Arrays.asList(TokenScope.values())));
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_bp_is_not_modifiable_before_deciphering_the_access() {
        accessToken = new JOSEAccessToken(cyphered.serialize(), decipherCred.getKey(), decipherCred.getValue());
        accessToken.setBP(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_bp_is_not_modifiable_after_deciphering_the_access() {
        when_decipher_the_access_token();
        accessToken.setBP(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_state_is_not_modifiable_before_deciphering_the_access() {
        accessToken = new JOSEAccessToken(cyphered.serialize(), decipherCred.getKey(), decipherCred.getValue());
        accessToken.setState(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_state_is_not_modifiable_after_deciphering_the_access() {
        when_decipher_the_access_token();
        accessToken.setState(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_issuer_is_not_modifiable_before_deciphering_the_access() {
        accessToken = new JOSEAccessToken(cyphered.serialize(), decipherCred.getKey(), decipherCred.getValue());
        accessToken.setIssuer(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_issuer_is_not_modifiable_after_deciphering_the_access() {
        when_decipher_the_access_token();
        accessToken.setIssuer(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_audience_is_not_modifiable_before_deciphering_the_access() {
        accessToken = new JOSEAccessToken(cyphered.serialize(), decipherCred.getKey(), decipherCred.getValue());
        accessToken.setAudience(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_audience_is_not_modifiable_after_deciphering_the_access() {
        when_decipher_the_access_token();
        accessToken.setAudience(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_target_url_is_not_modifiable_before_deciphering_the_access() {
        accessToken = new JOSEAccessToken(cyphered.serialize(), decipherCred.getKey(), decipherCred.getValue());
        accessToken.setTargetURL(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_target_url_is_not_modifiable_after_deciphering_the_access() {
        when_decipher_the_access_token();
        accessToken.setTargetURL(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_delegator_is_not_modifiable_before_deciphering_the_access() {
        accessToken = new JOSEAccessToken(cyphered.serialize(), decipherCred.getKey(), decipherCred.getValue());
        accessToken.setDelegator(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_delegator_is_not_modifiable_after_deciphering_the_access() {
        when_decipher_the_access_token();
        accessToken.setDelegator(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_delegate_is_not_modifiable_before_deciphering_the_access() {
        accessToken = new JOSEAccessToken(cyphered.serialize(), decipherCred.getKey(), decipherCred.getValue());
        accessToken.setDelegate(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_delegate_is_not_modifiable_after_deciphering_the_access() {
        when_decipher_the_access_token();
        accessToken.setDelegate(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_verdict_is_not_modifiable_before_deciphering_the_access() {
        accessToken = new JOSEAccessToken(cyphered.serialize(), decipherCred.getKey(), decipherCred.getValue());
        accessToken.setVerdict(SUCCESS);
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_verdict_is_not_modifiable_after_deciphering_the_access() {
        when_decipher_the_access_token();
        accessToken.setVerdict(SUCCESS);
    }

    private void given_expected_access_claims() {
        final Random random = new Random();
        accessClaims = new JWTClaimsSet.Builder()
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
                //CHECKSTYLE:OFF
                .claim(Claims.VERDICT.getValue(), random.nextInt(2000) % 2 == 0 ? SUCCESS : FAIL)
                .claim(Claims.VALIDATION_MODE.getValue(), random.nextInt(542154) % 2 == 0 ? STANDARD : STRICT)
                .claim(Claims.REFRESH_MODE.getValue(), random.nextInt(54662)%2 == 0 ? WHEN_EXPIRES : EACH_TIME)
                //CHECKSTYLE:ON
                .expirationTime(new Date(System.currentTimeMillis() + THREE_MINUTES))
                .notBeforeTime(new Date())
                .issueTime(new Date())
                .build();
    }

    @SuppressWarnings("Duplicates")
    private void given_the_access_claims_are_cyphered() throws JOSEException {
        final JWSHeader signatureHeader = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .customParam(ISSUER.getValue(), accessClaims.getIssuer())
                .build();
        SignedJWT signature = new SignedJWT(signatureHeader, accessClaims);
        signature.sign(new RSASSASigner(cypherCred.getKey()));
        final JWEHeader header = new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A128GCM).build();
        cyphered = new JWEObject(header, new Payload(signature));
        cyphered.encrypt(new RSAEncrypter((RSAPublicKey) cypherCred.getValue()));
    }

    @SuppressWarnings("Duplicates")
    private void given_the_access_claims_are_cyphered_with_mismatch_issuer_in_header() throws JOSEException {
        SignedJWT signature = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).
                customParam(ISSUER.getValue(), randomString())
                .build(), accessClaims);
        signature.sign(new RSASSASigner(cypherCred.getKey()));
        final JWEHeader header = new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A128GCM).build();
        cyphered = new JWEObject(header, new Payload(signature));
        cyphered.encrypt(new RSAEncrypter((RSAPublicKey) cypherCred.getValue()));
    }

    private Pair<PrivateKey, RSAPublicKey> given_wrong_Credential() {
        final Pair<PrivateKey, X509Certificate> credential = KeysForTests.generateRSACredential();
        return new Pair<>(credential.getKey(), (RSAPublicKey) credential.getValue().getPublicKey());
    }

    private void when_decipher_the_access_token() {
        accessToken = new JOSEAccessToken(cyphered.serialize(), decipherCred.getKey(), decipherCred.getValue());
        accessToken.decipher();
    }
}
