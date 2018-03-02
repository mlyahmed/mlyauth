package com.mlyauth.utests.security.token.jwt;

import com.mlyauth.constants.TokenNorm;
import com.mlyauth.constants.TokenStatus;
import com.mlyauth.constants.TokenType;
import com.mlyauth.exception.TokenAlreadyCommitedException;
import com.mlyauth.exception.TokenNotCipheredException;
import com.mlyauth.security.token.jwt.JOSEAccessToken;
import com.mlyauth.tools.KeysForTests;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.SignedJWT;
import javafx.util.Pair;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.Random;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class JOSEFreshAccessTokenTest {

    private JOSEAccessToken token;
    private Pair<PrivateKey, RSAPublicKey> cypherCred;
    private Pair<PrivateKey, RSAPublicKey> decipherCred;

    @Before
    public void setup() {
        final Pair<PrivateKey, X509Certificate> peerCred = KeysForTests.generateRSACredential();
        final Pair<PrivateKey, X509Certificate> localCred = KeysForTests.generateRSACredential();
        cypherCred = new Pair<>(localCred.getKey(), (RSAPublicKey) peerCred.getValue().getPublicKey());
        decipherCred = new Pair<>(peerCred.getKey(), (RSAPublicKey) localCred.getValue().getPublicKey());
        token = new JOSEAccessToken(cypherCred.getKey(), cypherCred.getValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_create_a_fresh_token_and_private_key_is_null_then_error() {
        final Pair<PrivateKey, X509Certificate> credential = KeysForTests.generateRSACredential();
        new JOSEAccessToken(null, (RSAPublicKey) credential.getValue().getPublicKey());
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_create_a_fresh_token_and_public_key_is_null_then_error() {
        final Pair<PrivateKey, X509Certificate> credential = KeysForTests.generateRSACredential();
        new JOSEAccessToken(credential.getKey(), null);
    }

    @Test
    public void when_create_fresh_response_then_token_claims_must_be_fresh() {
        assertThat(token.getId(), nullValue());
        assertThat(token.getSubject(), nullValue());
        assertThat(token.getScopes(), empty());
        assertThat(token.getBP(), nullValue());
        assertThat(token.getState(), nullValue());
        assertThat(token.getIssuer(), nullValue());
        assertThat(token.getAudience(), nullValue());
        assertThat(token.getDelegator(), nullValue());
        assertThat(token.getDelegate(), nullValue());
        assertThat(token.getVerdict(), nullValue());
        assertThat(token.getNorm(), equalTo(TokenNorm.JWT));
        assertThat(token.getType(), equalTo(TokenType.ACCESS));
        assertThat(token.getStatus(), equalTo(TokenStatus.FRESH));
    }

    @Test
    public void when_create_a_fresh_token_and_set_Id_then_must_be_set() {
        String id = randomString();
        token.setId(id);
        assertThat(token.getId(), equalTo(id));
        assertThat(token.getStatus(), equalTo(TokenStatus.FORGED));
    }

    @Test
    public void when_serialize_cyphered_token_then_the_id_must_be_committed() throws Exception {
        final String id = randomString();
        token.setId(id);
        token.cypher();
        JWEObject jweObject = JWEObject.parse(token.serialize());
        jweObject.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = jweObject.getPayload().toSignedJWT();
        assertThat(signedJWT.getJWTClaimsSet().getJWTID(), equalTo(id));
    }

    @Test
    public void when_create_a_fresh_token_and_set_subject_then_must_be_set() {
        String subject = randomString();
        token.setSubject(subject);
        assertThat(token.getSubject(), equalTo(subject));
        assertThat(token.getStatus(), equalTo(TokenStatus.FORGED));
    }

    @Test
    public void when_serialize_cyphered_token_then_the_subject_must_be_committed() throws Exception {
        String subject = randomString();
        token.setSubject(subject);
        token.cypher();
        JWEObject jweObject = JWEObject.parse(token.serialize());
        jweObject.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = jweObject.getPayload().toSignedJWT();
        assertThat(signedJWT.getJWTClaimsSet().getSubject(), equalTo(subject));
    }

    @Test
    public void when_cypher_a_fresh_token_then_it_must_be_signed_and_encrypted() throws Exception {
        token.cypher();
        JWEObject jweObject = JWEObject.parse(token.serialize());
        jweObject.decrypt(new RSADecrypter(decipherCred.getKey()));
        final SignedJWT signedJWT = jweObject.getPayload().toSignedJWT();
        assertThat(signedJWT, notNullValue());
        assertTrue(signedJWT.verify(new RSASSAVerifier(decipherCred.getValue())));
    }

    @Test
    public void when_cypher_a_fresh_token_then_set_it_as_cyphered() {
        token.cypher();
        assertThat(token.getStatus(), equalTo(TokenStatus.CYPHERED));
    }

    @Test(expected = TokenAlreadyCommitedException.class)
    public void when_cypher_a_fresh_token_and_decypher_then_error() {
        token.cypher();
        token.decipher();
    }

    @Test(expected = TokenNotCipheredException.class)
    public void when_serialize_a_non_cyphered_token_then_error() {
        token.serialize();
    }

    private static String randomString() {
        final int length = (new Random()).nextInt(50);
        return RandomStringUtils.random(length > 0 ? length : 50, true, true);
    }
}
