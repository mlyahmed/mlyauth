package com.mlyauth.token.jose;

import com.mlyauth.constants.TokenNorm;
import com.mlyauth.constants.TokenStatus;
import com.mlyauth.exception.JOSEErrorException;
import com.mlyauth.exception.TokenNotCipheredException;
import com.mlyauth.token.AbstractToken;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;

import static com.mlyauth.constants.TokenStatus.CYPHERED;
import static com.mlyauth.token.Claims.ISSUER;
import static com.nimbusds.jose.EncryptionMethod.A128GCM;
import static com.nimbusds.jose.JWEAlgorithm.RSA_OAEP_256;
import static com.nimbusds.jose.JWSAlgorithm.RS256;

public abstract class AbstractJOSEToken extends AbstractToken {

    protected TokenStatus status = TokenStatus.FRESH;

    protected PrivateKey privateKey;
    protected PublicKey publicKey;

    protected JWEObject token;
    protected JWTClaimsSet.Builder builder;

    @Override
    public String getStamp() {
        return builder.build().getJWTID();
    }

    @Override
    public void setStamp(String stamp) {
        checkUnmodifiable();
        builder = builder.jwtID(stamp);
        status = TokenStatus.FORGED;
    }

    @Override
    public TokenStatus getStatus() {
        return status;
    }

    @Override
    public TokenNorm getNorm() {
        return TokenNorm.JOSE;
    }

    @Override
    public void cypher() {
        try {
            signAndEncrypt();
            status = CYPHERED;
            committed = true;
        } catch (Exception e) {
            throw JOSEErrorException.newInstance(e);
        }
    }

    private void signAndEncrypt() throws JOSEException {
        SignedJWT tokenSigned = new SignedJWT(buildJWSHeader(), builder.build());
        tokenSigned.sign(new RSASSASigner(privateKey));
        token = new JWEObject(new JWEHeader.Builder(RSA_OAEP_256, A128GCM).keyID(getAudience()).build(), new Payload(tokenSigned));
        token.encrypt(new RSAEncrypter((RSAPublicKey) publicKey));
    }

    private JWSHeader buildJWSHeader() {
        JWSHeader.Builder headerBuilder = new JWSHeader.Builder(RS256).customParam(ISSUER.getValue(), getIssuer());
        return headerBuilder.build();
    }

    @Override
    public String serialize() {
        if (getStatus() != CYPHERED)
            throw TokenNotCipheredException.newInstance();
        return token.serialize();
    }
}
