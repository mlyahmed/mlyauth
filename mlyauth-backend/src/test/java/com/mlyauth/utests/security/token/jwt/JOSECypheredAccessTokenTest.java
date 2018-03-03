package com.mlyauth.utests.security.token.jwt;

import com.mlyauth.constants.TokenVerdict;
import com.mlyauth.security.token.ExtraClaims;
import com.mlyauth.security.token.jwt.JOSEAccessToken;
import com.mlyauth.tools.KeysForTests;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import javafx.util.Pair;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;

public class JOSECypheredAccessTokenTest {

    private JOSEAccessToken token;
    private Pair<PrivateKey, RSAPublicKey> cypherCred;
    private Pair<PrivateKey, RSAPublicKey> decipherCred;
    private JWTClaimsSet expectedClaims;

    @Test
    public void when_given_cyphered_token_then_load_it() throws JOSEException {
        final Pair<PrivateKey, X509Certificate> peerCred = KeysForTests.generateRSACredential();
        final Pair<PrivateKey, X509Certificate> localCred = KeysForTests.generateRSACredential();
        cypherCred = new Pair<>(localCred.getKey(), (RSAPublicKey) peerCred.getValue().getPublicKey());
        decipherCred = new Pair<>(peerCred.getKey(), (RSAPublicKey) localCred.getValue().getPublicKey());


        expectedClaims = new JWTClaimsSet.Builder()
                .jwtID(UUID.randomUUID().toString())
                .subject(randomString())
                .claim(ExtraClaims.BP.getValue(), randomString())
                .claim(ExtraClaims.STATE.getValue(), randomString())
                .issuer(randomString())
                .audience(randomString())
                .claim(ExtraClaims.TARGET_URL.getValue(), randomString())
                .claim(ExtraClaims.DELEGATOR.getValue(), randomString())
                .claim(ExtraClaims.DELEGATE.getValue(), randomString())
                .claim(ExtraClaims.VERDICT.getValue(), TokenVerdict.SUCCESS)
                .expirationTime(new Date(System.currentTimeMillis() + 1000 * 60 * 10))
                .notBeforeTime(new Date())
                .issueTime(new Date())
                .build();

        SignedJWT tokenSigned = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), expectedClaims);
        tokenSigned.sign(new RSASSASigner(cypherCred.getKey()));
        final JWEHeader header = new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A128GCM).build();
        JWEObject tokenEncrypted = new JWEObject(header, new Payload(tokenSigned));
        tokenEncrypted.encrypt(new RSAEncrypter(cypherCred.getValue()));

        token = new JOSEAccessToken(tokenEncrypted.serialize(), decipherCred.getKey(), decipherCred.getValue());

        token.decipher();

        Assert.assertThat(token.getId(), equalTo(expectedClaims.getJWTID()));
        Assert.assertThat(token.getSubject(), equalTo(expectedClaims.getSubject()));
        Assert.assertThat(token.getSubject(), equalTo(expectedClaims.getSubject()));
    }

    private static String randomString() {
        final int length = (new Random()).nextInt(50);
        return RandomStringUtils.random(length > 0 ? length : 50, true, true);
    }
}
