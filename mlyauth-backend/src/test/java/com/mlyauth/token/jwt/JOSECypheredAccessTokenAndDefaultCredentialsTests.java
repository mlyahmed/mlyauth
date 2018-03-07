package com.mlyauth.token.jwt;

import com.mlyauth.constants.TokenScope;
import com.mlyauth.constants.TokenStatus;
import com.mlyauth.constants.TokenVerdict;
import com.mlyauth.key.CredentialManager;
import com.mlyauth.key.MockCredentialManager;
import com.mlyauth.token.IDPClaims;
import com.mlyauth.token.jose.JOSEAccessToken;
import com.mlyauth.tools.KeysForTests;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import javafx.util.Pair;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.mlyauth.token.IDPClaims.SCOPES;
import static com.mlyauth.token.IDPClaims.STATE;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class JOSECypheredAccessTokenAndDefaultCredentialsTests {

    private CredentialManager credentialManager;
    private JOSEAccessToken token;
    private JWTClaimsSet expectedClaims;
    private JWEObject tokenEncrypted;
    private KeyPair cypherCred;
    private KeyPair decipherCred;

    @Test
    public void the_token_status_must_be_cyphered() throws JOSEException {

        final Pair<PrivateKey, X509Certificate> peerCred = KeysForTests.generateRSACredential();
        final Pair<PrivateKey, X509Certificate> localCred = KeysForTests.generateRSACredential();
        cypherCred = new KeyPair(peerCred.getValue().getPublicKey(), localCred.getKey());
        decipherCred = new KeyPair(localCred.getValue().getPublicKey(), peerCred.getKey());
        credentialManager = new MockCredentialManager(decipherCred.getPrivate(), decipherCred.getPublic());
        given_expected_claims();
        given_the_claims_are_cyphered();

        token = new JOSEAccessToken(tokenEncrypted.serialize());
        ReflectionTestUtils.setField(token, "credentialManager", credentialManager);
        assertThat(token.getStatus(), equalTo(TokenStatus.CYPHERED));
    }


    private void given_expected_claims() {
        expectedClaims = new JWTClaimsSet.Builder()
                .jwtID(UUID.randomUUID().toString())
                .subject(randomString())
                .claim(SCOPES.getValue(), Arrays.stream(TokenScope.values()).map(TokenScope::name)
                        .collect(Collectors.joining("|")))
                .claim(IDPClaims.BP.getValue(), randomString())
                .claim(STATE.getValue(), randomString())
                .issuer(randomString())
                .audience(randomString())
                .claim(IDPClaims.TARGET_URL.getValue(), randomString())
                .claim(IDPClaims.DELEGATOR.getValue(), randomString())
                .claim(IDPClaims.DELEGATE.getValue(), randomString())
                .claim(IDPClaims.VERDICT.getValue(), TokenVerdict.SUCCESS)
                .expirationTime(new Date(System.currentTimeMillis() + 1000 * 60 * 3))
                .notBeforeTime(new Date())
                .issueTime(new Date())
                .build();
    }

    private void given_the_claims_are_cyphered() throws JOSEException {
        SignedJWT tokenSigned = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), expectedClaims);
        tokenSigned.sign(new RSASSASigner(cypherCred.getPrivate()));
        final JWEHeader header = new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A128GCM).build();
        tokenEncrypted = new JWEObject(header, new Payload(tokenSigned));
        tokenEncrypted.encrypt(new RSAEncrypter((RSAPublicKey) cypherCred.getPublic()));
    }

    private static String randomString() {
        final int length = (new Random()).nextInt(50);
        return RandomStringUtils.random(length > 0 ? length : 50, true, true);
    }
}
