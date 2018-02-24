package com.mlyauth.utests.security.jwt;

import com.mlyauth.tools.KeysForTests;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import javafx.util.Pair;
import org.junit.Test;
import sun.security.rsa.RSAPublicKeyImpl;

import javax.crypto.SecretKey;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import static org.junit.Assert.*;

public class JWTTest {

    @Test
    public void signAToken() throws JOSEException, InvalidKeyException, ParseException {
        final Pair<PrivateKey, X509Certificate> pair = KeysForTests.generateRSACredential();

        JWSObject jwsObject = new JWSObject(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID("123").build(),
                new Payload("In RSA we trust!"));

        jwsObject.sign(new RSASSASigner(pair.getKey()));
        String s = jwsObject.serialize();

        jwsObject = JWSObject.parse(s);
        JWSVerifier verifier = new RSASSAVerifier(new RSAPublicKeyImpl(pair.getValue().getPublicKey().getEncoded()));

        assertTrue(jwsObject.verify(verifier));
        assertEquals("In RSA we trust!", jwsObject.getPayload().toString());
    }

    @Test
    public void encryptAToken() throws Exception {
        final Pair<PrivateKey, X509Certificate> pair = KeysForTests.generateRSACredential();

        JWTClaimsSet jwtClaims = new JWTClaimsSet.Builder()
                .issuer("https://idp.prima-solutions.com")
                .subject("alice")
                .audience(Arrays.asList("https://policy.clients.boursorama-assurances.com",
                        "https://claims.clients.boursorama-assurances.com"))
                .expirationTime(new Date(System.currentTimeMillis() + 1000 * 60 * 10)) // expires in 10 minutes
                .notBeforeTime(new Date())
                .issueTime(new Date())
                .jwtID(UUID.randomUUID().toString())
                .build();
        System.out.println(jwtClaims.toJSONObject());

        JWEHeader header = new JWEHeader(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A128GCM);
        EncryptedJWT jwt = new EncryptedJWT(header, jwtClaims);
        jwt.encrypt(new RSAEncrypter(new RSAPublicKeyImpl(pair.getValue().getPublicKey().getEncoded())));
        String jwtString = jwt.serialize();
        System.out.println(jwtString);

        jwt = EncryptedJWT.parse(jwtString);
        jwt.decrypt(new RSADecrypter(pair.getKey()));
        System.out.println(jwt.getJWTClaimsSet().getIssuer());
        ;
        System.out.println(jwt.getJWTClaimsSet().getSubject());
        System.out.println(jwt.getJWTClaimsSet().getAudience().size());
        System.out.println(jwt.getJWTClaimsSet().getExpirationTime());
        System.out.println(jwt.getJWTClaimsSet().getNotBeforeTime());
        System.out.println(jwt.getJWTClaimsSet().getIssueTime());
        System.out.println(jwt.getJWTClaimsSet().getJWTID());
    }

    @Test
    public void sign_And_Encrypt_Using_AES_Token() throws Exception {
        SecretKey secretKey = KeysForTests.generateAES256SecretKey();
        JWSSigner signer = new MACSigner(secretKey.getEncoded());

        JWTClaimsSet jwtClaims = new JWTClaimsSet.Builder()
                .issuer("https://idp.prima-solutions.com")
                .subject("alice")
                .audience(Arrays.asList("https://policy.clients.boursorama-assurances.com",
                        "https://claims.clients.boursorama-assurances.com"))
                .expirationTime(new Date(System.currentTimeMillis() + 1000 * 60 * 10)) // expires in 10 minutes
                .notBeforeTime(new Date())
                .issueTime(new Date())
                .jwtID(UUID.randomUUID().toString())
                .build();

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), jwtClaims);
        signedJWT.sign(signer);


        JWEObject jweObject = new JWEObject(
                new JWEHeader.Builder(JWEAlgorithm.DIR, EncryptionMethod.A256GCM)
                        .contentType("JWT") // required to signal nested JWT
                        .build(),
                new Payload(signedJWT));

        jweObject.encrypt(new DirectEncrypter(secretKey.getEncoded()));
        String jweString = jweObject.serialize();


        jweObject = JWEObject.parse(jweString);
        jweObject.decrypt(new DirectDecrypter(secretKey.getEncoded()));
        signedJWT = jweObject.getPayload().toSignedJWT();

        assertNotNull("Payload not a signed JWT", signedJWT);
        assertTrue(signedJWT.verify(new MACVerifier(secretKey.getEncoded())));
        assertEquals("alice", signedJWT.getJWTClaimsSet().getSubject());

    }


}
