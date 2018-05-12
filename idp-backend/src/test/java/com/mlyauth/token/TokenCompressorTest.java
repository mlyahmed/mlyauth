package com.mlyauth.token;

import com.mlyauth.constants.TokenScope;
import com.mlyauth.constants.TokenVerdict;
import com.mlyauth.token.jose.JOSEAccessToken;
import com.mlyauth.token.saml.SAMLAccessToken;
import com.mlyauth.token.saml.SAMLHelper;
import javafx.util.Pair;
import org.junit.Before;
import org.junit.Test;
import org.opensaml.DefaultBootstrap;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.security.credential.Credential;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashSet;

import static com.mlyauth.tools.KeysForTests.generateRSACredential;
import static com.mlyauth.tools.RandomForTests.randomString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class TokenCompressorTest {


    private TokenCompressor compressor;

    @Before
    public void setUp() {
        compressor = new TokenCompressor();
    }

    @Test
    public void when_compress_a_saml_token_then_compress_it() throws IOException, ConfigurationException {
        SAMLAccessToken samlAccessToken = given_cyphered_saml_token();
        final String serialize = samlAccessToken.serialize();
        final byte[] compress = compressor.compress(serialize);
        assertTrue(serialize.length() > compress.length);
        assertThat(serialize, equalTo(compressor.decompress(compress)));
    }

    @Test
    public void when_compress_a_token_then_compress_it() throws IOException {
        JOSEAccessToken joseAccess = given_cyphered_jose_token();
        final String serialize = joseAccess.serialize();
        final byte[] compress = compressor.compress(serialize);
        assertTrue(serialize.length() > compress.length);
        assertThat(serialize, equalTo(compressor.decompress(compress)));
    }

    private SAMLAccessToken given_cyphered_saml_token() throws ConfigurationException {
        DefaultBootstrap.bootstrap();
        SAMLHelper samlHelper = new SAMLHelper();
        final Pair<PrivateKey, X509Certificate> pair = generateRSACredential();
        final Credential credential = samlHelper.toCredential(pair.getKey(), pair.getValue());
        SAMLAccessToken samlAccessToken = new SAMLAccessToken(credential);
        samlAccessToken.setStamp(randomString());
        samlAccessToken.setSubject(randomString());
        samlAccessToken.setScopes(new HashSet<>(Arrays.asList(TokenScope.values())));
        samlAccessToken.setBP(randomString());
        samlAccessToken.setState(randomString());
        samlAccessToken.setIssuer(randomString());
        samlAccessToken.setAudience(randomString());
        samlAccessToken.setTargetURL(randomString());
        samlAccessToken.setDelegator(randomString());
        samlAccessToken.setDelegate(randomString());
        samlAccessToken.setVerdict(TokenVerdict.SUCCESS);
        samlAccessToken.cypher();
        return samlAccessToken;
    }

    private JOSEAccessToken given_cyphered_jose_token() {
        final Pair<PrivateKey, X509Certificate> credential = generateRSACredential();
        JOSEAccessToken access = new JOSEAccessToken(credential.getKey(), credential.getValue().getPublicKey());
        access.setStamp(randomString());
        access.setSubject(randomString());
        access.setScopes(new HashSet<>(Arrays.asList(TokenScope.values())));
        access.setBP(randomString());
        access.setState(randomString());
        access.setIssuer(randomString());
        access.setAudience(randomString());
        access.setTargetURL(randomString());
        access.setDelegator(randomString());
        access.setDelegate(randomString());
        access.setVerdict(TokenVerdict.SUCCESS);
        access.cypher();
        return access;
    }

}
