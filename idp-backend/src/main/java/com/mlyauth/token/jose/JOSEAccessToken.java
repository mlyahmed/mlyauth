package com.mlyauth.token.jose;

import com.mlyauth.constants.TokenStatus;
import com.mlyauth.constants.TokenType;
import com.mlyauth.exception.InvalidTokenException;
import com.mlyauth.exception.JOSEErrorException;
import com.mlyauth.token.Claims;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static com.mlyauth.constants.TokenStatus.DECIPHERED;
import static org.springframework.util.Assert.notNull;

public class JOSEAccessToken extends AbstractJOSEToken {

    public JOSEAccessToken(PrivateKey privateKey, PublicKey publicKey) {
        notNull(privateKey, "The private key is mandatory");
        notNull(publicKey, "The public key is mandatory");
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        builder = new JWTClaimsSet.Builder();
        initTimes();
    }

    private void initTimes() {
        Instant threeMinutesAfter = LocalDateTime.now().plusSeconds(179).atZone(ZoneId.systemDefault()).toInstant();
        Instant aSecondAgo = LocalDateTime.now().minusSeconds(1).atZone(ZoneId.systemDefault()).toInstant();
        builder = builder.expirationTime(Date.from(threeMinutesAfter))
                .notBeforeTime(Date.from(aSecondAgo))
                .issueTime(Date.from(aSecondAgo));
    }

    public JOSEAccessToken(String serialize, PrivateKey privateKey, PublicKey publicKey) {
        notNull(serialize, "The cyphered token is mandatory");
        notNull(privateKey, "The private key is mandatory");
        notNull(publicKey, "The public key is mandatory");
        parseCipheredToken(serialize);
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        status = TokenStatus.CYPHERED;
        locked = true;
    }

    private void parseCipheredToken(String serialize) {
        try {
            token = JWEObject.parse(serialize);
        } catch (ParseException e) {
            throw JOSEErrorException.newInstance(e);
        }
    }

    @Override
    public TokenType getType() {
        return TokenType.ACCESS;
    }

    @Override
    public void decipher() {
        checkCommitted();
        try {
            builder = new JWTClaimsSet.Builder(decipherClaims().getJWTClaimsSet());
            status = DECIPHERED;
        } catch (JOSEException | ParseException e) {
            throw JOSEErrorException.newInstance(e);
        }
    }

    private SignedJWT decipherClaims() throws JOSEException, ParseException {
        token.decrypt(new RSADecrypter(privateKey));
        final SignedJWT signedJWT = token.getPayload().toSignedJWT();
        checkSignature(signedJWT);
        checkIssuerMatch(signedJWT);
        return signedJWT;
    }

    private void checkIssuerMatch(SignedJWT signedJWT) throws ParseException {
        if (!String.valueOf(signedJWT.getHeader().getCustomParam(Claims.ISSUER.getValue())).equals(signedJWT.getJWTClaimsSet().getIssuer()))
            throw InvalidTokenException.newInstance("Issuer mismatch");
    }

    private void checkSignature(SignedJWT signedJWT) throws JOSEException {
        if (signedJWT == null || !signedJWT.verify(new RSASSAVerifier((RSAPublicKey) publicKey)))
            throw JOSEErrorException.newInstance(new JOSEException("Failed to verify signature"));
    }

}
