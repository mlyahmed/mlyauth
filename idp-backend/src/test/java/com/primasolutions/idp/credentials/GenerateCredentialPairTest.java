package com.primasolutions.idp.credentials;

import com.primasolutions.idp.tools.KeysForTests;
import javafx.util.Pair;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import static org.junit.Assert.assertThat;

public class GenerateCredentialPairTest {

    @Test
    public void when_encode_a_pair_then_it_can_be_decoded() {
        final Pair<PrivateKey, X509Certificate> credential = KeysForTests.generateRSACredential();

        String privateString = KeysForTests.encodePrivateKey(credential.getKey());
        String certificateString = KeysForTests.encodeCertificate(credential.getValue());
        String publicString = KeysForTests.encodePublicKey(credential.getValue().getPublicKey());

        final Certificate certificate = KeysForTests.decodeCertificate(certificateString);
        PublicKey publicKey = KeysForTests.decodeRSAPublicKey(publicString);
        PrivateKey privateKey = KeysForTests.decodeRSAPrivateKey(privateString);

        assertThat(credential.getKey(), Matchers.equalTo(privateKey));
        assertThat(credential.getValue(), Matchers.equalTo(certificate));
        assertThat(credential.getValue().getPublicKey(), Matchers.equalTo(publicKey));
    }

}
