package com.mlyauth.tools;

import com.mlyauth.exception.EncryptionCredentialException;
import javafx.util.Pair;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.math.BigInteger;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.Date;

public class KeysForTests {

    private static final long validity = 1000L * 60 * 60 * 24 * 30; // 30 days
    private static final int keysize = 1024;
    private static final String commonName = "www.primaIDP.com";
    private static final String organizationalUnit = "SGIProject";
    private static final String organization = "Prima-Solutions";
    private static final String city = "Paris";
    private static final String email = "sgi@prima-solutions.com";


    public static Pair<PrivateKey, X509Certificate> generateCredential() {
        try {

            final BouncyCastleProvider provider = new BouncyCastleProvider();
            Security.addProvider(provider);
            KeyPair KPair = generateKeyPair();

            final Date notBefore = new Date(System.currentTimeMillis() - validity);
            final Date notAfter = new Date(System.currentTimeMillis() + validity);

            X509v3CertificateBuilder certificateBuilder = new JcaX509v3CertificateBuilder(buildIssuer(),
                    BigInteger.valueOf(new SecureRandom().nextInt()),
                    notBefore, notAfter, buildSubject(), KPair.getPublic());

            JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();
            certificateBuilder.addExtension(Extension.subjectKeyIdentifier, false, extUtils.createSubjectKeyIdentifier(KPair.getPublic()));
            certificateBuilder.addExtension(Extension.authorityKeyIdentifier, false, extUtils.createAuthorityKeyIdentifier(KPair.getPublic()));
            X509CertificateHolder certHldr = certificateBuilder.build(new JcaContentSignerBuilder("SHA1WithRSA").setProvider(provider.getName()).build(KPair.getPrivate()));
            X509Certificate certificate = new JcaX509CertificateConverter().setProvider(provider.getName()).getCertificate(certHldr);
            return new Pair<>(KPair.getPrivate(), certificate);

        } catch (Exception e) {
            throw EncryptionCredentialException.newInstance(e);
        }
    }

    private static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(keysize);
        return keyPairGenerator.generateKeyPair();
    }

    private static X500Name buildSubject() {
        X500NameBuilder subjectBuilder = new X500NameBuilder(BCStyle.INSTANCE);
        subjectBuilder.addRDN(BCStyle.C, commonName);
        subjectBuilder.addRDN(BCStyle.O, organization);
        subjectBuilder.addRDN(BCStyle.L, city);
        subjectBuilder.addRDN(BCStyle.CN, KeysForTests.commonName);
        subjectBuilder.addRDN(BCStyle.EmailAddress, email);
        return subjectBuilder.build();
    }

    private static X500Name buildIssuer() {
        X500NameBuilder issuerBuilder = new X500NameBuilder(BCStyle.INSTANCE);
        issuerBuilder.addRDN(BCStyle.C, commonName);
        issuerBuilder.addRDN(BCStyle.O, organization);
        issuerBuilder.addRDN(BCStyle.OU, organizationalUnit);
        issuerBuilder.addRDN(BCStyle.EmailAddress, email);
        return issuerBuilder.build();
    }

}
