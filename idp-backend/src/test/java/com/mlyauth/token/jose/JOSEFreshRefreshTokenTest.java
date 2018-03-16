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
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static com.mlyauth.constants.TokenScope.*;
import static com.mlyauth.token.Claims.*;
import static com.mlyauth.tools.RandomForTests.randomString;
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

}
