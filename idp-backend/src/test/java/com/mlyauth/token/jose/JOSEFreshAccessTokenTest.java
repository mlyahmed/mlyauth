package com.mlyauth.token.jose;

import com.mlyauth.constants.*;
import com.mlyauth.exception.TokenNotCipheredException;
import com.mlyauth.exception.TokenUnmodifiableException;
import com.mlyauth.token.Claims;
import com.mlyauth.tools.KeysForTests;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.SignedJWT;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import javafx.util.Pair;
import org.exparity.hamcrest.date.DateMatchers;
import org.exparity.hamcrest.date.LocalDateTimeMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import static com.mlyauth.constants.TokenRefreshMode.WHEN_EXPIRES;
import static com.mlyauth.constants.TokenScope.*;
import static com.mlyauth.constants.TokenValidationMode.STANDARD;
import static com.mlyauth.constants.TokenValidationMode.STRICT;
import static com.mlyauth.constants.TokenVerdict.FAIL;
import static com.mlyauth.constants.TokenVerdict.SUCCESS;
import static com.mlyauth.token.Claims.*;
import static com.mlyauth.tools.RandomForTests.randomString;
import static java.lang.System.currentTimeMillis;
import static java.time.LocalDateTime.now;
import static java.util.Date.from;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(DataProviderRunner.class)
public class JOSEFreshAccessTokenTest {

    private JOSEAccessToken accessToken;
    private Pair<PrivateKey, RSAPublicKey> cypherCred;
    private Pair<PrivateKey, RSAPublicKey> decipherCred;

    @Before
    public void setup() {
        final Pair<PrivateKey, X509Certificate> peerCred = KeysForTests.generateRSACredential();
        final Pair<PrivateKey, X509Certificate> localCred = KeysForTests.generateRSACredential();
        cypherCred = new Pair<>(localCred.getKey(), (RSAPublicKey) peerCred.getValue().getPublicKey());
        decipherCred = new Pair<>(peerCred.getKey(), (RSAPublicKey) localCred.getValue().getPublicKey());
        accessToken = new JOSEAccessToken(cypherCred.getKey(), cypherCred.getValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_create_a_fresh_access_token_and_private_key_is_null_then_error() {
        final Pair<PrivateKey, X509Certificate> credential = KeysForTests.generateRSACredential();
        new JOSEAccessToken(null, credential.getValue().getPublicKey());
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_create_a_fresh_access_token_and_public_key_is_null_then_error() {
        final Pair<PrivateKey, X509Certificate> credential = KeysForTests.generateRSACredential();
        new JOSEAccessToken(credential.getKey(), null);
    }

    @Test
    public void when_create_fresh_access_response_then_token_claims_must_be_fresh() {
        assertThat(accessToken.getRefreshMode(), equalTo(TokenRefreshMode.EACH_TIME));
        assertThat(accessToken.getValidationMode(), equalTo(STRICT));
        assertThat(accessToken.getStamp(), nullValue());
        assertThat(accessToken.getSubject(), nullValue());
        assertThat(accessToken.getScopes(), empty());
        assertThat(accessToken.getBP(), nullValue());
        assertThat(accessToken.getState(), nullValue());
        assertThat(accessToken.getIssuer(), nullValue());
        assertThat(accessToken.getAudience(), nullValue());
        assertThat(accessToken.getDelegator(), nullValue());
        assertThat(accessToken.getDelegate(), nullValue());
        assertThat(accessToken.getVerdict(), nullValue());
        assertThat(accessToken.getNorm(), equalTo(TokenNorm.JOSE));
        assertThat(accessToken.getType(), equalTo(TokenType.ACCESS));
        assertThat(accessToken.getStatus(), equalTo(TokenProcessingStatus.FRESH));
        assertThat(accessToken.getExpiryTime(), nullValue());
        assertThat(accessToken.getEffectiveTime(), nullValue());
        assertThat(accessToken.getIssuanceTime(), nullValue());
    }

    @Test
    public void when_create_a_fresh_access_token_and_set_refresh_mode_then_must_be_set(){
        accessToken.setRefreshMode(WHEN_EXPIRES);
        assertThat(accessToken.getRefreshMode(), equalTo(WHEN_EXPIRES));
        assertThat(accessToken.getStatus(), equalTo(TokenProcessingStatus.FORGED));
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_create_a_fresh_access_token_and_set_null_as_refresh_mode_then_error(){
        accessToken.setRefreshMode(null);
    }

    @Test
    @SuppressWarnings("Duplicates")
    public void when_serialize_cyphered_access_token_then_the_refresh_mode_must_be_committed() throws Exception {
        accessToken.setRefreshMode(WHEN_EXPIRES);
        accessToken.cypher();
        JWEObject loadedToken = JWEObject.parse(accessToken.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        assertThat(signedJWT.getJWTClaimsSet().getClaim(REFRESH_MODE.getValue()), equalTo(WHEN_EXPIRES.name()));
    }

    @Test
    public void when_create_a_fresh_access_token_and_set_validation_mode_then_must_be_set(){
        accessToken.setValidationMode(STANDARD);
        assertThat(accessToken.getValidationMode(), equalTo(STANDARD));
        assertThat(accessToken.getStatus(), equalTo(TokenProcessingStatus.FORGED));
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_create_a_fresh_access_token_and_set_null_as_validation_mode_then_error(){
        accessToken.setValidationMode(null);
    }

    @Test
    @SuppressWarnings("Duplicates")
    public void when_serialize_cyphered_access_token_then_the_validation_mode_must_be_committed() throws Exception {
        accessToken.setValidationMode(STANDARD);
        accessToken.cypher();
        JWEObject loadedToken = JWEObject.parse(accessToken.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        assertThat(signedJWT.getJWTClaimsSet().getClaim(VALIDATION_MODE.getValue()), equalTo(STANDARD.name()));
    }

    @Test
    public void when_create_a_fresh_access_token_and_set_stamp_then_must_be_set() {
        String id = randomString();
        accessToken.setStamp(id);
        assertThat(accessToken.getStamp(), equalTo(id));
        assertThat(accessToken.getStatus(), equalTo(TokenProcessingStatus.FORGED));
    }

    @Test
    @SuppressWarnings("Duplicates")
    public void when_serialize_cyphered_access_token_then_the_stamp_must_be_committed() throws Exception {
        final String id = randomString();
        accessToken.setStamp(id);
        accessToken.cypher();
        JWEObject loadedToken = JWEObject.parse(accessToken.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        assertThat(signedJWT.getJWTClaimsSet().getJWTID(), equalTo(id));
    }

    @Test
    public void when_create_a_fresh_access_token_and_set_subject_then_must_be_set() {
        String subject = randomString();
        accessToken.setSubject(subject);
        assertThat(accessToken.getSubject(), equalTo(subject));
        assertThat(accessToken.getStatus(), equalTo(TokenProcessingStatus.FORGED));
    }

    @Test
    @SuppressWarnings("Duplicates")
    public void when_serialize_cyphered_access_token_then_the_subject_must_be_committed() throws Exception {
        String subject = randomString();
        accessToken.setSubject(subject);
        accessToken.cypher();
        JWEObject loadedToken = JWEObject.parse(accessToken.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        assertThat(signedJWT.getJWTClaimsSet().getSubject(), equalTo(subject));
    }

    @DataProvider
    public static Object[][] accessScopes() {
        // @formatter:off
        return new Object[][]{
                {PROPOSAL.name(), POLICY.name(), CLAIM.name()},
                {PROPOSAL.name(), POLICY.name(), PERSON.name()},
                {POLICY.name(), CLAIM.name(), PERSON.name(), PROPOSAL.name()},
                {PROPOSAL.name()},
                {PERSON.name()},
        };
        // @formatter:on
    }

    @Test
    @UseDataProvider("accessScopes")
    public void when_create_a_fresh_access_token_and_set_scopes_then_they_must_be_set(String... scopesArray) {
        final Set<TokenScope> scopes = Arrays.stream(scopesArray).map(TokenScope::valueOf).collect(toSet());
        accessToken.setScopes(scopes);
        assertThat(accessToken.getScopes(), equalTo(scopes));
        assertThat(accessToken.getStatus(), equalTo(TokenProcessingStatus.FORGED));
    }

    @Test
    @UseDataProvider("accessScopes")
    @SuppressWarnings("Duplicates")
    public void when_serialize_cyphered_access_token_then_the_scopes_must_be_committed(String... scopesArray) throws Exception {
        final Set<TokenScope> scopes = Arrays.stream(scopesArray).map(TokenScope::valueOf).collect(toSet());
        accessToken.setScopes(scopes);
        accessToken.cypher();
        JWEObject loadedToken = JWEObject.parse(accessToken.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        assertThat(signedJWT.getJWTClaimsSet().getClaim(SCOPES.getValue()),
                equalTo(scopes.stream().map(TokenScope::name).collect(Collectors.joining("|"))));
    }

    @Test
    public void when_create_a_fresh_access_token_and_set_BP_then_must_be_set() {
        String bp = randomString();
        accessToken.setBP(bp);
        assertThat(accessToken.getBP(), equalTo(bp));
        assertThat(accessToken.getStatus(), equalTo(TokenProcessingStatus.FORGED));
    }

    @Test
    @SuppressWarnings("Duplicates")
    public void when_serialize_cyphered_access_token_then_the_BP_must_be_committed() throws Exception {
        String bp = randomString();
        accessToken.setBP(bp);
        accessToken.cypher();
        JWEObject loadedToken = JWEObject.parse(accessToken.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        assertThat(signedJWT.getJWTClaimsSet().getClaim(BP.getValue()), equalTo(bp));
    }

    @Test
    public void when_create_a_fresh_access_token_and_set_State_then_must_be_set() {
        String state = randomString();
        accessToken.setState(state);
        assertThat(accessToken.getState(), equalTo(state));
        assertThat(accessToken.getStatus(), equalTo(TokenProcessingStatus.FORGED));
    }

    @Test
    @SuppressWarnings("Duplicates")
    public void when_serialize_cyphered_access_token_then_the_State_must_be_committed() throws Exception {
        String state = randomString();
        accessToken.setState(state);
        accessToken.cypher();
        JWEObject loadedToken = JWEObject.parse(accessToken.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        assertThat(signedJWT.getJWTClaimsSet().getClaim(STATE.getValue()), equalTo(state));
    }

    @Test
    public void when_create_a_fresh_access_token_and_set_Issuer_then_must_be_set() {
        String issuer = randomString();
        accessToken.setIssuer(issuer);
        assertThat(accessToken.getIssuer(), equalTo(issuer));
        assertThat(accessToken.getStatus(), equalTo(TokenProcessingStatus.FORGED));
    }

    @Test
    @SuppressWarnings("Duplicates")
    public void when_serialize_cyphered_access_token_then_the_Issuer_must_be_committed() throws Exception {
        String issuer = randomString();
        accessToken.setIssuer(issuer);
        accessToken.cypher();
        JWEObject loadedToken = JWEObject.parse(accessToken.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        assertThat(signedJWT.getJWTClaimsSet().getIssuer(), equalTo(issuer));
        assertThat(signedJWT.getHeader().getCustomParam(Claims.ISSUER.getValue()), equalTo(issuer));
    }

    @Test
    public void when_create_a_fresh_access_token_and_set_Audience_then_must_be_set() {
        String audienceURI = randomString();
        accessToken.setAudience(audienceURI);
        assertThat(accessToken.getAudience(), equalTo(audienceURI));
        assertThat(accessToken.getStatus(), equalTo(TokenProcessingStatus.FORGED));
    }

    @Test
    @SuppressWarnings("Duplicates")
    public void when_serialize_cyphered_token_then_the_Audience_must_be_committed() throws Exception {
        String audienceURI = randomString();
        accessToken.setAudience(audienceURI);
        accessToken.cypher();
        JWEObject loadedToken = JWEObject.parse(accessToken.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        assertThat(signedJWT.getJWTClaimsSet().getAudience().get(0), equalTo(audienceURI));
    }

    @Test
    public void when_create_a_fresh_access_token_and_set_Target_URL_then_must_be_set() {
        String targetURL = randomString();
        accessToken.setTargetURL(targetURL);
        assertThat(accessToken.getTargetURL(), equalTo(targetURL));
        assertThat(accessToken.getStatus(), equalTo(TokenProcessingStatus.FORGED));
    }

    @Test
    @SuppressWarnings("Duplicates")
    public void when_serialize_cyphered_access_token_then_the_Target_URL_must_be_committed() throws Exception {
        String targetURL = randomString();
        accessToken.setTargetURL(targetURL);
        accessToken.cypher();
        JWEObject loadedToken = JWEObject.parse(accessToken.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        assertThat(signedJWT.getJWTClaimsSet().getClaim(TARGET_URL.getValue()), equalTo(targetURL));
    }

    @Test
    public void when_create_a_fresh_access_token_and_set_Delegator_then_must_be_set() {
        String delegator = randomString();
        accessToken.setDelegator(delegator);
        assertThat(accessToken.getDelegator(), equalTo(delegator));
        assertThat(accessToken.getStatus(), equalTo(TokenProcessingStatus.FORGED));
    }

    @Test
    @SuppressWarnings("Duplicates")
    public void when_serialize_cyphered_access_token_then_the_Delegator_must_be_committed() throws Exception {
        String delegator = randomString();
        accessToken.setDelegator(delegator);
        accessToken.cypher();
        JWEObject loadedToken = JWEObject.parse(accessToken.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        assertThat(signedJWT.getJWTClaimsSet().getClaim(DELEGATOR.getValue()), equalTo(delegator));
    }

    @Test
    public void when_create_a_fresh_access_token_and_set_Delegate_then_must_be_set() {
        String delegate = randomString();
        accessToken.setDelegate(delegate);
        assertThat(accessToken.getDelegate(), equalTo(delegate));
        assertThat(accessToken.getStatus(), equalTo(TokenProcessingStatus.FORGED));
    }

    @Test
    @SuppressWarnings("Duplicates")
    public void when_serialize_cyphered_access_token_then_the_Delegate_must_be_committed() throws Exception {
        String delegate = randomString();
        accessToken.setDelegate(delegate);
        accessToken.cypher();
        JWEObject loadedToken = JWEObject.parse(accessToken.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        assertThat(signedJWT.getJWTClaimsSet().getClaim(DELEGATE.getValue()), equalTo(delegate));
    }

    @Test
    public void when_create_a_fresh_access_token_and_set_Verdict_then_must_be_set() {
        accessToken.setVerdict(SUCCESS);
        assertThat(accessToken.getVerdict(), equalTo(SUCCESS));
        assertThat(accessToken.getStatus(), equalTo(TokenProcessingStatus.FORGED));
    }

    @Test
    public void when_serialize_cyphered_access_token_then_the_Verdict_must_be_committed() throws Exception {
        accessToken.setVerdict(FAIL);
        accessToken.cypher();
        JWEObject loadedToken = JWEObject.parse(accessToken.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        assertThat(signedJWT.getJWTClaimsSet().getClaim(VERDICT.getValue()), equalTo(FAIL.name()));
    }

    @DataProvider
    public static Object[][] accessClaims() {
        // @formatter:off
        return new String[][]{
                {randomString(), randomString()},
                {randomString(), randomString()},
                {randomString(), randomString()},
                {randomString(), randomString()},
        };
        // @formatter:on
    }

    @Test
    @UseDataProvider("accessClaims")
    public void when_set_other_access_claim_then_it_must_be_set(String... claimPair) {
        accessToken.setClaim(claimPair[0], claimPair[1]);
        assertThat(accessToken.getClaim(claimPair[0]), equalTo(claimPair[1]));
        assertThat(accessToken.getStatus(), equalTo(TokenProcessingStatus.FORGED));
    }

    @Test
    @UseDataProvider("accessClaims")
    @SuppressWarnings("Duplicates")
    public void when_serialize_cyphered_token_then_the_other_claims_must_be_committed(String... claimPair) throws Exception {
        accessToken.setClaim(claimPair[0], claimPair[1]);
        accessToken.cypher();
        JWEObject loadedToken = JWEObject.parse(accessToken.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        assertThat(signedJWT.getJWTClaimsSet().getClaim(claimPair[0]), equalTo(claimPair[1]));
    }

    @Test
    public void when_create_a_fresh_access_token_then_it_expires_in_3_minutes() {
        accessToken.cypher();
        assertThat(accessToken.getExpiryTime(), notNullValue());
        assertThat(accessToken.getExpiryTime(), LocalDateTimeMatchers.within(180, ChronoUnit.SECONDS, now()));
        assertThat(accessToken.getExpiryTime(), LocalDateTimeMatchers.after(now().plusSeconds(179)));
    }

    @Test
    public void when_serialize_cyphered_access_token_then_the_expiry_time_must_be_committed() throws Exception {
        accessToken.cypher();
        JWEObject loadedToken = JWEObject.parse(accessToken.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        final Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        assertThat(expirationTime, DateMatchers.within(3, ChronoUnit.MINUTES, new Date()));
        assertThat(expirationTime, DateMatchers.after(new Date(currentTimeMillis() + (1000 * 59 * 3))));
    }

    @Test
    public void when_create_a_fresh_access_token_and_each_time_refresh_mode_then_it_expires_in_3_minutes() throws Exception {
        accessToken.setRefreshMode(TokenRefreshMode.EACH_TIME);
        accessToken.cypher();
        JWEObject loadedToken = JWEObject.parse(accessToken.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        final Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        assertThat(expirationTime, DateMatchers.within(3, ChronoUnit.MINUTES, new Date()));
        assertThat(expirationTime, DateMatchers.after(new Date(currentTimeMillis() + (1000 * 59 * 3))));
    }

    @Test
    public void when_create_a_fresh_access_token_and_expiry_refresh_mode_then_it_expires_in_30_minutes() throws Exception {
        accessToken.setRefreshMode(TokenRefreshMode.WHEN_EXPIRES);
        accessToken.cypher();
        JWEObject loadedToken = JWEObject.parse(accessToken.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        final Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        assertThat(expirationTime, DateMatchers.within(30, ChronoUnit.MINUTES, new Date()));
        assertThat(expirationTime, DateMatchers.after(new Date(currentTimeMillis() + (1000 * 60 * 29))));
    }

    @Test
    public void when_create_a_fresh_access_token_then_it_is_effective_now() {
        accessToken.cypher();
        assertThat(accessToken.getEffectiveTime(), notNullValue());
        assertThat(accessToken.getEffectiveTime().isAfter(now().minusSeconds(2)), equalTo(true));
        assertThat(accessToken.getEffectiveTime().isBefore(now().plusSeconds(1)), equalTo(true));
    }

    @Test
    public void when_serialize_cyphered_access_token_then_the_effective_time_must_be_committed() throws Exception {
        accessToken.cypher();
        Instant twoSecondsAgo = now().minusSeconds(2).atZone(ZoneId.systemDefault()).toInstant();
        JWEObject loadedToken = JWEObject.parse(accessToken.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        assertThat(signedJWT.getJWTClaimsSet().getNotBeforeTime().after(from(twoSecondsAgo)), equalTo(true));
        assertThat(signedJWT.getJWTClaimsSet().getNotBeforeTime().before(new Date()), equalTo(true));
    }

    @Test
    public void when_create_a_fresh_access_token_then_it_is_issued_now() {
        accessToken.cypher();
        assertThat(accessToken.getIssuanceTime(), notNullValue());
        assertThat(accessToken.getIssuanceTime().isAfter(now().minusSeconds(2)), equalTo(true));
        assertThat(accessToken.getIssuanceTime().isBefore(now().plusSeconds(1)), equalTo(true));
    }

    @Test
    public void when_serialize_cyphered_access_token_then_the_issuance_time_must_be_committed() throws Exception {
        accessToken.cypher();
        Instant twoSecondsAgo = now().minusSeconds(2).atZone(ZoneId.systemDefault()).toInstant();
        JWEObject loadedToken = JWEObject.parse(accessToken.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        assertThat(signedJWT.getJWTClaimsSet().getIssueTime().after(from(twoSecondsAgo)), equalTo(true));
        assertThat(signedJWT.getJWTClaimsSet().getIssueTime().before(new Date()), equalTo(true));
    }


    @Test
    public void when_serialize_an_access_cyphered_token_many_times_then_return_the_same_value(){
        accessToken.cypher();
        assertThat(accessToken.serialize(), equalTo(accessToken.serialize()));
    }

    @Test
    @SuppressWarnings("Duplicates")
    public void when_serialize_an_access_each_time_after_a_cypher_then_return_different_value(){
        accessToken.cypher();
        final String first = accessToken.serialize();
        accessToken.cypher();
        final String second = accessToken.serialize();
        assertThat(first, not(equalTo(second)));
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void when_set_stamp_and_the_access_token_is_already_ciphered_then_error() {
        accessToken.cypher();
        accessToken.setStamp(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void when_set_subject_and_the_access_token_is_already_ciphered_then_error() {
        accessToken.cypher();
        accessToken.setSubject(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void when_set_scopes_and_the_access_token_is_already_ciphered_then_error() {
        accessToken.cypher();
        accessToken.setScopes(Collections.emptySet());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void when_set_BP_and_the_access_token_is_already_ciphered_then_error() {
        accessToken.cypher();
        accessToken.setBP(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void when_set_State_and_already_ciphered_then_error() {
        accessToken.cypher();
        accessToken.setState(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void when_set_Issuer_and_the_access_token_is_already_ciphered_then_error() {
        accessToken.cypher();
        accessToken.setIssuer(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void when_set_Audience_and_the_access_token_is_already_ciphered_then_error() {
        accessToken.cypher();
        accessToken.setAudience(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void when_set_Target_URL_and_the_access_token_is_already_ciphered_then_error() {
        accessToken.cypher();
        accessToken.setTargetURL(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void when_set_Delegator_and_the_access_token_is_already_ciphered_then_error() {
        accessToken.cypher();
        accessToken.setDelegator(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void when_set_Delegate_and_the_access_token_is_already_ciphered_then_error() {
        accessToken.cypher();
        accessToken.setDelegate(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void when_set_Verdict_and_the_access_token_is_already_ciphered_then_error() {
        accessToken.cypher();
        accessToken.setVerdict(TokenVerdict.SUCCESS);
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void when_set_Claim_and_the_access_token_is_already_ciphered_then_error() {
        accessToken.cypher();
        accessToken.setClaim(randomString(), randomString());
    }

    @Test
    @SuppressWarnings("Duplicates")
    public void when_cypher_a_fresh_access_token_then_it_must_be_signed_and_encrypted() throws Exception {
        accessToken.cypher();
        JWEObject loadedToken = JWEObject.parse(accessToken.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        assertThat(signedJWT, notNullValue());
        assertTrue(signedJWT.verify(new RSASSAVerifier(decipherCred.getValue())));
    }

    @Test
    public void when_cypher_a_fresh_access_token_then_set_it_as_cyphered() {
        accessToken.cypher();
        assertThat(accessToken.getStatus(), equalTo(TokenProcessingStatus.CYPHERED));
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void when_cypher_a_fresh_access_token_and_decypher_it_then_error() {
        accessToken.cypher();
        accessToken.decipher();
    }

    @Test(expected = TokenNotCipheredException.class)
    public void when_serialize_a_non_cyphered_access_token_then_error() {
        accessToken.serialize();
    }
}
