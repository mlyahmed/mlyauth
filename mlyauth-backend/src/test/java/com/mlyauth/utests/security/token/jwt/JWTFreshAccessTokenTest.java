package com.mlyauth.utests.security.token.jwt;

import com.mlyauth.security.token.jwt.JWTAccessToken;
import com.mlyauth.tools.KeysForTests;
import javafx.util.Pair;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

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
}
