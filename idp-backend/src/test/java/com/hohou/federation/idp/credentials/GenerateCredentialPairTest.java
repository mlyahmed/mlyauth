package com.hohou.federation.idp.credentials;

import com.hohou.federation.idp.tools.KeysForTests;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;

import static org.junit.Assert.assertThat;

public class GenerateCredentialPairTest {

    @Test
    public void when_encode_a_pair_then_it_can_be_decoded() {
        final CredentialsPair credential = KeysForTests.generateRSACredential();

        String privateString = KeysForTests.encodePrivateKey(credential.getPrivateKey());
        String certificateString = KeysForTests.encodeCertificate(credential.getCertificate());
        String publicString = KeysForTests.encodePublicKey(credential.getCertificate().getPublicKey());

        final Certificate certificate = KeysForTests.decodeCertificate(certificateString);
        PublicKey publicKey = KeysForTests.decodeRSAPublicKey(publicString);
        PrivateKey privateKey = KeysForTests.decodeRSAPrivateKey(privateString);

        assertThat(credential.getPrivateKey(), Matchers.equalTo(privateKey));
        assertThat(credential.getCertificate(), Matchers.equalTo(certificate));
        assertThat(credential.getCertificate().getPublicKey(), Matchers.equalTo(publicKey));
    }

}
