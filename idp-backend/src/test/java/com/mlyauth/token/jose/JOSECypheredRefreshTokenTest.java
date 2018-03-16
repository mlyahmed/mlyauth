package com.mlyauth.token.jose;

import com.mlyauth.constants.TokenScope;
import com.mlyauth.constants.TokenStatus;
import com.mlyauth.constants.TokenVerdict;
import com.mlyauth.token.Claims;
import com.mlyauth.tools.KeysForTests;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import javafx.util.Pair;
import org.junit.Before;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.mlyauth.token.Claims.*;
import static com.mlyauth.tools.RandomForTests.randomString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class JOSECypheredRefreshTokenTest {

    private Pair<PrivateKey, PublicKey> cypherCred;
    private Pair<PrivateKey, PublicKey> decipherCred;
    private JOSERefreshToken refreshToken;
    private JWTClaimsSet refreshClaims;
    private JWEObject cyphered;

    @Before
    public void setup() throws JOSEException {
        final Pair<PrivateKey, X509Certificate> peerCred = KeysForTests.generateRSACredential();
        final Pair<PrivateKey, X509Certificate> localCred = KeysForTests.generateRSACredential();
        cypherCred = new Pair<>(localCred.getKey(), peerCred.getValue().getPublicKey());
        decipherCred = new Pair<>(peerCred.getKey(), localCred.getValue().getPublicKey());
        given_expected_refresh_claims();
        given_the_refresh_claims_are_cyphered();
    }

    @Test
    public void the_refresh_token_status_must_be_cyphered() {
        refreshToken = new JOSERefreshToken(cyphered.serialize(), decipherCred.getKey(), decipherCred.getValue());
        assertThat(refreshToken.getStatus(), equalTo(TokenStatus.CYPHERED));
    }

    private void given_expected_refresh_claims() {
        refreshClaims = new JWTClaimsSet.Builder()
                .jwtID(UUID.randomUUID().toString())
                .subject(randomString())
                .claim(SCOPES.getValue(), Arrays.stream(TokenScope.values()).map(TokenScope::name)
                        .collect(Collectors.joining("|")))
                .claim(Claims.BP.getValue(), randomString())
                .claim(STATE.getValue(), randomString())
                .issuer(randomString())
                .audience(randomString())
                .claim(Claims.TARGET_URL.getValue(), randomString())
                .claim(Claims.DELEGATOR.getValue(), randomString())
                .claim(Claims.DELEGATE.getValue(), randomString())
                .claim(Claims.VERDICT.getValue(), TokenVerdict.SUCCESS)
                .expirationTime(new Date(System.currentTimeMillis() + 1000 * 60 * 3))
                .notBeforeTime(new Date())
                .issueTime(new Date())
                .build();
    }

    @SuppressWarnings("Duplicates")
    private void given_the_refresh_claims_are_cyphered() throws JOSEException {
        final JWSHeader signatureHeader = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .customParam(ISSUER.getValue(), refreshClaims.getIssuer())
                .build();
        SignedJWT signature = new SignedJWT(signatureHeader, refreshClaims);
        signature.sign(new RSASSASigner(cypherCred.getKey()));
        final JWEHeader header = new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A128GCM).build();
        cyphered = new JWEObject(header, new Payload(signature));
        cyphered.encrypt(new RSAEncrypter((RSAPublicKey) cypherCred.getValue()));
    }

}
