package com.mlyauth.utests.security.token.jwt;

import com.mlyauth.constants.TokenNorm;
import com.mlyauth.constants.TokenStatus;
import com.mlyauth.constants.TokenType;
import com.mlyauth.security.token.jwt.JWTAccessToken;
import com.mlyauth.tools.KeysForTests;
import javafx.util.Pair;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class JWTFreshAccessTokenTest {

    @Test(expected = IllegalArgumentException.class)
    public void when_create_a_fresh_token_and_private_key_is_null_then_error() {
        final Pair<PrivateKey, X509Certificate> credential = KeysForTests.generateRSACredential();
        new JWTAccessToken(null, credential.getValue().getPublicKey());
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_create_a_fresh_token_and_public_key_is_null_then_error() {
        final Pair<PrivateKey, X509Certificate> credential = KeysForTests.generateRSACredential();
        new JWTAccessToken(credential.getKey(), null);
    }

    @Test
    public void when_create_fresh_response_then_token_claims_must_be_fresh() {
        final Pair<PrivateKey, X509Certificate> credential = KeysForTests.generateRSACredential();
        JWTAccessToken token = new JWTAccessToken(credential.getKey(), credential.getValue().getPublicKey());
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
}
