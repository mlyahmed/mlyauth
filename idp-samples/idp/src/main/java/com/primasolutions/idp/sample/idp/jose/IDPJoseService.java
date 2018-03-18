package com.primasolutions.idp.sample.idp.jose;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.primasolutions.idp.sample.idp.SampleIDPToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.saml.key.KeyManager;
import org.springframework.stereotype.Component;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.UUID;

@Component
public class IDPJoseService {

    @Autowired
    private KeyManager keyManager;

    public String generateJOSEAccess(SampleIDPToken token) {
        try {
            JWTClaimsSet.Builder claims = new JWTClaimsSet.Builder()
                    .issuer(token.getIssuer())
                    .subject(token.getSubject())
                    .audience(token.getAudience())
                    .expirationTime(new Date(System.currentTimeMillis() + 1000 * 60 * 10))
                    .notBeforeTime(new Date())
                    .claim("scopes", token.getScopes())
                    .claim("bp", token.getBp())
                    .claim("state", token.getState())
                    .claim("targetURL", token.getTargetURL())
                    .claim("delegator", token.getDelegator())
                    .claim("delegate", token.getDelegate())
                    .claim("verdict", token.getVerdict())
                    .claim("idClient", token.getClientId())
                    .claim("profilUtilisateur", token.getClientProfile())
                    .claim("idPrestation", token.getEntityId())
                    .claim("action", token.getAction())
                    .claim("application", token.getApplication())
                    .issueTime(new Date())
                    .jwtID(UUID.randomUUID().toString());

            final PublicKey publicKey = keyManager.getCertificate("sgi.prima-solutions.com").getPublicKey();
            final PrivateKey privateKey = keyManager.getDefaultCredential().getPrivateKey();

            SignedJWT tokenSigned = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .customParam("iss", "sample-idp").build(), claims.build());
            tokenSigned.sign(new RSASSASigner(privateKey));

            JWEObject tokenEncrypted = new JWEObject(
                    new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A128GCM).build(),
                    new Payload(tokenSigned));

            tokenEncrypted.encrypt(new RSAEncrypter((RSAPublicKey) publicKey));

            return tokenEncrypted.serialize();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
