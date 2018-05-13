package com.primasolutions.idp.token.jose;

import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.SignedJWT;
import com.primasolutions.idp.constants.TokenNorm;
import com.primasolutions.idp.constants.TokenProcessingStatus;
import com.primasolutions.idp.constants.TokenScope;
import com.primasolutions.idp.constants.TokenType;
import com.primasolutions.idp.constants.TokenVerdict;
import com.primasolutions.idp.exception.TokenNotCipheredException;
import com.primasolutions.idp.exception.TokenUnmodifiableException;
import com.primasolutions.idp.token.Claims;
import com.primasolutions.idp.tools.KeysForTests;
import com.primasolutions.idp.tools.RandomForTests;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import javafx.util.Pair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static com.primasolutions.idp.constants.TokenRefreshMode.WHEN_EXPIRES;
import static com.primasolutions.idp.constants.TokenScope.CLAIM;
import static com.primasolutions.idp.constants.TokenScope.PERSON;
import static com.primasolutions.idp.constants.TokenScope.POLICY;
import static com.primasolutions.idp.constants.TokenScope.PROPOSAL;
import static com.primasolutions.idp.constants.TokenValidationMode.STANDARD;
import static com.primasolutions.idp.constants.TokenVerdict.FAIL;
import static com.primasolutions.idp.constants.TokenVerdict.SUCCESS;
import static com.primasolutions.idp.token.Claims.BP;
import static com.primasolutions.idp.token.Claims.DELEGATE;
import static com.primasolutions.idp.token.Claims.DELEGATOR;
import static com.primasolutions.idp.token.Claims.REFRESH_MODE;
import static com.primasolutions.idp.token.Claims.SCOPES;
import static com.primasolutions.idp.token.Claims.STATE;
import static com.primasolutions.idp.token.Claims.TARGET_URL;
import static com.primasolutions.idp.token.Claims.VALIDATION_MODE;
import static com.primasolutions.idp.token.Claims.VERDICT;
import static java.util.Date.from;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(DataProviderRunner.class)
public class JOSEFreshRefreshTokenTest {

    private JOSERefreshToken refreshToken;
    private Pair<PrivateKey, RSAPublicKey> cypherCred;
    private Pair<PrivateKey, RSAPublicKey> decipherCred;

    @Before
    public void setup() {
        final Pair<PrivateKey, X509Certificate> peerCred = KeysForTests.generateRSACredential();
        final Pair<PrivateKey, X509Certificate> localCred = KeysForTests.generateRSACredential();
        cypherCred = new Pair<>(localCred.getKey(), (RSAPublicKey) peerCred.getValue().getPublicKey());
        decipherCred = new Pair<>(peerCred.getKey(), (RSAPublicKey) localCred.getValue().getPublicKey());
        refreshToken = new JOSERefreshToken(cypherCred.getKey(), cypherCred.getValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_create_a_fresh_refresh_token_and_private_key_is_null_then_error() {
        final Pair<PrivateKey, X509Certificate> credential = KeysForTests.generateRSACredential();
        new JOSERefreshToken(null, credential.getValue().getPublicKey());
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_create_a_fresh_access_token_and_public_key_is_null_then_error() {
        final Pair<PrivateKey, X509Certificate> credential = KeysForTests.generateRSACredential();
        new JOSERefreshToken(credential.getKey(), null);
    }

    @Test
    public void when_create_fresh_refresh_response_then_token_claims_must_be_fresh() {
        assertThat(refreshToken.getStamp(), nullValue());
        assertThat(refreshToken.getSubject(), nullValue());
        assertThat(refreshToken.getScopes(), empty());
        assertThat(refreshToken.getBP(), nullValue());
        assertThat(refreshToken.getState(), nullValue());
        assertThat(refreshToken.getIssuer(), nullValue());
        assertThat(refreshToken.getAudience(), nullValue());
        assertThat(refreshToken.getDelegator(), nullValue());
        assertThat(refreshToken.getDelegate(), nullValue());
        assertThat(refreshToken.getVerdict(), nullValue());
        assertThat(refreshToken.getNorm(), equalTo(TokenNorm.JOSE));
        assertThat(refreshToken.getType(), equalTo(TokenType.REFRESH));
        assertThat(refreshToken.getStatus(), equalTo(TokenProcessingStatus.FRESH));
    }

    @Test
    public void when_create_a_fresh_refresh_token_and_set_refresh_mode_then_must_be_set() {
        refreshToken.setRefreshMode(WHEN_EXPIRES);
        assertThat(refreshToken.getRefreshMode(), equalTo(WHEN_EXPIRES));
        assertThat(refreshToken.getStatus(), equalTo(TokenProcessingStatus.FORGED));
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_create_a_fresh_refresh_token_and_set_null_as_refresh_mode_then_error() {
        refreshToken.setRefreshMode(null);
    }

    @Test
    @SuppressWarnings("Duplicates")
    public void when_serialize_cyphered_refresh_token_then_the_refresh_mode_must_be_committed() throws Exception {
        refreshToken.setRefreshMode(WHEN_EXPIRES);
        refreshToken.cypher();
        JWEObject loadedToken = JWEObject.parse(refreshToken.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        assertThat(signedJWT.getJWTClaimsSet().getClaim(REFRESH_MODE.getValue()), equalTo(WHEN_EXPIRES.name()));
    }

    @Test
    public void when_create_a_fresh_refresh_token_and_set_validation_mode_then_must_be_set() {
        refreshToken.setValidationMode(STANDARD);
        assertThat(refreshToken.getValidationMode(), equalTo(STANDARD));
        assertThat(refreshToken.getStatus(), equalTo(TokenProcessingStatus.FORGED));
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_create_a_fresh_refresh_token_and_set_null_as_validation_mode_then_error() {
        refreshToken.setValidationMode(null);
    }

    @Test
    @SuppressWarnings("Duplicates")
    public void when_serialize_cyphered_refresh_token_then_the_validation_mode_must_be_committed() throws Exception {
        refreshToken.setValidationMode(STANDARD);
        refreshToken.cypher();
        JWEObject loadedToken = JWEObject.parse(refreshToken.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        assertThat(signedJWT.getJWTClaimsSet().getClaim(VALIDATION_MODE.getValue()), equalTo(STANDARD.name()));
    }

    @Test
    public void when_create_a_fresh_refresh_token_and_set_stamp_then_must_be_set() {
        String id = RandomForTests.randomString();
        refreshToken.setStamp(id);
        assertThat(refreshToken.getStamp(), equalTo(id));
        assertThat(refreshToken.getStatus(), equalTo(TokenProcessingStatus.FORGED));
    }

    @Test
    @SuppressWarnings("Duplicates")
    public void when_serialize_cyphered_refresh_token_then_the_stamp_must_be_committed() throws Exception {
        final String id = RandomForTests.randomString();
        refreshToken.setStamp(id);
        refreshToken.cypher();
        JWEObject loadedToken = JWEObject.parse(refreshToken.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        assertThat(signedJWT.getJWTClaimsSet().getJWTID(), equalTo(id));
    }

    @Test
    public void when_create_a_fresh_refresh_token_and_set_subject_then_must_be_set() {
        String subject = RandomForTests.randomString();
        refreshToken.setSubject(subject);
        assertThat(refreshToken.getSubject(), equalTo(subject));
        assertThat(refreshToken.getStatus(), equalTo(TokenProcessingStatus.FORGED));
    }

    @Test
    @SuppressWarnings("Duplicates")
    public void when_serialize_cyphered_refresh_token_then_the_subject_must_be_committed() throws Exception {
        String subject = RandomForTests.randomString();
        refreshToken.setSubject(subject);
        refreshToken.cypher();
        JWEObject loadedToken = JWEObject.parse(refreshToken.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        assertThat(signedJWT.getJWTClaimsSet().getSubject(), equalTo(subject));
    }

    @DataProvider
    public static Object[][] refreshScopes() {
        // @formatter:off
        return new Object[][]{
                {PERSON.name(), POLICY.name(), PROPOSAL.name()},
                {PROPOSAL.name(), POLICY.name(), CLAIM.name()},
                {POLICY.name(), CLAIM.name(), PERSON.name(), PROPOSAL.name()},
                {CLAIM.name(), POLICY.name()},
                {PROPOSAL.name()},
        };
        // @formatter:on
    }

    @Test
    @UseDataProvider("refreshScopes")
    public void when_create_a_fresh_refresh_token_and_set_scopes_then_they_must_be_set(final String... scopesArrays) {
        final Set<TokenScope> scopes = Arrays.stream(scopesArrays).map(TokenScope::valueOf).collect(toSet());
        refreshToken.setScopes(scopes);
        assertThat(refreshToken.getScopes(), equalTo(scopes));
        assertThat(refreshToken.getStatus(), equalTo(TokenProcessingStatus.FORGED));
    }

    @Test
    @UseDataProvider("refreshScopes")
    @SuppressWarnings("Duplicates")
    public void when_serialize_cyphered_refresh_token_then_the_scopes_must_be_committed(final String... scopesArray)
            throws Exception {
        final Set<TokenScope> scopes = Arrays.stream(scopesArray).map(TokenScope::valueOf).collect(toSet());
        refreshToken.setScopes(scopes);
        refreshToken.cypher();
        JWEObject loadedToken = JWEObject.parse(refreshToken.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        assertThat(signedJWT.getJWTClaimsSet().getClaim(SCOPES.getValue()),
                equalTo(scopes.stream().map(TokenScope::name).collect(Collectors.joining("|"))));
    }

    @Test
    public void when_create_a_fresh_refresh_token_and_set_BP_then_must_be_set() {
        String bp = RandomForTests.randomString();
        refreshToken.setBP(bp);
        assertThat(refreshToken.getBP(), equalTo(bp));
        assertThat(refreshToken.getStatus(), equalTo(TokenProcessingStatus.FORGED));
    }

    @Test
    @SuppressWarnings("Duplicates")
    public void when_serialize_cyphered_refresh_token_then_the_BP_must_be_committed() throws Exception {
        String bp = RandomForTests.randomString();
        refreshToken.setBP(bp);
        refreshToken.cypher();
        JWEObject loadedToken = JWEObject.parse(refreshToken.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        assertThat(signedJWT.getJWTClaimsSet().getClaim(BP.getValue()), equalTo(bp));
    }

    @Test
    public void when_create_a_fresh_refresh_token_and_set_State_then_must_be_set() {
        String state = RandomForTests.randomString();
        refreshToken.setState(state);
        assertThat(refreshToken.getState(), equalTo(state));
        assertThat(refreshToken.getStatus(), equalTo(TokenProcessingStatus.FORGED));
    }

    @Test
    @SuppressWarnings("Duplicates")
    public void when_serialize_cyphered_refresh_token_then_the_State_must_be_committed() throws Exception {
        String state = RandomForTests.randomString();
        refreshToken.setState(state);
        refreshToken.cypher();
        JWEObject loadedToken = JWEObject.parse(refreshToken.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        assertThat(signedJWT.getJWTClaimsSet().getClaim(STATE.getValue()), equalTo(state));
    }

    @Test
    public void when_create_a_fresh_refresh_token_and_set_Issuer_then_must_be_set() {
        String issuer = RandomForTests.randomString();
        refreshToken.setIssuer(issuer);
        assertThat(refreshToken.getIssuer(), equalTo(issuer));
        assertThat(refreshToken.getStatus(), equalTo(TokenProcessingStatus.FORGED));
    }

    @Test
    @SuppressWarnings("Duplicates")
    public void when_serialize_cyphered_refresh_token_then_the_Issuer_must_be_committed() throws Exception {
        String issuer = RandomForTests.randomString();
        refreshToken.setIssuer(issuer);
        refreshToken.cypher();
        JWEObject loadedToken = JWEObject.parse(refreshToken.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        assertThat(signedJWT.getJWTClaimsSet().getIssuer(), equalTo(issuer));
        assertThat(signedJWT.getHeader().getCustomParam(Claims.ISSUER.getValue()), equalTo(issuer));
    }

    @Test
    public void when_create_a_fresh_refresh_token_and_set_Audience_then_must_be_set() {
        String audienceURI = RandomForTests.randomString();
        refreshToken.setAudience(audienceURI);
        assertThat(refreshToken.getAudience(), equalTo(audienceURI));
        assertThat(refreshToken.getStatus(), equalTo(TokenProcessingStatus.FORGED));
    }

    @Test
    @SuppressWarnings("Duplicates")
    public void when_serialize_cyphered_token_then_the_Audience_must_be_committed() throws Exception {
        String audienceURI = RandomForTests.randomString();
        refreshToken.setAudience(audienceURI);
        refreshToken.cypher();
        JWEObject loadedToken = JWEObject.parse(refreshToken.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        assertThat(signedJWT.getJWTClaimsSet().getAudience().get(0), equalTo(audienceURI));
    }

    @Test
    public void when_create_a_fresh_refresh_token_and_set_Target_URL_then_must_be_set() {
        String targetURL = RandomForTests.randomString();
        refreshToken.setTargetURL(targetURL);
        assertThat(refreshToken.getTargetURL(), equalTo(targetURL));
        assertThat(refreshToken.getStatus(), equalTo(TokenProcessingStatus.FORGED));
    }

    @Test
    @SuppressWarnings("Duplicates")
    public void when_serialize_cyphered_refresh_token_then_the_Target_URL_must_be_committed() throws Exception {
        String targetURL = RandomForTests.randomString();
        refreshToken.setTargetURL(targetURL);
        refreshToken.cypher();
        JWEObject loadedToken = JWEObject.parse(refreshToken.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        assertThat(signedJWT.getJWTClaimsSet().getClaim(TARGET_URL.getValue()), equalTo(targetURL));
    }

    @Test
    public void when_create_a_fresh_refresh_token_and_set_Delegator_then_must_be_set() {
        String delegator = RandomForTests.randomString();
        refreshToken.setDelegator(delegator);
        assertThat(refreshToken.getDelegator(), equalTo(delegator));
        assertThat(refreshToken.getStatus(), equalTo(TokenProcessingStatus.FORGED));
    }

    @Test
    @SuppressWarnings("Duplicates")
    public void when_serialize_cyphered_refresh_token_then_the_Delegator_must_be_committed() throws Exception {
        String delegator = RandomForTests.randomString();
        refreshToken.setDelegator(delegator);
        refreshToken.cypher();
        JWEObject loadedToken = JWEObject.parse(refreshToken.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        assertThat(signedJWT.getJWTClaimsSet().getClaim(DELEGATOR.getValue()), equalTo(delegator));
    }

    @Test
    public void when_create_a_fresh_refresh_token_and_set_Delegate_then_must_be_set() {
        String delegate = RandomForTests.randomString();
        refreshToken.setDelegate(delegate);
        assertThat(refreshToken.getDelegate(), equalTo(delegate));
        assertThat(refreshToken.getStatus(), equalTo(TokenProcessingStatus.FORGED));
    }

    @Test
    @SuppressWarnings("Duplicates")
    public void when_serialize_cyphered_refresh_token_then_the_Delegate_must_be_committed() throws Exception {
        String delegate = RandomForTests.randomString();
        refreshToken.setDelegate(delegate);
        refreshToken.cypher();
        JWEObject loadedToken = JWEObject.parse(refreshToken.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        assertThat(signedJWT.getJWTClaimsSet().getClaim(DELEGATE.getValue()), equalTo(delegate));
    }

    @Test
    public void when_create_a_fresh_refresh_token_and_set_Verdict_then_must_be_set() {
        refreshToken.setVerdict(FAIL);
        assertThat(refreshToken.getVerdict(), equalTo(FAIL));
        assertThat(refreshToken.getStatus(), equalTo(TokenProcessingStatus.FORGED));
    }

    @Test
    public void when_serialize_cyphered_refresh_token_then_the_Verdict_must_be_committed() throws Exception {
        refreshToken.setVerdict(SUCCESS);
        refreshToken.cypher();
        JWEObject loadedToken = JWEObject.parse(refreshToken.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        assertThat(signedJWT.getJWTClaimsSet().getClaim(VERDICT.getValue()), equalTo(SUCCESS.name()));
    }

    @DataProvider
    public static Object[][] refreshClaims() {
        // @formatter:off
        return new String[][]{
                {RandomForTests.randomString(), RandomForTests.randomString()},
                {RandomForTests.randomString(), RandomForTests.randomString()},
                {RandomForTests.randomString(), RandomForTests.randomString()},
                {RandomForTests.randomString(), RandomForTests.randomString()},
        };
        // @formatter:on
    }

    @Test
    @UseDataProvider("refreshClaims")
    public void when_set_other_refresh_claim_then_it_must_be_set(final String... claimPair) {
        refreshToken.setClaim(claimPair[0], claimPair[1]);
        assertThat(refreshToken.getClaim(claimPair[0]), equalTo(claimPair[1]));
        assertThat(refreshToken.getStatus(), equalTo(TokenProcessingStatus.FORGED));
    }

    @Test
    @UseDataProvider("refreshClaims")
    @SuppressWarnings("Duplicates")
    public void when_serialize_cyphered_token_then_the_other_claims_must_be_committed(final String... claimPair)
            throws Exception {
        refreshToken.setClaim(claimPair[0], claimPair[1]);
        refreshToken.cypher();
        JWEObject loadedToken = JWEObject.parse(refreshToken.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        assertThat(signedJWT.getJWTClaimsSet().getClaim(claimPair[0]), equalTo(claimPair[1]));
    }

    @Test
    public void when_create_a_fresh_refresh_token_then_it_expires_in_3_years() {
        //CHECKSTYLE:OFF
        assertThat(refreshToken.getExpiryTime(), notNullValue());
        assertThat(refreshToken.getExpiryTime().isBefore(LocalDateTime.now().plusDays(366 * 3)), equalTo(true));
        assertThat(refreshToken.getExpiryTime().isAfter(LocalDateTime.now().plusDays(365 * 3)), equalTo(true));
        //CHECKSTYLE:ON
    }

    @Test
    public void when_serialize_cyphered_access_token_then_the_expiry_time_must_be_committed() throws Exception {
        //CHECKSTYLE:OFF
        refreshToken.cypher();
        Instant maxDate = LocalDateTime.now().plusDays(366 * 3).atZone(ZoneId.systemDefault()).toInstant();
        Instant minDate = LocalDateTime.now().plusDays(365 * 3).atZone(ZoneId.systemDefault()).toInstant();
        JWEObject loadedToken = JWEObject.parse(refreshToken.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        assertThat(signedJWT.getJWTClaimsSet().getExpirationTime().before(Date.from(maxDate)), equalTo(true));
        assertThat(signedJWT.getJWTClaimsSet().getExpirationTime().after(Date.from(minDate)), equalTo(true));
        //CHECKSTYLE:ON
    }

    @Test
    public void when_create_a_fresh_refresh_token_then_it_is_effective_now() {
        assertThat(refreshToken.getEffectiveTime(), notNullValue());
        assertThat(refreshToken.getEffectiveTime().isAfter(LocalDateTime.now().minusSeconds(2)), equalTo(true));
        assertThat(refreshToken.getEffectiveTime().isBefore(LocalDateTime.now().plusSeconds(1)), equalTo(true));
    }

    @Test
    public void when_create_a_fresh_refresh_token_then_it_is_issued_now() {
        assertThat(refreshToken.getIssuanceTime(), notNullValue());
        assertThat(refreshToken.getIssuanceTime().isAfter(LocalDateTime.now().minusSeconds(2)), equalTo(true));
        assertThat(refreshToken.getIssuanceTime().isBefore(LocalDateTime.now().plusSeconds(1)), equalTo(true));
    }

    @Test
    public void when_serialize_cyphered_refresh_token_then_the_issuance_time_must_be_committed() throws Exception {
        refreshToken.cypher();
        Instant twoSecondsAgo = LocalDateTime.now().minusSeconds(2).atZone(ZoneId.systemDefault()).toInstant();
        JWEObject loadedToken = JWEObject.parse(refreshToken.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        assertThat(signedJWT.getJWTClaimsSet().getIssueTime().after(from(twoSecondsAgo)), equalTo(true));
        assertThat(signedJWT.getJWTClaimsSet().getIssueTime().before(new java.util.Date()), equalTo(true));
    }

    @Test
    public void when_serialize_a_refresh_cyphered_token_many_times_then_return_the_same_value() {
        refreshToken.cypher();
        assertThat(refreshToken.serialize(), equalTo(refreshToken.serialize()));
    }

    @Test
    @SuppressWarnings("Duplicates")
    public void when_serialize_a_refresh_each_time_after_a_cypher_then_return_different_value() {
        refreshToken.cypher();
        final String first = refreshToken.serialize();
        refreshToken.cypher();
        final String second = refreshToken.serialize();
        assertThat(first, not(equalTo(second)));
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void when_set_stamp_and_the_refresh_is_already_ciphered_then_error() {
        refreshToken.cypher();
        refreshToken.setStamp(RandomForTests.randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void when_set_subject_and_the_refresh_is_already_ciphered_then_error() {
        refreshToken.cypher();
        refreshToken.setSubject(RandomForTests.randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void when_set_scopes_and_the_refresh_token_is_already_ciphered_then_error() {
        refreshToken.cypher();
        refreshToken.setScopes(Collections.emptySet());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void when_set_BP_and_the_refresh_token_is_already_ciphered_then_error() {
        refreshToken.cypher();
        refreshToken.setBP(RandomForTests.randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void when_set_Issuer_and_the_refresh_token_is_already_ciphered_then_error() {
        refreshToken.cypher();
        refreshToken.setIssuer(RandomForTests.randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void when_set_Audience_and_the_refresh_token_is_already_ciphered_then_error() {
        refreshToken.cypher();
        refreshToken.setAudience(RandomForTests.randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void when_set_Target_URL_and_the_refresh_token_is_already_ciphered_then_error() {
        refreshToken.cypher();
        refreshToken.setTargetURL(RandomForTests.randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void when_set_Delegator_and_the_refresh_token_is_already_ciphered_then_error() {
        refreshToken.cypher();
        refreshToken.setDelegator(RandomForTests.randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void when_set_Delegate_and_the_refresh_token_is_already_ciphered_then_error() {
        refreshToken.cypher();
        refreshToken.setDelegate(RandomForTests.randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void when_set_Verdict_and_the_refresh_token_is_already_ciphered_then_error() {
        refreshToken.cypher();
        refreshToken.setVerdict(TokenVerdict.FAIL);
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void when_set_Claim_and_the_refresh_token_is_already_ciphered_then_error() {
        refreshToken.cypher();
        refreshToken.setClaim(RandomForTests.randomString(), RandomForTests.randomString());
    }

    @Test
    @SuppressWarnings("Duplicates")
    public void when_cypher_a_fresh_refresh_token_then_it_must_be_signed_and_encrypted() throws Exception {
        refreshToken.cypher();
        JWEObject loadedToken = JWEObject.parse(refreshToken.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        assertThat(signedJWT, notNullValue());
        assertTrue(signedJWT.verify(new RSASSAVerifier(decipherCred.getValue())));
    }

    @Test
    public void when_cypher_a_fresh_refresh_token_then_set_it_as_cyphered() {
        refreshToken.cypher();
        assertThat(refreshToken.getStatus(), equalTo(TokenProcessingStatus.CYPHERED));
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void when_cypher_a_fresh_access_token_and_decypher_it_then_error() {
        refreshToken.cypher();
        refreshToken.decipher();
    }

    @Test(expected = TokenNotCipheredException.class)
    public void when_serialize_a_non_cyphered_refresh_token_then_error() {
        refreshToken.serialize();
    }
}
