package com.primasolutions.idp.token.jose;

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
import com.primasolutions.idp.constants.TokenProcessingStatus;
import com.primasolutions.idp.constants.TokenRefreshMode;
import com.primasolutions.idp.constants.TokenScope;
import com.primasolutions.idp.constants.TokenValidationMode;
import com.primasolutions.idp.constants.TokenVerdict;
import com.primasolutions.idp.exception.InvalidTokenException;
import com.primasolutions.idp.exception.JOSEErrorException;
import com.primasolutions.idp.exception.TokenUnmodifiableException;
import com.primasolutions.idp.token.Claims;
import com.primasolutions.idp.tools.KeysForTests;
import com.primasolutions.idp.tools.RandomForTests;
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

import static com.primasolutions.idp.constants.TokenRefreshMode.EACH_TIME;
import static com.primasolutions.idp.constants.TokenRefreshMode.WHEN_EXPIRES;
import static com.primasolutions.idp.constants.TokenValidationMode.STANDARD;
import static com.primasolutions.idp.constants.TokenValidationMode.STRICT;
import static com.primasolutions.idp.constants.TokenVerdict.FAIL;
import static com.primasolutions.idp.constants.TokenVerdict.SUCCESS;
import static com.primasolutions.idp.token.Claims.BP;
import static com.primasolutions.idp.token.Claims.DELEGATE;
import static com.primasolutions.idp.token.Claims.DELEGATOR;
import static com.primasolutions.idp.token.Claims.ISSUER;
import static com.primasolutions.idp.token.Claims.REFRESH_MODE;
import static com.primasolutions.idp.token.Claims.SCOPES;
import static com.primasolutions.idp.token.Claims.STATE;
import static com.primasolutions.idp.token.Claims.TARGET_URL;
import static com.primasolutions.idp.token.Claims.VALIDATION_MODE;
import static com.primasolutions.idp.token.Claims.VERDICT;
import static org.exparity.hamcrest.date.LocalDateTimeMatchers.within;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class JOSECypheredRefreshTokenTest {

    public static final int THREE_MINUTES = 1000 * 60 * 3;
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
    public void when_decipher_the_access_token_then_the_refresh_mode_is_loaded() {
        when_decipher_the_refresh_token();
        assertThat(refreshToken.getRefreshMode(), equalTo(refreshClaims.getClaim(REFRESH_MODE.getValue())));
    }

    @Test
    public void when_decipher_the_access_token_then_the_validation_mode_is_loaded() {
        when_decipher_the_refresh_token();
        assertThat(refreshToken.getValidationMode(), equalTo(refreshClaims.getClaim(VALIDATION_MODE.getValue())));
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
        new JOSERefreshToken(RandomForTests.randomString(), decipherCred.getKey(), decipherCred.getValue());
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
    public void the_validation_mode_is_not_modifiable_before_deciphering_the_refresh() {
        refreshToken = new JOSERefreshToken(cyphered.serialize(), decipherCred.getKey(), decipherCred.getValue());
        refreshToken.setValidationMode(TokenValidationMode.STANDARD);
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_validation_mode_is_not_modifiable_after_deciphering_the_refresh() {
        when_decipher_the_refresh_token();
        refreshToken.setValidationMode(TokenValidationMode.STRICT);
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_refresh_mode_is_not_modifiable_before_deciphering_the_refresh() {
        refreshToken = new JOSERefreshToken(cyphered.serialize(), decipherCred.getKey(), decipherCred.getValue());
        refreshToken.setRefreshMode(TokenRefreshMode.EACH_TIME);
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_refresh_mode_is_not_modifiable_after_deciphering_the_refresh() {
        when_decipher_the_refresh_token();
        refreshToken.setRefreshMode(TokenRefreshMode.WHEN_EXPIRES);
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_stamp_is_not_modifiable_before_deciphering_the_refresh() {
        refreshToken = new JOSERefreshToken(cyphered.serialize(), decipherCred.getKey(), decipherCred.getValue());
        refreshToken.setStamp(RandomForTests.randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_stamp_is_not_modifiable_after_deciphering_the_refresh() {
        when_decipher_the_refresh_token();
        refreshToken.setStamp(RandomForTests.randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_subject_is_not_modifiable_before_deciphing_the_refresh() {
        refreshToken = new JOSERefreshToken(cyphered.serialize(), decipherCred.getKey(), decipherCred.getValue());
        refreshToken.setSubject(RandomForTests.randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_subject_is_not_modifiable_after_deciphering_the_refresh() {
        when_decipher_the_refresh_token();
        refreshToken.setSubject(RandomForTests.randomString());
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
        refreshToken.setBP(RandomForTests.randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_bp_is_not_modifiable_after_deciphering_the_refresh() {
        when_decipher_the_refresh_token();
        refreshToken.setBP(RandomForTests.randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_state_is_not_modifiable_before_deciphering_the_refresh() {
        refreshToken = new JOSERefreshToken(cyphered.serialize(), decipherCred.getKey(), decipherCred.getValue());
        refreshToken.setState(RandomForTests.randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_state_is_not_modifiable_after_deciphering_the_refresh() {
        when_decipher_the_refresh_token();
        refreshToken.setState(RandomForTests.randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_issuer_is_not_modifiable_before_deciphering_the_refresh() {
        refreshToken = new JOSERefreshToken(cyphered.serialize(), decipherCred.getKey(), decipherCred.getValue());
        refreshToken.setIssuer(RandomForTests.randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_issuer_is_not_modifiable_after_deciphering_the_refresh() {
        when_decipher_the_refresh_token();
        refreshToken.setIssuer(RandomForTests.randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_audience_is_not_modifiable_before_deciphering_the_refresh() {
        refreshToken = new JOSERefreshToken(cyphered.serialize(), decipherCred.getKey(), decipherCred.getValue());
        refreshToken.setAudience(RandomForTests.randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_audience_is_not_modifiable_after_deciphering_the_refresh() {
        when_decipher_the_refresh_token();
        refreshToken.setAudience(RandomForTests.randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_target_url_is_not_modifiable_before_deciphering_the_refresh() {
        refreshToken = new JOSERefreshToken(cyphered.serialize(), decipherCred.getKey(), decipherCred.getValue());
        refreshToken.setTargetURL(RandomForTests.randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_target_url_is_not_modifiable_after_deciphering_the_refresh() {
        when_decipher_the_refresh_token();
        refreshToken.setTargetURL(RandomForTests.randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_delegator_is_not_modifiable_before_deciphering_the_refresh() {
        refreshToken = new JOSERefreshToken(cyphered.serialize(), decipherCred.getKey(), decipherCred.getValue());
        refreshToken.setDelegator(RandomForTests.randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_delegator_is_not_modifiable_after_deciphering_the_refresh() {
        when_decipher_the_refresh_token();
        refreshToken.setDelegator(RandomForTests.randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_delegate_is_not_modifiable_before_deciphering_the_refresh() {
        refreshToken = new JOSERefreshToken(cyphered.serialize(), decipherCred.getKey(), decipherCred.getValue());
        refreshToken.setDelegate(RandomForTests.randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_delegate_is_not_modifiable_after_deciphering_the_refresh() {
        when_decipher_the_refresh_token();
        refreshToken.setDelegate(RandomForTests.randomString());
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
        final Random random = new Random();
        refreshClaims = new JWTClaimsSet.Builder()
                .jwtID(UUID.randomUUID().toString())
                .subject(RandomForTests.randomString())
                .claim(SCOPES.getValue(), Arrays.stream(TokenScope.values()).map(TokenScope::name)
                        .collect(Collectors.joining("|")))
                .claim(Claims.BP.getValue(), RandomForTests.randomString())
                .claim(STATE.getValue(), RandomForTests.randomString())
                .issuer(RandomForTests.randomString())
                .audience(RandomForTests.randomString())
                .claim(Claims.TARGET_URL.getValue(), RandomForTests.randomString())
                .claim(Claims.DELEGATOR.getValue(), RandomForTests.randomString())
                .claim(Claims.DELEGATE.getValue(), RandomForTests.randomString())
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
                customParam(ISSUER.getValue(), RandomForTests.randomString())
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
