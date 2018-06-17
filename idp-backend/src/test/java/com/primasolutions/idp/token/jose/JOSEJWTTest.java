package com.primasolutions.idp.token.jose;

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
import com.primasolutions.idp.credentials.CredentialsPair;
import com.primasolutions.idp.tools.KeysForTests;
import org.junit.Before;
import org.junit.Test;

import javax.crypto.SecretKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.Date;
import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class JOSEJWTTest {

    private static final String PAYLOAD_STRING_EXAMPLE = "A text to check the process";
    private static final String ISSUER_URI = "https://idp.prima-solutions.com";
    private static final String SUB = "BA0000000000001";
    private static final String AUD_ID = "https://policy.clients.boursorama-assurances.com";
    private static final int TEN_MINUTES = 1000 * 60 * 10;

    private JWTClaimsSet claims;

    @Before
    public void setup() {
        claims = new JWTClaimsSet.Builder()
                .issuer(ISSUER_URI)
                .subject(SUB)
                .audience(AUD_ID)
                .expirationTime(new Date(System.currentTimeMillis() + TEN_MINUTES))
                .notBeforeTime(new Date())
                .issueTime(new Date())
                .jwtID(UUID.randomUUID().toString())
                .build();
    }

    @Test
    public void the_way_to_sign_a_token_using_RSA() throws JOSEException, ParseException {
        final CredentialsPair pair = KeysForTests.generateRSACredential();
        JWSObject tokenHolder = new JWSObject(new JWSHeader(JWSAlgorithm.RS256), new Payload(PAYLOAD_STRING_EXAMPLE));
        tokenHolder.sign(new RSASSASigner(pair.getPrivateKey()));
        String signedToken = tokenHolder.serialize();
        assertThat(signedToken, notNullValue());
        //CHECKSTYLE:OFF
        assertThat(signedToken.split("\\.").length, equalTo(3));
        //CHECKSTYLE:ON
        tokenHolder = JWSObject.parse(signedToken);
        JWSVerifier verifier = new RSASSAVerifier((RSAPublicKey) pair.getPublicKey());
        assertTrue(tokenHolder.verify(verifier));
        assertEquals(PAYLOAD_STRING_EXAMPLE, tokenHolder.getPayload().toString());
    }

    @Test
    public void the_way_to_encrypt_a_token_using_RSA() throws Exception {
        final CredentialsPair pair = KeysForTests.generateRSACredential();
        JWEHeader header = new JWEHeader(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A128GCM);
        EncryptedJWT tokenHolder = new EncryptedJWT(header, claims);
        tokenHolder.encrypt(new RSAEncrypter((RSAPublicKey) pair.getPublicKey()));
        tokenHolder = EncryptedJWT.parse(tokenHolder.serialize());
        tokenHolder.decrypt(new RSADecrypter(pair.getPrivateKey()));
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
        final CredentialsPair issuerCredential = KeysForTests.generateRSACredential();
        final CredentialsPair audienceCredential = KeysForTests.generateRSACredential();
        SignedJWT tokenSigned = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claims);
        tokenSigned.sign(new RSASSASigner(issuerCredential.getPrivateKey()));
        JWEObject tokenEncrypted = new JWEObject(
                new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A128GCM).contentType("JWT").build(),
                new Payload(tokenSigned));
        tokenEncrypted.encrypt(new RSAEncrypter((RSAPublicKey) audienceCredential.getPublicKey()));


        //Peer side
        JWEObject jweObject = JWEObject.parse(tokenEncrypted.serialize());
        jweObject.decrypt(new RSADecrypter(audienceCredential.getPrivateKey()));
        final SignedJWT signedJWT = jweObject.getPayload().toSignedJWT();
        assertNotNull("Payload not a signed JWT", signedJWT);
        assertTrue(signedJWT.verify(new RSASSAVerifier((RSAPublicKey) issuerCredential.getPublicKey())));
        assertEquals(SUB, signedJWT.getJWTClaimsSet().getSubject());
    }

}
