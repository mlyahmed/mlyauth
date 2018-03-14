package com.primasolutions.idp.sample.idp;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.saml.key.KeyManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.UUID;

@Controller
public class IDPJOSEController {

    @Autowired
    private KeyManager keyManager;


    @GetMapping("/idp-jose")
    public String greetingForm(Model model) {
        model.addAttribute("token", new Token());
        return "idp-form";
    }

    @PostMapping("/idp-jose")
    public String greetingSubmit(@ModelAttribute Token token, Model model, HttpServletResponse response) {
        Navigation navigation = new Navigation();
        navigation.setTarget("http://localhost:16666/sp/jose/sso");
        model.addAttribute("navigation", navigation);
        Cookie foo = new Cookie("Bearer", generateJOSEAccess(token));
        foo.setMaxAge(30);
        response.addCookie(foo);
        return "idp-navigation";
    }





    private String generateJOSEAccess(Token token){
        try{
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .issuer("sample-idp")
                    .subject("1")
                    .audience("primainsureIDP")
                    .expirationTime(new Date(System.currentTimeMillis() + 1000 * 60 * 10))
                    .notBeforeTime(new Date())
                    .claim("scopes", "PERSON")
                    .claim("bp", "SSO")
                    .claim("state", "SSO")
                    .claim("targetURL", "http://localhost:16666/sp/jose/sso")
                    .claim("delegator", "1")
                    .claim("delegate", "sample-idp")
                    .claim("verdict", "SUCCESS")
                    .claim("idClient", "1")
                    .claim("profilUtilisateur", "CL")
                    .claim("idPrestation", "BA00000000000212")
                    .claim("action", "S")
                    .claim("application", "PolicySB")
                    .issueTime(new Date())
                    .jwtID(UUID.randomUUID().toString())
                    .build();

            final PublicKey publicKey = keyManager.getCertificate("sgi.prima-solutions.com").getPublicKey();
            final PrivateKey privateKey = keyManager.getDefaultCredential().getPrivateKey();

            SignedJWT tokenSigned = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .customParam("iss", "sample-idp").build(), claims);
            tokenSigned.sign(new RSASSASigner(privateKey));

            JWEObject tokenEncrypted = new JWEObject(
                    new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A128GCM).build(),
                    new Payload(tokenSigned));

            tokenEncrypted.encrypt(new RSAEncrypter((RSAPublicKey) publicKey));

            return tokenEncrypted.serialize();

        }catch(Exception e){
            throw new RuntimeException(e);
        }

    }

}
