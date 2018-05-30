package com.primasolutions.idp.token;

import com.primasolutions.idp.constants.TokenScope;
import com.primasolutions.idp.constants.TokenVerdict;
import com.primasolutions.idp.token.jose.JOSEAccessToken;
import com.primasolutions.idp.token.saml.SAMLAccessToken;
import com.primasolutions.idp.token.saml.SAMLHelper;
import com.primasolutions.idp.tools.KeysForTests;
import com.primasolutions.idp.tools.RandomForTests;
import javafx.util.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opensaml.DefaultBootstrap;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.security.credential.Credential;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashSet;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@DisplayName("Token Compression")
class TokenCompressorTest {


    private TokenCompressor compressor;

    @BeforeEach
    void setUp() {
        compressor = new TokenCompressor();
    }

    @Test
    @DisplayName("Compress a SAML Token")
    void when_compress_a_saml_token_then_compress_it() throws IOException, ConfigurationException {
        SAMLAccessToken samlAccessToken = given_cyphered_saml_token();
        final String serialize = samlAccessToken.serialize();
        final byte[] compress = compressor.compress(serialize);
        assertTrue(serialize.length() > compress.length);
        assertThat(serialize, equalTo(compressor.decompress(compress)));
    }

    @Test
    void when_compress_a_token_then_compress_it() throws IOException {
        JOSEAccessToken joseAccess = given_cyphered_jose_token();
        final String serialize = joseAccess.serialize();
        final byte[] compress = compressor.compress(serialize);
        assertTrue(serialize.length() > compress.length);
        assertThat(serialize, equalTo(compressor.decompress(compress)));
    }

    private SAMLAccessToken given_cyphered_saml_token() throws ConfigurationException {
        DefaultBootstrap.bootstrap();
        SAMLHelper samlHelper = new SAMLHelper();
        final Pair<PrivateKey, X509Certificate> pair = KeysForTests.generateRSACredential();
        final Credential credential = samlHelper.toCredential(pair.getKey(), pair.getValue());
        SAMLAccessToken samlAccessToken = new SAMLAccessToken(credential);
        samlAccessToken.setStamp(RandomForTests.randomString());
        samlAccessToken.setSubject(RandomForTests.randomString());
        samlAccessToken.setScopes(new HashSet<>(Arrays.asList(TokenScope.values())));
        samlAccessToken.setBP(RandomForTests.randomString());
        samlAccessToken.setState(RandomForTests.randomString());
        samlAccessToken.setIssuer(RandomForTests.randomString());
        samlAccessToken.setAudience(RandomForTests.randomString());
        samlAccessToken.setTargetURL(RandomForTests.randomString());
        samlAccessToken.setDelegator(RandomForTests.randomString());
        samlAccessToken.setDelegate(RandomForTests.randomString());
        samlAccessToken.setVerdict(TokenVerdict.SUCCESS);
        samlAccessToken.cypher();
        return samlAccessToken;
    }

    private JOSEAccessToken given_cyphered_jose_token() {
        final Pair<PrivateKey, X509Certificate> credential = KeysForTests.generateRSACredential();
        JOSEAccessToken access = new JOSEAccessToken(credential.getKey(), credential.getValue().getPublicKey());
        access.setStamp(RandomForTests.randomString());
        access.setSubject(RandomForTests.randomString());
        access.setScopes(new HashSet<>(Arrays.asList(TokenScope.values())));
        access.setBP(RandomForTests.randomString());
        access.setState(RandomForTests.randomString());
        access.setIssuer(RandomForTests.randomString());
        access.setAudience(RandomForTests.randomString());
        access.setTargetURL(RandomForTests.randomString());
        access.setDelegator(RandomForTests.randomString());
        access.setDelegate(RandomForTests.randomString());
        access.setVerdict(TokenVerdict.SUCCESS);
        access.cypher();
        return access;
    }

}
