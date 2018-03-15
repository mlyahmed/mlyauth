package com.mlyauth.token.jose;

import com.mlyauth.token.Claims;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

import java.security.PrivateKey;

@Component
public class JOSEHelper {

    public String loadIssuer(String encodedToken, PrivateKey key) {
        try {
            EncryptedJWT tokenHolder = EncryptedJWT.parse(encodedToken);
            tokenHolder.decrypt(new RSADecrypter(key));
            final SignedJWT signedJWT = tokenHolder.getPayload().toSignedJWT();
            return (String) signedJWT.getHeader().getCustomParam(Claims.ISSUER.getValue());
        } catch (Exception e) {
            throw new BadCredentialsException("Couldn't verify the credentials", e);
        }
    }

}
