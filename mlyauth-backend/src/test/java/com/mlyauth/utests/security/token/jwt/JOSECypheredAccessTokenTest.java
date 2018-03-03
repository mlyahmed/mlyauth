package com.mlyauth.utests.security.token.jwt;

import com.mlyauth.constants.TokenScope;
import com.mlyauth.constants.TokenStatus;
import com.mlyauth.constants.TokenVerdict;
import com.mlyauth.exception.JOSEErrorException;
import com.mlyauth.exception.TokenUnmodifiableException;
import com.mlyauth.security.token.ExtraClaims;
import com.mlyauth.security.token.jwt.JOSEAccessToken;
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
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.mlyauth.security.token.ExtraClaims.*;
import static org.exparity.hamcrest.date.LocalDateTimeMatchers.within;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class JOSECypheredAccessTokenTest {

    private Pair<PrivateKey, RSAPublicKey> cypherCred;
    private Pair<PrivateKey, RSAPublicKey> decipherCred;
    private JOSEAccessToken token;
    private JWTClaimsSet expectedClaims;
    private JWEObject tokenEncrypted;

    @Before
    public void setup() throws JOSEException {
        final Pair<PrivateKey, X509Certificate> peerCred = KeysForTests.generateRSACredential();
        final Pair<PrivateKey, X509Certificate> localCred = KeysForTests.generateRSACredential();
        cypherCred = new Pair<>(localCred.getKey(), (RSAPublicKey) peerCred.getValue().getPublicKey());
        decipherCred = new Pair<>(peerCred.getKey(), (RSAPublicKey) localCred.getValue().getPublicKey());
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
    public void when_given_cyphered_token_then_the_id_is_loaded() {
        when_decipher_the_token();
        assertThat(token.getId(), equalTo(expectedClaims.getJWTID()));
    }

    @Test
    public void when_given_cyphered_token_then_the_subject_is_loaded() {
        when_decipher_the_token();
        assertThat(token.getId(), equalTo(expectedClaims.getJWTID()));
        assertThat(token.getSubject(), equalTo(expectedClaims.getSubject()));
    }

    @Test
    public void when_given_cyphered_token_then_the_scopes_are_loaded() {
        when_decipher_the_token();
        assertThat(token.getBP(), equalTo(expectedClaims.getClaim(BP.getValue())));
    }

    @Test
    public void when_given_cyphered_token_then_the_bp_is_loaded() {
        when_decipher_the_token();
        assertThat(expectedClaims.getClaim(SCOPES.getValue()), notNullValue());
        assertThat(token.getScopes(), equalTo(Arrays.stream(expectedClaims.getClaim(SCOPES.getValue())
                .toString().split("\\|")).map(TokenScope::valueOf).collect(Collectors.toSet())));
    }

    @Test
    public void when_given_cyphered_token_then_the_state_is_loaded() {
        when_decipher_the_token();
        assertThat(token.getIssuer(), equalTo(expectedClaims.getIssuer()));
    }

    @Test
    public void when_given_cyphered_token_then_the_issuer_is_loaded() {
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
        tokenHolder.encrypt(new RSAEncrypter(cypherCred.getValue()));
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
    public void the_id_is_not_modifiable_before_decipher() {
        token = new JOSEAccessToken(tokenEncrypted.serialize(), decipherCred.getKey(), decipherCred.getValue());
        token.setId(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_id_is_not_modifiable_after_decipher() {
        when_decipher_the_token();
        token.setId(randomString());
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

    private void given_expected_claims() {
        expectedClaims = new JWTClaimsSet.Builder()
                .jwtID(UUID.randomUUID().toString())
                .subject(randomString())
                .claim(SCOPES.getValue(), Arrays.stream(TokenScope.values()).map(TokenScope::name)
                        .collect(Collectors.joining("|")))
                .claim(ExtraClaims.BP.getValue(), randomString())
                .claim(STATE.getValue(), randomString())
                .issuer(randomString())
                .audience(randomString())
                .claim(ExtraClaims.TARGET_URL.getValue(), randomString())
                .claim(ExtraClaims.DELEGATOR.getValue(), randomString())
                .claim(ExtraClaims.DELEGATE.getValue(), randomString())
                .claim(ExtraClaims.VERDICT.getValue(), TokenVerdict.SUCCESS)
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
        SignedJWT tokenSigned = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), expectedClaims);
        tokenSigned.sign(new RSASSASigner(cypherCred.getKey()));
        final JWEHeader header = new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A128GCM).build();
        tokenEncrypted = new JWEObject(header, new Payload(tokenSigned));
        tokenEncrypted.encrypt(new RSAEncrypter(cypherCred.getValue()));
    }

    private static String randomString() {
        final int length = (new Random()).nextInt(50);
        return RandomStringUtils.random(length > 0 ? length : 50, true, true);
    }
}
