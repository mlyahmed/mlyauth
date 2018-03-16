package com.mlyauth.token.jose;

import com.mlyauth.constants.TokenNorm;
import com.mlyauth.constants.TokenStatus;
import com.mlyauth.token.AbstractToken;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jwt.JWTClaimsSet;

import java.security.PrivateKey;
import java.security.PublicKey;

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
}
