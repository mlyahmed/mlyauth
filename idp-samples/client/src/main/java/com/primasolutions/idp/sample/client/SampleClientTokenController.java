package com.primasolutions.idp.sample.client;

import com.nimbusds.jose.JWEHeader.Builder;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.io.IOUtils;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.saml.key.KeyManager;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.Charset;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;

import static com.nimbusds.jose.EncryptionMethod.A128GCM;
import static com.nimbusds.jose.JWEAlgorithm.RSA_OAEP_256;

@RestController
@RequestMapping("/token")
public class SampleClientTokenController {

    @Value("${cl.jose.peerRefreshEndpoint}")
    private String peerRefreshEndpoint;

    @Value("${cl.jose.login}")
    private String login;

    @Value("${cl.jose.password}")
    private String password;


    @Autowired
    private KeyManager keyManager;

    @Autowired
    private SampleClientJOSETokenInitializer tokenInitializer;

    @PostMapping("/refreshAccess")
    public String refreshAccess() throws Exception {
        final SampleClientToken token = tokenInitializer.newToken();
        JWTClaimsSet.Builder claims = new JWTClaimsSet.Builder()
                .issuer(token.getIssuer())
                .subject(token.getSubject())
                .audience(token.getAudience())
                .expirationTime(new Date(System.currentTimeMillis() + 1000 * 60 * 10))
                .notBeforeTime(new Date())
                .issueTime(new Date())
                .jwtID(token.getId());

        final PublicKey publicKey = keyManager.getCertificate("sgi.prima-solutions.com").getPublicKey();
        final PrivateKey privateKey = keyManager.getDefaultCredential().getPrivateKey();
        SignedJWT tokenSigned = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256)
                .customParam("iss", token.getIssuer()).build(), claims.build());
        tokenSigned.sign(new RSASSASigner(privateKey));
        JWEObject tokenEncrypted = new JWEObject(new Builder(RSA_OAEP_256, A128GCM).build(), new Payload(tokenSigned));
        tokenEncrypted.encrypt(new RSAEncrypter((RSAPublicKey) publicKey));
        final String serialized = tokenEncrypted.serialize();

        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(peerRefreshEndpoint);

        httpPost.setEntity(new StringEntity(serialized));
        UsernamePasswordCredentials creds
                = new UsernamePasswordCredentials(login, password);
        httpPost.addHeader(new BasicScheme().authenticate(creds, httpPost, null));
        CloseableHttpResponse response = client.execute(httpPost);
        Assert.isTrue( response.getStatusLine().getStatusCode() == 201, "");
        String access = IOUtils.toString(response.getEntity().getContent(), Charset.forName("UTF-8"));
        client.close();
        return access;
    }

}
