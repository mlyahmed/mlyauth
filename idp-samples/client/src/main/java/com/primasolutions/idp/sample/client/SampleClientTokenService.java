package com.primasolutions.idp.sample.client;

import com.google.common.base.Stopwatch;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.apache.commons.io.IOUtils;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.saml.key.KeyManager;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.nio.charset.Charset;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;

import static com.nimbusds.jose.EncryptionMethod.A128GCM;
import static com.nimbusds.jose.JWEAlgorithm.RSA_OAEP_256;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static net.minidev.json.parser.JSONParser.MODE_JSON_SIMPLE;

@Service
public class SampleClientTokenService {
    private static Logger logger = LoggerFactory.getLogger(SampleClientTokenService.class);

    @Value("${cl.jose.peerRefreshEndpoint}")
    private String peerRefreshEndpoint;

    @Value("${cl.jose.login}")
    private String login;

    @Value("${cl.jose.password}")
    private String password;


    @Autowired
    private KeyManager keyManager;

    public SampleClientTokenBean refreshAccess(SampleClientToken token){

        try{

            JWTClaimsSet.Builder claims = new JWTClaimsSet.Builder()
                    .issuer(token.getIssuer())
                    .subject(token.getSubject())
                    .audience(token.getAudience())
                    .expirationTime(new Date(System.currentTimeMillis() + 1000 * 60 * 10))
                    .notBeforeTime(new Date())
                    .issueTime(new Date())
                    .claim("delegator", token.getDelegator())
                    .jwtID(token.getId());

            final PublicKey publicKey = keyManager.getCertificate("sgi.prima-solutions.com").getPublicKey();
            final PrivateKey privateKey = keyManager.getDefaultCredential().getPrivateKey();
            SignedJWT tokenSigned = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .customParam("iss", token.getIssuer()).build(), claims.build());
            tokenSigned.sign(new RSASSASigner(privateKey));
            JWEObject tokenEncrypted = new JWEObject(new JWEHeader.Builder(RSA_OAEP_256, A128GCM).build(), new Payload(tokenSigned));
            tokenEncrypted.encrypt(new RSAEncrypter((RSAPublicKey) publicKey));
            final String serialized = tokenEncrypted.serialize();

            CloseableHttpClient client = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(peerRefreshEndpoint);

            httpPost.setEntity(new StringEntity(serialized));
            UsernamePasswordCredentials creds = new UsernamePasswordCredentials(login, password);
            httpPost.addHeader(new BasicScheme().authenticate(creds, httpPost, null));

            final Stopwatch started = Stopwatch.createStarted();
            CloseableHttpResponse response = client.execute(httpPost);
            Assert.isTrue( response.getStatusLine().getStatusCode() == 201, "");
            logger.info("Refresh Elapsed time : ("+started.elapsed(MILLISECONDS)+" ms)");

            String access = IOUtils.toString(response.getEntity().getContent(), Charset.forName("UTF-8"));
            client.close();

            JSONParser parser = new JSONParser(MODE_JSON_SIMPLE);
            JSONObject jsonObject = (JSONObject)parser.parse(access);

            return new SampleClientTokenBean(jsonObject.getAsString("serialized"), jsonObject.getAsString("expiryTime"));

        }catch(Exception e){
            throw new RuntimeException(e);
        }


    }

}
