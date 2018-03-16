package com.mlyauth.token.jose;

import com.mlyauth.constants.TokenNorm;
import com.mlyauth.constants.TokenStatus;
import com.mlyauth.constants.TokenType;
import com.mlyauth.tools.KeysForTests;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jwt.SignedJWT;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import javafx.util.Pair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;

import static com.mlyauth.tools.RandomForTests.randomString;
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
    public void when_serialize_cyphered_refresh_token_then_the_stamp_must_be_committed() throws Exception {
        final String id = randomString();
        refreshToken.setStamp(id);
        refreshToken.cypher();
        JWEObject loadedToken = JWEObject.parse(refreshToken.serialize());
        loadedToken.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = loadedToken.getPayload().toSignedJWT();
        assertThat(signedJWT.getJWTClaimsSet().getJWTID(), equalTo(id));
    }

}
