package com.mlyauth.token.jose;

import com.mlyauth.constants.TokenNorm;
import com.mlyauth.constants.TokenScope;
import com.mlyauth.constants.TokenStatus;
import com.mlyauth.constants.TokenType;
import com.mlyauth.token.Claims;
import com.mlyauth.tools.KeysForTests;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jwt.SignedJWT;
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
import java.util.Set;
import java.util.stream.Collectors;

import static com.mlyauth.constants.TokenScope.*;
import static com.mlyauth.constants.TokenVerdict.FAIL;
import static com.mlyauth.constants.TokenVerdict.SUCCESS;
import static com.mlyauth.token.Claims.*;
import static com.mlyauth.tools.RandomForTests.randomString;
import static java.util.Date.from;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

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
        assertThat(refreshToken.getStatus(), equalTo(TokenStatus.FRESH));
    }

    @Test
    public void when_create_a_fresh_refresh_token_and_set_stamp_then_must_be_set() {
        String id = randomString();
        refreshToken.setStamp(id);
        assertThat(refreshToken.getStamp(), equalTo(id));
        assertThat(refreshToken.getStatus(), equalTo(TokenStatus.FORGED));
    }

    @Test
    @SuppressWarnings("Duplicates")
    public void when_serialize_cyphered_refresh_token_then_the_stamp_must_be_committed() throws Exception {
        final String id = randomString();
        refreshToken.setStamp(id);
        refreshToken.cypher();
        JWEObject loadedToken = JWEObject.parse(refreshToken.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        assertThat(signedJWT.getJWTClaimsSet().getJWTID(), equalTo(id));
    }

    @Test
    public void when_create_a_fresh_refresh_token_and_set_subject_then_must_be_set() {
        String subject = randomString();
        refreshToken.setSubject(subject);
        assertThat(refreshToken.getSubject(), equalTo(subject));
        assertThat(refreshToken.getStatus(), equalTo(TokenStatus.FORGED));
    }

    @Test
    @SuppressWarnings("Duplicates")
    public void when_serialize_cyphered_refresh_token_then_the_subject_must_be_committed() throws Exception {
        String subject = randomString();
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
    public void when_create_a_fresh_refresh_token_and_set_scopes_then_they_must_be_set(String... scopesArrays) {
        final Set<TokenScope> scopes = Arrays.stream(scopesArrays).map(TokenScope::valueOf).collect(toSet());
        refreshToken.setScopes(scopes);
        assertThat(refreshToken.getScopes(), equalTo(scopes));
        assertThat(refreshToken.getStatus(), equalTo(TokenStatus.FORGED));
    }

    @Test
    @UseDataProvider("refreshScopes")
    @SuppressWarnings("Duplicates")
    public void when_serialize_cyphered_refresh_token_then_the_scopes_must_be_committed(String... scopesArray) throws Exception {
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
        String bp = randomString();
        refreshToken.setBP(bp);
        assertThat(refreshToken.getBP(), equalTo(bp));
        assertThat(refreshToken.getStatus(), equalTo(TokenStatus.FORGED));
    }

    @Test
    @SuppressWarnings("Duplicates")
    public void when_serialize_cyphered_refresh_token_then_the_BP_must_be_committed() throws Exception {
        String bp = randomString();
        refreshToken.setBP(bp);
        refreshToken.cypher();
        JWEObject loadedToken = JWEObject.parse(refreshToken.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        assertThat(signedJWT.getJWTClaimsSet().getClaim(BP.getValue()), equalTo(bp));
    }

    @Test
    public void when_create_a_fresh_refresh_token_and_set_State_then_must_be_set() {
        String state = randomString();
        refreshToken.setState(state);
        assertThat(refreshToken.getState(), equalTo(state));
        assertThat(refreshToken.getStatus(), equalTo(TokenStatus.FORGED));
    }

    @Test
    @SuppressWarnings("Duplicates")
    public void when_serialize_cyphered_refresh_token_then_the_State_must_be_committed() throws Exception {
        String state = randomString();
        refreshToken.setState(state);
        refreshToken.cypher();
        JWEObject loadedToken = JWEObject.parse(refreshToken.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        assertThat(signedJWT.getJWTClaimsSet().getClaim(STATE.getValue()), equalTo(state));
    }

    @Test
    public void when_create_a_fresh_refresh_token_and_set_Issuer_then_must_be_set() {
        String issuer = randomString();
        refreshToken.setIssuer(issuer);
        assertThat(refreshToken.getIssuer(), equalTo(issuer));
        assertThat(refreshToken.getStatus(), equalTo(TokenStatus.FORGED));
    }

    @Test
    @SuppressWarnings("Duplicates")
    public void when_serialize_cyphered_refresh_token_then_the_Issuer_must_be_committed() throws Exception {
        String issuer = randomString();
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
        String audienceURI = randomString();
        refreshToken.setAudience(audienceURI);
        assertThat(refreshToken.getAudience(), equalTo(audienceURI));
        assertThat(refreshToken.getStatus(), equalTo(TokenStatus.FORGED));
    }

    @Test
    @SuppressWarnings("Duplicates")
    public void when_serialize_cyphered_token_then_the_Audience_must_be_committed() throws Exception {
        String audienceURI = randomString();
        refreshToken.setAudience(audienceURI);
        refreshToken.cypher();
        JWEObject loadedToken = JWEObject.parse(refreshToken.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        assertThat(signedJWT.getJWTClaimsSet().getAudience().get(0), equalTo(audienceURI));
    }

    @Test
    public void when_create_a_fresh_refresh_token_and_set_Target_URL_then_must_be_set() {
        String targetURL = randomString();
        refreshToken.setTargetURL(targetURL);
        assertThat(refreshToken.getTargetURL(), equalTo(targetURL));
        assertThat(refreshToken.getStatus(), equalTo(TokenStatus.FORGED));
    }

    @Test
    @SuppressWarnings("Duplicates")
    public void when_serialize_cyphered_refresh_token_then_the_Target_URL_must_be_committed() throws Exception {
        String targetURL = randomString();
        refreshToken.setTargetURL(targetURL);
        refreshToken.cypher();
        JWEObject loadedToken = JWEObject.parse(refreshToken.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        assertThat(signedJWT.getJWTClaimsSet().getClaim(TARGET_URL.getValue()), equalTo(targetURL));
    }

    @Test
    public void when_create_a_fresh_refresh_token_and_set_Delegator_then_must_be_set() {
        String delegator = randomString();
        refreshToken.setDelegator(delegator);
        assertThat(refreshToken.getDelegator(), equalTo(delegator));
        assertThat(refreshToken.getStatus(), equalTo(TokenStatus.FORGED));
    }

    @Test
    @SuppressWarnings("Duplicates")
    public void when_serialize_cyphered_refresh_token_then_the_Delegator_must_be_committed() throws Exception {
        String delegator = randomString();
        refreshToken.setDelegator(delegator);
        refreshToken.cypher();
        JWEObject loadedToken = JWEObject.parse(refreshToken.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        assertThat(signedJWT.getJWTClaimsSet().getClaim(DELEGATOR.getValue()), equalTo(delegator));
    }

    @Test
    public void when_create_a_fresh_refresh_token_and_set_Delegate_then_must_be_set() {
        String delegate = randomString();
        refreshToken.setDelegate(delegate);
        assertThat(refreshToken.getDelegate(), equalTo(delegate));
        assertThat(refreshToken.getStatus(), equalTo(TokenStatus.FORGED));
    }

    @Test
    @SuppressWarnings("Duplicates")
    public void when_serialize_cyphered_refresh_token_then_the_Delegate_must_be_committed() throws Exception {
        String delegate = randomString();
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
        assertThat(refreshToken.getStatus(), equalTo(TokenStatus.FORGED));
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
                {randomString(), randomString()},
                {randomString(), randomString()},
                {randomString(), randomString()},
                {randomString(), randomString()},
        };
        // @formatter:on
    }

    @Test
    @UseDataProvider("refreshClaims")
    public void when_set_other_refresh_claim_then_it_must_be_set(String... claimPair) {
        refreshToken.setClaim(claimPair[0], claimPair[1]);
        assertThat(refreshToken.getClaim(claimPair[0]), equalTo(claimPair[1]));
        assertThat(refreshToken.getStatus(), equalTo(TokenStatus.FORGED));
    }

    @Test
    @UseDataProvider("refreshClaims")
    @SuppressWarnings("Duplicates")
    public void when_serialize_cyphered_token_then_the_other_claims_must_be_committed(String... claimPair) throws Exception {
        refreshToken.setClaim(claimPair[0], claimPair[1]);
        refreshToken.cypher();
        JWEObject loadedToken = JWEObject.parse(refreshToken.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        assertThat(signedJWT.getJWTClaimsSet().getClaim(claimPair[0]), equalTo(claimPair[1]));
    }

    @Test
    public void when_create_a_fresh_refresh_token_then_it_expires_in_3_years() {
        assertThat(refreshToken.getExpiryTime(), notNullValue());
        assertThat(refreshToken.getExpiryTime().isBefore(LocalDateTime.now().plusDays(366 * 3)), equalTo(true));
        assertThat(refreshToken.getExpiryTime().isAfter(LocalDateTime.now().plusDays(365 * 3)), equalTo(true));
    }

    @Test
    public void when_serialize_cyphered_access_token_then_the_expiry_time_must_be_committed() throws Exception {
        refreshToken.cypher();
        Instant maxDate = LocalDateTime.now().plusDays(366 * 3).atZone(ZoneId.systemDefault()).toInstant();
        Instant minDate = LocalDateTime.now().plusDays(365 * 3).atZone(ZoneId.systemDefault()).toInstant();
        JWEObject loadedToken = JWEObject.parse(refreshToken.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        assertThat(signedJWT.getJWTClaimsSet().getExpirationTime().before(Date.from(maxDate)), equalTo(true));
        assertThat(signedJWT.getJWTClaimsSet().getExpirationTime().after(Date.from(minDate)), equalTo(true));
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

}
