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
import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;
import java.util.Map;

import static java.util.Base64.getDecoder;
import static java.util.Base64.getEncoder;

public final class KeysForTests {

    private static final long VALIDITY = 1000L * 60 * 60 * 24 * 30; // 30 days
    private static final String COMMON_NAME = "www.primaIDP.com";
    private static final String ORGANIZATIONAL_UNIT = "SGIProject";
    private static final String ORGANIZATION = "Prima-Solutions";
    private static final String CITY = "Paris";
    private static final String EMAIL = "sgi@prima-solutions.com";
    private static final int KEY_1024_SIZE = 1024;
    private static final int KEY_256_SIZE = 256;

    private KeysForTests() {

    }

    public static Pair<PrivateKey, X509Certificate> generateRSACredential() {
        return generatePairCredential("RSA", KEY_1024_SIZE, "SHA1WithRSA");
    }

    public static Pair<PrivateKey, X509Certificate> generatePairCredential(final String algorithm, final int keysize,
                                                                           final String signatureAlgorithm) {
        try {

            final BouncyCastleProvider provider = new BouncyCastleProvider();
            Security.addProvider(provider);
            final KeyPair pair = generateKeyPair(algorithm, keysize);

            final Date notBefore = new Date(System.currentTimeMillis() - VALIDITY);
            final Date notAfter = new Date(System.currentTimeMillis() + VALIDITY);

            X509v3CertificateBuilder certificateBuilder = new JcaX509v3CertificateBuilder(buildIssuer(),
                    BigInteger.valueOf(new SecureRandom().nextInt()),
                    notBefore, notAfter, buildSubject(), pair.getPublic());

            JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();
            certificateBuilder.addExtension(Extension.subjectKeyIdentifier, false,
                    extUtils.createSubjectKeyIdentifier(pair.getPublic()));
            certificateBuilder.addExtension(Extension.authorityKeyIdentifier, false,
                    extUtils.createAuthorityKeyIdentifier(pair.getPublic()));
            X509CertificateHolder certHldr = certificateBuilder.build(new JcaContentSignerBuilder(signatureAlgorithm)
                    .setProvider(provider.getName()).build(pair.getPrivate()));
            X509Certificate certificate = new JcaX509CertificateConverter().setProvider(provider.getName())
                    .getCertificate(certHldr);
            return new Pair<>(pair.getPrivate(), certificate);

        } catch (Exception e) {
            throw EncryptionCredentialException.newInstance(e);
        }
    }

    public static String encodeCertificate(final Certificate certificate) {
        try {
            return getEncoder().encodeToString(certificate.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Certificate decodeCertificate(final String encodedCertificate) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(getDecoder().decode(encodedCertificate));
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            return certFactory.generateCertificate(inputStream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String encodePublicKey(final PublicKey publicKey) {
        try {
            return getEncoder().encodeToString(publicKey.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static PublicKey decodeRSAPublicKey(final String encodedPublicKey) {
        try {
            X509EncodedKeySpec encodedKeySpec = new X509EncodedKeySpec(getDecoder().decode(encodedPublicKey));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(encodedKeySpec);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String encodePrivateKey(final PrivateKey privateKey) {
        try {
            return getEncoder().encodeToString(privateKey.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static PrivateKey decodeRSAPrivateKey(final String encodedPrivateKey) {
        try {
            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(getDecoder().decode(encodedPrivateKey));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(pkcs8EncodedKeySpec);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static KeyPair generateKeyPair(final String algorithm, final int keysize) throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(algorithm);
        keyPairGenerator.initialize(keysize);
        return keyPairGenerator.generateKeyPair();
    }

    private static X500Name buildSubject() {
        X500NameBuilder subjectBuilder = new X500NameBuilder(BCStyle.INSTANCE);
        subjectBuilder.addRDN(BCStyle.C, COMMON_NAME);
        subjectBuilder.addRDN(BCStyle.O, ORGANIZATION);
        subjectBuilder.addRDN(BCStyle.L, CITY);
        subjectBuilder.addRDN(BCStyle.CN, KeysForTests.COMMON_NAME);
        subjectBuilder.addRDN(BCStyle.EmailAddress, EMAIL);
        return subjectBuilder.build();
    }

    private static X500Name buildIssuer() {
        X500NameBuilder issuerBuilder = new X500NameBuilder(BCStyle.INSTANCE);
        issuerBuilder.addRDN(BCStyle.C, COMMON_NAME);
        issuerBuilder.addRDN(BCStyle.O, ORGANIZATION);
        issuerBuilder.addRDN(BCStyle.OU, ORGANIZATIONAL_UNIT);
        issuerBuilder.addRDN(BCStyle.EmailAddress, EMAIL);
        return issuerBuilder.build();
    }

    public static SecretKey generateAES256SecretKey() {
        try {
            removeCryptographyRestrictions();
            return generateSecretKey("AES", KEY_256_SIZE);
        } catch (Exception e) {
            throw EncryptionCredentialException.newInstance(e);
        }
    }

    private static SecretKey generateSecretKey(final String algorithm, final int keysize) throws Exception {
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
