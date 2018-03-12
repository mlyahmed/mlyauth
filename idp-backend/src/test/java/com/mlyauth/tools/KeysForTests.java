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

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Map;

public class KeysForTests {

    private static final long validity = 1000L * 60 * 60 * 24 * 30; // 30 days
    private static final String commonName = "www.primaIDP.com";
    private static final String organizationalUnit = "SGIProject";
    private static final String organization = "Prima-Solutions";
    private static final String city = "Paris";
    private static final String email = "sgi@prima-solutions.com";


    public static Pair<PrivateKey, X509Certificate> generateRSACredential() {
        return generatePairCredential("RSA", 1024, "SHA1WithRSA");
    }

    private static Pair<PrivateKey, X509Certificate> generatePairCredential(String algorithm, int keysize, String signatureAlgorithm) {
        try {

            final BouncyCastleProvider provider = new BouncyCastleProvider();
            Security.addProvider(provider);
            KeyPair KPair = generateKeyPair(algorithm, keysize);

            final Date notBefore = new Date(System.currentTimeMillis() - validity);
            final Date notAfter = new Date(System.currentTimeMillis() + validity);

            X509v3CertificateBuilder certificateBuilder = new JcaX509v3CertificateBuilder(buildIssuer(),
                    BigInteger.valueOf(new SecureRandom().nextInt()),
                    notBefore, notAfter, buildSubject(), KPair.getPublic());

            JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();
            certificateBuilder.addExtension(Extension.subjectKeyIdentifier, false, extUtils.createSubjectKeyIdentifier(KPair.getPublic()));
            certificateBuilder.addExtension(Extension.authorityKeyIdentifier, false, extUtils.createAuthorityKeyIdentifier(KPair.getPublic()));
            X509CertificateHolder certHldr = certificateBuilder.build(new JcaContentSignerBuilder(signatureAlgorithm).setProvider(provider.getName()).build(KPair.getPrivate()));
            X509Certificate certificate = new JcaX509CertificateConverter().setProvider(provider.getName()).getCertificate(certHldr);
            return new Pair<>(KPair.getPrivate(), certificate);

        } catch (Exception e) {
            throw EncryptionCredentialException.newInstance(e);
        }
    }

    private static KeyPair generateKeyPair(String algorithm, int keysize) throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(algorithm);
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

    public static SecretKey generateAES256SecretKey() {
        try {
            removeCryptographyRestrictions();
            return generateSecretKey("AES", 256);
        } catch (Exception e) {
            throw EncryptionCredentialException.newInstance(e);
        }
    }

    private static SecretKey generateSecretKey(String algorithm, int keysize) throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(algorithm);
        keyGen.init(keysize);
        return keyGen.generateKey();
    }

    private static void removeCryptographyRestrictions() throws Exception {
        if (!isRestrictedCryptography()) {
            return;
        }

        final Class<?> jceSecurity = Class.forName("javax.crypto.JceSecurity");
        final Class<?> cryptoPermissions = Class.forName("javax.crypto.CryptoPermissions");
        final Class<?> cryptoAllPermission = Class.forName("javax.crypto.CryptoAllPermission");

        final Field isRestrictedField = jceSecurity.getDeclaredField("isRestricted");
        isRestrictedField.setAccessible(true);
        final Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(isRestrictedField, isRestrictedField.getModifiers() & ~Modifier.FINAL);
        isRestrictedField.set(null, false);

        final Field defaultPolicyField = jceSecurity.getDeclaredField("defaultPolicy");
        defaultPolicyField.setAccessible(true);
        final PermissionCollection defaultPolicy = (PermissionCollection) defaultPolicyField.get(null);

        final Field perms = cryptoPermissions.getDeclaredField("perms");
        perms.setAccessible(true);
        ((Map<?, ?>) perms.get(defaultPolicy)).clear();

        final Field instance = cryptoAllPermission.getDeclaredField("INSTANCE");
        instance.setAccessible(true);
        defaultPolicy.add((Permission) instance.get(null));

    }

    private static boolean isRestrictedCryptography() {
        final String name = System.getProperty("java.runtime.name");
        final String ver = System.getProperty("java.version");
        return name != null && name.equals("Java(TM) SE Runtime Environment")
                && ver != null && (ver.startsWith("1.7") || ver.startsWith("1.8"));
    }

}
