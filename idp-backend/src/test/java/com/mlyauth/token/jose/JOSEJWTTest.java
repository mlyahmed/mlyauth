package com.mlyauth.token.jose;

import com.mlyauth.tools.KeysForTests;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import javafx.util.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.crypto.SecretKey;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.Date;
import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class JOSEJWTTest {

    public static final String PAYLOAD_STRING_EXAMPLE = "A text to check the process";
    public static final String ISSUER_URI = "https://idp.prima-solutions.com";
    public static final String SUB = "BA0000000000001";
    public static final String AUD_ID = "https://policy.clients.boursorama-assurances.com";

    private JWTClaimsSet claims;

    @Before
    public void setup() {
        claims = new JWTClaimsSet.Builder()
                .issuer(ISSUER_URI)
                .subject(SUB)
                .audience(AUD_ID)
                .expirationTime(new Date(System.currentTimeMillis() + 1000 * 60 * 10))
                .notBeforeTime(new Date())
                .issueTime(new Date())
                .jwtID(UUID.randomUUID().toString())
                .build();
    }

    @Test
    public void the_way_to_sign_a_token_using_RSA() throws JOSEException, ParseException {
        final Pair<PrivateKey, X509Certificate> pair = KeysForTests.generateRSACredential();
        JWSObject tokenHolder = new JWSObject(new JWSHeader(JWSAlgorithm.RS256), new Payload(PAYLOAD_STRING_EXAMPLE));
        tokenHolder.sign(new RSASSASigner(pair.getKey()));
        String signedToken = tokenHolder.serialize();
        Assert.assertThat(signedToken, notNullValue());
        Assert.assertThat(signedToken.split("\\.").length, equalTo(3));

        tokenHolder = JWSObject.parse(signedToken);
        JWSVerifier verifier = new RSASSAVerifier((RSAPublicKey) pair.getValue().getPublicKey());
        assertTrue(tokenHolder.verify(verifier));
        assertEquals(PAYLOAD_STRING_EXAMPLE, tokenHolder.getPayload().toString());
    }

    @Test
    public void the_way_to_encrypt_a_token_using_RSA() throws Exception {
        final Pair<PrivateKey, X509Certificate> pair = KeysForTests.generateRSACredential();
        JWEHeader header = new JWEHeader(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A128GCM);
        EncryptedJWT tokenHolder = new EncryptedJWT(header, claims);
        tokenHolder.encrypt(new RSAEncrypter((RSAPublicKey) pair.getValue().getPublicKey()));
        tokenHolder = EncryptedJWT.parse(tokenHolder.serialize());
        tokenHolder.decrypt(new RSADecrypter(pair.getKey()));
    }

    @Test
    public void sign_And_Encrypt_Using_AES_Token() throws Exception {
        SecretKey secretKey = KeysForTests.generateAES256SecretKey();
        SignedJWT tokenSigned = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        tokenSigned.sign(new MACSigner(secretKey.getEncoded()));
        JWEObject tokenEncrypted = new JWEObject(
                new JWEHeader.Builder(JWEAlgorithm.DIR, EncryptionMethod.A256GCM).contentType("JWT").build(),
                new Payload(tokenSigned));
        tokenEncrypted.encrypt(new DirectEncrypter(secretKey.getEncoded()));
        tokenEncrypted = JWEObject.parse(tokenEncrypted.serialize());
        tokenEncrypted.decrypt(new DirectDecrypter(secretKey.getEncoded()));
        tokenSigned = tokenEncrypted.getPayload().toSignedJWT();
        assertNotNull("Payload not a signed JWT", tokenSigned);
        assertTrue(tokenSigned.verify(new MACVerifier(secretKey.getEncoded())));
        assertEquals(SUB, tokenSigned.getJWTClaimsSet().getSubject());
    }


    @Test
    public void sign_and_encrypt_using_rsa() throws Exception {
        final Pair<PrivateKey, X509Certificate> issuerCredential = KeysForTests.generateRSACredential();
        final Pair<PrivateKey, X509Certificate> audienceCredential = KeysForTests.generateRSACredential();
        SignedJWT tokenSigned = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claims);
        tokenSigned.sign(new RSASSASigner(issuerCredential.getKey()));
        JWEObject tokenEncrypted = new JWEObject(
                new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A128GCM).contentType("JWT").build(),
                new Payload(tokenSigned));
        tokenEncrypted.encrypt(new RSAEncrypter((RSAPublicKey) audienceCredential.getValue().getPublicKey()));


        //Peer side
        JWEObject jweObject = JWEObject.parse(tokenEncrypted.serialize());
        jweObject.decrypt(new RSADecrypter(audienceCredential.getKey()));
        final SignedJWT signedJWT = jweObject.getPayload().toSignedJWT();
        assertNotNull("Payload not a signed JWT", signedJWT);
        assertTrue(signedJWT.verify(new RSASSAVerifier((RSAPublicKey) issuerCredential.getValue().getPublicKey())));
        assertEquals(SUB, signedJWT.getJWTClaimsSet().getSubject());
    }

}
