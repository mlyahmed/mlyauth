package com.hohou.federation.idp.token.jose;

import com.hohou.federation.idp.constants.TokenProcessingStatus;
import com.hohou.federation.idp.constants.TokenRefreshMode;
import com.hohou.federation.idp.constants.TokenScope;
import com.hohou.federation.idp.constants.TokenValidationMode;
import com.hohou.federation.idp.constants.TokenVerdict;
import com.hohou.federation.idp.credentials.CredentialsPair;
import com.hohou.federation.idp.exception.InvalidTokenExc;
import com.hohou.federation.idp.exception.JOSEErrorExc;
import com.hohou.federation.idp.exception.TokenUnmodifiableExc;
import com.hohou.federation.idp.token.Claims;
import com.hohou.federation.idp.tools.KeysForTests;
import com.hohou.federation.idp.tools.RandomForTests;
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
import org.junit.Before;
import org.junit.Test;

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

import static com.hohou.federation.idp.constants.TokenVerdict.FAIL;
import static com.hohou.federation.idp.constants.TokenVerdict.SUCCESS;
import static org.exparity.hamcrest.date.LocalDateTimeMatchers.within;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class JOSECypheredRefreshTokenTest {

    private static final int THREE_MINUTES = 1000 * 60 * 3;
    private CredentialsPair cypherCred;
    private CredentialsPair decipherCred;
    private JOSERefreshToken refreshToken;
    private JWTClaimsSet refreshClaims;
    private JWEObject cyphered;

    @Before
    public void setup() throws JOSEException {
        final CredentialsPair peerCred = KeysForTests.generateRSACredential();
        final CredentialsPair localCred = KeysForTests.generateRSACredential();
        cypherCred = new CredentialsPair(localCred.getPrivateKey(), peerCred.getCertificate());
        decipherCred = new CredentialsPair(peerCred.getPrivateKey(), localCred.getCertificate());
        given_expected_refresh_claims();
        given_the_refresh_claims_are_cyphered();
    }

    @Test
    public void the_refresh_token_status_must_be_cyphered() {
        refreshToken = new JOSERefreshToken(cyphered.serialize(), decipherCred.getPrivateKey(),
                decipherCred.getPublicKey());
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
        assertThat(refreshToken.getRefreshMode(), equalTo(refreshClaims.getClaim(Claims.REFRESH_MODE.getValue())));
    }

    @Test
    public void when_decipher_the_access_token_then_the_validation_mode_is_loaded() {
        when_decipher_the_refresh_token();
        assertThat(refreshToken.getValidationMode(), equalTo(refreshClaims.getClaim(Claims.VALIDATION_MODE.getValue())));
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
        assertThat(refreshToken.getBP(), equalTo(refreshClaims.getClaim(Claims.BP.getValue())));
    }

    @Test
    public void when_decipher_the_refresh_token_then_the_scopes_are_loaded() {
        when_decipher_the_refresh_token();
        assertThat(refreshClaims.getClaim(Claims.SCOPES.getValue()), notNullValue());
        assertThat(refreshToken.getScopes(), equalTo(Arrays.stream(refreshClaims.getClaim(Claims.SCOPES.getValue())
                .toString().split("\\|")).map(TokenScope::valueOf).collect(Collectors.toSet())));
    }

    @Test
    public void when_decipher_the_access_token_then_the_issuer_is_loaded() {
        when_decipher_the_refresh_token();
        assertThat(refreshToken.getIssuer(), equalTo(refreshClaims.getIssuer()));
    }

    @Test(expected = InvalidTokenExc.class)
    public void when_the_refresh_issuer_in_header_and_in_claims_are_different_then_error() throws JOSEException {
        given_the_refresh_claims_are_cyphered_with_mismatch_issuer_in_header();
        when_decipher_the_refresh_token();
    }

    @Test
    public void when_decipher_the_refresh_token_then_the_state_is_loaded() {
        when_decipher_the_refresh_token();
        assertThat(refreshToken.getState(), equalTo(refreshClaims.getClaim(Claims.STATE.getValue())));
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
        assertThat(refreshToken.getTargetURL(), equalTo(refreshClaims.getClaim(Claims.TARGET_URL.getValue())));
    }

    @Test
    public void when_decipher_the_refresh_token_then_the_delegator_is_loaded() {
        when_decipher_the_refresh_token();
        assertThat(refreshToken.getDelegator(), equalTo(refreshClaims.getClaim(Claims.DELEGATOR.getValue())));
    }

    @Test
    public void when_decipher_the_refresh_token_then_the_delegate_is_loaded() {
        when_decipher_the_refresh_token();
        assertThat(refreshToken.getDelegate(), equalTo(refreshClaims.getClaim(Claims.DELEGATE.getValue())));
    }

    @Test
    public void when_decipher_the_refresh_token_then_the_verdict_is_loaded() {
        when_decipher_the_refresh_token();
        assertThat(refreshToken.getVerdict(), equalTo(refreshClaims.getClaim(Claims.VERDICT.getValue())));
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

    @Test(expected = JOSEErrorExc.class)
    public void when_the_refresh_decryption_key_does_not_match_then_error() {
        CredentialsPair wrongCred = given_wrong_Credential();
        refreshToken = new JOSERefreshToken(cyphered.serialize(), wrongCred.getPrivateKey(),
                decipherCred.getPublicKey());
        refreshToken.decipher();
    }

    @Test(expected = JOSEErrorExc.class)
    public void when_the_refresh_signature_key_does_not_match_then_error() {
        CredentialsPair wrongCred = given_wrong_Credential();
        refreshToken = new JOSERefreshToken(cyphered.serialize(), decipherCred.getPrivateKey(),
                wrongCred.getPublicKey());
        refreshToken.decipher();
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_the_cyphered_refresh_token_is_null_then_error() {
        new JOSERefreshToken(null, decipherCred.getPrivateKey(), decipherCred.getPublicKey());
    }

    @Test(expected = JOSEErrorExc.class)
    public void when_the_cyphered_refresh_token_is_not_well_formatted_then_error() {
        new JOSERefreshToken(RandomForTests.randomString(), decipherCred.getPrivateKey(), decipherCred.getPublicKey());
    }

    @Test(expected = JOSEErrorExc.class)
    public void when_the_refresh_token_is_not_signed_then_error() throws JOSEException {
        final JWEHeader tokenHeader = new JWEHeader(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A128GCM);
        EncryptedJWT tokenHolder = new EncryptedJWT(tokenHeader, refreshClaims);
        tokenHolder.encrypt(new RSAEncrypter((RSAPublicKey) cypherCred.getPublicKey()));
        refreshToken = new JOSERefreshToken(tokenHolder.serialize(), decipherCred.getPrivateKey(),
                decipherCred.getPublicKey());
        refreshToken.decipher();
    }

    @Test(expected = JOSEErrorExc.class)
    public void when_the_refresh_token_is_signed_but_not_encrypted_then_error() throws JOSEException {
        SignedJWT tokenSigned = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), refreshClaims);
        tokenSigned.sign(new RSASSASigner(cypherCred.getPrivateKey()));
        refreshToken = new JOSERefreshToken(tokenSigned.serialize(), decipherCred.getPrivateKey(),
                decipherCred.getPublicKey());
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_the_refresh_private_key_is_null_then_error() {
        new JOSERefreshToken(cyphered.serialize(), null, decipherCred.getPublicKey());
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_the_refresh_public_key_is_null_then_error() {
        new JOSERefreshToken(cyphered.serialize(), decipherCred.getPrivateKey(), null);
    }

    @Test(expected = TokenUnmodifiableExc.class)
    public void the_validation_mode_is_not_modifiable_before_deciphering_the_refresh() {
        refreshToken = new JOSERefreshToken(cyphered.serialize(), decipherCred.getPrivateKey(),
                decipherCred.getPublicKey());
        refreshToken.setValidationMode(TokenValidationMode.STANDARD);
    }

    @Test(expected = TokenUnmodifiableExc.class)
    public void the_validation_mode_is_not_modifiable_after_deciphering_the_refresh() {
        when_decipher_the_refresh_token();
        refreshToken.setValidationMode(TokenValidationMode.STRICT);
    }

    @Test(expected = TokenUnmodifiableExc.class)
    public void the_refresh_mode_is_not_modifiable_before_deciphering_the_refresh() {
        refreshToken = new JOSERefreshToken(cyphered.serialize(), decipherCred.getPrivateKey(),
                decipherCred.getPublicKey());
        refreshToken.setRefreshMode(TokenRefreshMode.EACH_TIME);
    }

    @Test(expected = TokenUnmodifiableExc.class)
    public void the_refresh_mode_is_not_modifiable_after_deciphering_the_refresh() {
        when_decipher_the_refresh_token();
        refreshToken.setRefreshMode(TokenRefreshMode.WHEN_EXPIRES);
    }

    @Test(expected = TokenUnmodifiableExc.class)
    public void the_stamp_is_not_modifiable_before_deciphering_the_refresh() {
        refreshToken = new JOSERefreshToken(cyphered.serialize(), decipherCred.getPrivateKey(),
                decipherCred.getPublicKey());
        refreshToken.setStamp(RandomForTests.randomString());
    }

    @Test(expected = TokenUnmodifiableExc.class)
    public void the_stamp_is_not_modifiable_after_deciphering_the_refresh() {
        when_decipher_the_refresh_token();
        refreshToken.setStamp(RandomForTests.randomString());
    }

    @Test(expected = TokenUnmodifiableExc.class)
    public void the_subject_is_not_modifiable_before_deciphing_the_refresh() {
        refreshToken = new JOSERefreshToken(cyphered.serialize(), decipherCred.getPrivateKey(),
                decipherCred.getPublicKey());
        refreshToken.setSubject(RandomForTests.randomString());
    }

    @Test(expected = TokenUnmodifiableExc.class)
    public void the_subject_is_not_modifiable_after_deciphering_the_refresh() {
        when_decipher_the_refresh_token();
        refreshToken.setSubject(RandomForTests.randomString());
    }

    @Test(expected = TokenUnmodifiableExc.class)
    public void the_scopes_are_not_modifiable_before_deciphing_the_refresh() {
        refreshToken = new JOSERefreshToken(cyphered.serialize(), decipherCred.getPrivateKey(),
                decipherCred.getPublicKey());
        refreshToken.setScopes(new HashSet<>(Arrays.asList(TokenScope.values())));
    }

    @Test(expected = TokenUnmodifiableExc.class)
    public void the_scopes_are_not_modifiable_after_deciphering_the_refresh() {
        when_decipher_the_refresh_token();
        refreshToken.setScopes(new HashSet<>(Arrays.asList(TokenScope.values())));
    }

    @Test(expected = TokenUnmodifiableExc.class)
    public void the_bp_is_not_modifiable_before_deciphering_the_refresh() {
        refreshToken = new JOSERefreshToken(cyphered.serialize(), decipherCred.getPrivateKey(),
                decipherCred.getPublicKey());
        refreshToken.setBP(RandomForTests.randomString());
    }

    @Test(expected = TokenUnmodifiableExc.class)
    public void the_bp_is_not_modifiable_after_deciphering_the_refresh() {
        when_decipher_the_refresh_token();
        refreshToken.setBP(RandomForTests.randomString());
    }

    @Test(expected = TokenUnmodifiableExc.class)
    public void the_state_is_not_modifiable_before_deciphering_the_refresh() {
        refreshToken = new JOSERefreshToken(cyphered.serialize(), decipherCred.getPrivateKey(),
                decipherCred.getPublicKey());
        refreshToken.setState(RandomForTests.randomString());
    }

    @Test(expected = TokenUnmodifiableExc.class)
    public void the_state_is_not_modifiable_after_deciphering_the_refresh() {
        when_decipher_the_refresh_token();
        refreshToken.setState(RandomForTests.randomString());
    }

    @Test(expected = TokenUnmodifiableExc.class)
    public void the_issuer_is_not_modifiable_before_deciphering_the_refresh() {
        refreshToken = new JOSERefreshToken(cyphered.serialize(), decipherCred.getPrivateKey(),
                decipherCred.getPublicKey());
        refreshToken.setIssuer(RandomForTests.randomString());
    }

    @Test(expected = TokenUnmodifiableExc.class)
    public void the_issuer_is_not_modifiable_after_deciphering_the_refresh() {
        when_decipher_the_refresh_token();
        refreshToken.setIssuer(RandomForTests.randomString());
    }

    @Test(expected = TokenUnmodifiableExc.class)
    public void the_audience_is_not_modifiable_before_deciphering_the_refresh() {
        refreshToken = new JOSERefreshToken(cyphered.serialize(), decipherCred.getPrivateKey(),
                decipherCred.getPublicKey());
        refreshToken.setAudience(RandomForTests.randomString());
    }

    @Test(expected = TokenUnmodifiableExc.class)
    public void the_audience_is_not_modifiable_after_deciphering_the_refresh() {
        when_decipher_the_refresh_token();
        refreshToken.setAudience(RandomForTests.randomString());
    }

    @Test(expected = TokenUnmodifiableExc.class)
    public void the_target_url_is_not_modifiable_before_deciphering_the_refresh() {
        refreshToken = new JOSERefreshToken(cyphered.serialize(), decipherCred.getPrivateKey(),
                decipherCred.getPublicKey());
        refreshToken.setTargetURL(RandomForTests.randomString());
    }

    @Test(expected = TokenUnmodifiableExc.class)
    public void the_target_url_is_not_modifiable_after_deciphering_the_refresh() {
        when_decipher_the_refresh_token();
        refreshToken.setTargetURL(RandomForTests.randomString());
    }

    @Test(expected = TokenUnmodifiableExc.class)
    public void the_delegator_is_not_modifiable_before_deciphering_the_refresh() {
        refreshToken = new JOSERefreshToken(cyphered.serialize(), decipherCred.getPrivateKey(),
                decipherCred.getPublicKey());
        refreshToken.setDelegator(RandomForTests.randomString());
    }

    @Test(expected = TokenUnmodifiableExc.class)
    public void the_delegator_is_not_modifiable_after_deciphering_the_refresh() {
        when_decipher_the_refresh_token();
        refreshToken.setDelegator(RandomForTests.randomString());
    }

    @Test(expected = TokenUnmodifiableExc.class)
    public void the_delegate_is_not_modifiable_before_deciphering_the_refresh() {
        refreshToken = new JOSERefreshToken(cyphered.serialize(), decipherCred.getPrivateKey(),
                decipherCred.getPublicKey());
        refreshToken.setDelegate(RandomForTests.randomString());
    }

    @Test(expected = TokenUnmodifiableExc.class)
    public void the_delegate_is_not_modifiable_after_deciphering_the_refresh() {
        when_decipher_the_refresh_token();
        refreshToken.setDelegate(RandomForTests.randomString());
    }

    @Test(expected = TokenUnmodifiableExc.class)
    public void the_verdict_is_not_modifiable_before_deciphering_the_refresh() {
        refreshToken = new JOSERefreshToken(cyphered.serialize(), decipherCred.getPrivateKey(),
                decipherCred.getPublicKey());
        refreshToken.setVerdict(TokenVerdict.SUCCESS);
    }

    @Test(expected = TokenUnmodifiableExc.class)
    public void the_verdict_is_not_modifiable_after_deciphering_the_refresh() {
        when_decipher_the_refresh_token();
        refreshToken.setVerdict(TokenVerdict.SUCCESS);
    }

    private void given_expected_refresh_claims() {
        final Random random = new Random();
        refreshClaims = new JWTClaimsSet.Builder()
                .jwtID(UUID.randomUUID().toString())
                .subject(RandomForTests.randomString())
                .claim(Claims.SCOPES.getValue(), Arrays.stream(TokenScope.values()).map(TokenScope::name)
                        .collect(Collectors.joining("|")))
                .claim(Claims.BP.getValue(), RandomForTests.randomString())
                .claim(Claims.STATE.getValue(), RandomForTests.randomString())
                .issuer(RandomForTests.randomString())
                .audience(RandomForTests.randomString())
                .claim(Claims.TARGET_URL.getValue(), RandomForTests.randomString())
                .claim(Claims.DELEGATOR.getValue(), RandomForTests.randomString())
                .claim(Claims.DELEGATE.getValue(), RandomForTests.randomString())
                //CHECKSTYLE:OFF
                .claim(Claims.VERDICT.getValue(), random.nextInt(2000) % 2 == 0 ? SUCCESS : FAIL)
                .claim(Claims.VALIDATION_MODE.getValue(), random.nextInt(542154) % 2 == 0 ? TokenValidationMode.STANDARD : TokenValidationMode.STRICT)
                .claim(Claims.REFRESH_MODE.getValue(), random.nextInt(54662)%2 == 0 ? TokenRefreshMode.WHEN_EXPIRES : TokenRefreshMode.EACH_TIME)
                //CHECKSTYLE:ON
                .expirationTime(new Date(System.currentTimeMillis() + THREE_MINUTES))
                .notBeforeTime(new Date())
                .issueTime(new Date())
                .build();
    }

    @SuppressWarnings("Duplicates")
    private void given_the_refresh_claims_are_cyphered() throws JOSEException {
        final JWSHeader signatureHeader = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .customParam(Claims.ISSUER.getValue(), refreshClaims.getIssuer())
                .build();
        SignedJWT signature = new SignedJWT(signatureHeader, refreshClaims);
        signature.sign(new RSASSASigner(cypherCred.getPrivateKey()));
        final JWEHeader header = new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A128GCM).build();
        cyphered = new JWEObject(header, new Payload(signature));
        cyphered.encrypt(new RSAEncrypter((RSAPublicKey) cypherCred.getPublicKey()));
    }

    @SuppressWarnings("Duplicates")
    private void given_the_refresh_claims_are_cyphered_with_mismatch_issuer_in_header() throws JOSEException {
        SignedJWT signature = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).
                customParam(Claims.ISSUER.getValue(), RandomForTests.randomString())
                .build(), refreshClaims);
        signature.sign(new RSASSASigner(cypherCred.getPrivateKey()));
        final JWEHeader header = new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A128GCM).build();
        cyphered = new JWEObject(header, new Payload(signature));
        cyphered.encrypt(new RSAEncrypter((RSAPublicKey) cypherCred.getPublicKey()));
    }

    private CredentialsPair given_wrong_Credential() {
        return KeysForTests.generateRSACredential();
    }

    private void when_decipher_the_refresh_token() {
        refreshToken = new JOSERefreshToken(cyphered.serialize(), decipherCred.getPrivateKey(),
                decipherCred.getPublicKey());
        refreshToken.decipher();
    }

}
