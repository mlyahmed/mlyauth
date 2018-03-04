package com.mlyauth.tools;

import com.mlyauth.exception.IDPSAMLErrorException;
import org.opensaml.Configuration;
import org.opensaml.common.SignableSAMLObject;
import org.opensaml.common.impl.SecureRandomIdentifierGenerator;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeValue;
import org.opensaml.saml2.core.EncryptedAssertion;
import org.opensaml.saml2.encryption.Decrypter;
import org.opensaml.saml2.encryption.Encrypter;
import org.opensaml.saml2.metadata.impl.EntityDescriptorImpl;
import org.opensaml.security.SAMLSignatureProfileValidator;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.encryption.EncryptionConstants;
import org.opensaml.xml.encryption.EncryptionParameters;
import org.opensaml.xml.encryption.InlineEncryptedKeyResolver;
import org.opensaml.xml.encryption.KeyEncryptionParameters;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.parse.BasicParserPool;
import org.opensaml.xml.parse.ParserPool;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.schema.impl.XSStringBuilder;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.keyinfo.StaticKeyInfoCredentialResolver;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureConstants;
import org.opensaml.xml.signature.SignatureValidator;
import org.opensaml.xml.signature.Signer;
import org.opensaml.xml.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

@Component
public class SAMLHelper {
    private static Logger logger = LoggerFactory.getLogger(SAMLHelper.class);


    @Autowired
    private ParserPool parserPool = new BasicParserPool();

    private static SecureRandomIdentifierGenerator randomIdGenerator;


    static {
        try {
            randomIdGenerator = new SecureRandomIdentifierGenerator();
        } catch (NoSuchAlgorithmException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public String generateRandomId() {
        return randomIdGenerator.generateIdentifier();
    }

    @SuppressWarnings("unchecked")
    public <T> T buildSAMLObject(final Class<T> clazz) {
        try {

            XMLObjectBuilderFactory builderFactory = Configuration.getBuilderFactory();
            QName defaultElementName = (QName) clazz.getDeclaredField("DEFAULT_ELEMENT_NAME").get(null);
            return (T) builderFactory.getBuilder(defaultElementName).buildObject(defaultElementName);

        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new IllegalArgumentException("Could not create SAML object");
        }
    }

    public EntityDescriptorImpl toMetadata(String content) throws Exception {
        final Document doc = parserPool.parse(new ByteArrayInputStream(content.getBytes()));
        UnmarshallerFactory unmarshallerFactory = org.opensaml.xml.Configuration.getUnmarshallerFactory();
        Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(doc.getDocumentElement());
        XMLObject xmlObject = unmarshaller.unmarshall(doc.getDocumentElement());
        return (EntityDescriptorImpl) xmlObject;
    }

    public Attribute buildStringAttribute(String attName, String attValue) {
        Attribute attribute = buildSAMLObject(Attribute.class);
        attribute.setName(attName);
        XSStringBuilder stringBuilder = (XSStringBuilder) org.opensaml.xml.Configuration.getBuilderFactory().getBuilder(XSString.TYPE_NAME);
        XSString value = stringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
        value.setValue(attValue);
        attribute.getAttributeValues().add(value);
        return attribute;
    }


    public X509Certificate toX509Certificate(String encodedCertificate) {
        try {
            final byte[] decode = org.opensaml.xml.util.Base64.decode(encodedCertificate);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(decode);
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            return (X509Certificate) certFactory.generateCertificate(inputStream);
        } catch (Exception e) {
            throw IDPSAMLErrorException.newInstance(e);
        }
    }

    public BasicX509Credential toBasicX509Credential(String encodedCertificate) {
        BasicX509Credential credential = new BasicX509Credential();
        credential.setEntityCertificate(toX509Certificate(encodedCertificate));
        return credential;
    }


    public Assertion decryptAssertion(EncryptedAssertion encryptedAssertion, Credential credential) {
        try {
            StaticKeyInfoCredentialResolver keyInfoCredentialResolver = new StaticKeyInfoCredentialResolver(credential);
            Decrypter decrypter = new Decrypter(null, keyInfoCredentialResolver, new InlineEncryptedKeyResolver());
            decrypter.setRootInNewDocument(true);
            return decrypter.decrypt(encryptedAssertion);
        } catch (Exception e) {
            throw IDPSAMLErrorException.newInstance(e);
        }
    }

    public EncryptedAssertion encryptAssertion(Assertion assertion, Credential credential) {
        try {
            EncryptionParameters encryptionParameters = new EncryptionParameters();
            encryptionParameters.setAlgorithm(EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES128);
            KeyEncryptionParameters keyEncryptionParameters = new KeyEncryptionParameters();
            keyEncryptionParameters.setEncryptionCredential(credential);
            keyEncryptionParameters.setAlgorithm(EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSAOAEP);
            Encrypter encrypter = new Encrypter(encryptionParameters, keyEncryptionParameters);
            encrypter.setKeyPlacement(Encrypter.KeyPlacement.INLINE);
            return encrypter.encrypt(assertion);
        } catch (Exception e) {
            throw IDPSAMLErrorException.newInstance(e);
        }
    }

    public void signObject(SignableSAMLObject object, Credential credential) {
        try {
            Signature signature = this.buildSAMLObject(Signature.class);
            signature.setSigningCredential(credential);
            signature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1);
            signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
            object.setSignature(signature);
            org.opensaml.xml.Configuration.getMarshallerFactory().getMarshaller(object).marshall(object);
            Signer.signObject(signature);
        } catch (Exception e) {
            throw IDPSAMLErrorException.newInstance(e);
        }
    }

    public void validateSignature(SignableSAMLObject object, Credential credential) {
        try {
            SAMLSignatureProfileValidator signatureProfileValidator = new SAMLSignatureProfileValidator();
            signatureProfileValidator.validate(object.getSignature());
            SignatureValidator sigValidator = new SignatureValidator(credential);
            sigValidator.validate(object.getSignature());
        } catch (Exception e) {
            throw IDPSAMLErrorException.newInstance(e);
        }
    }

    public Credential toCredential(PrivateKey privateKey, X509Certificate certificate) {
        try {
            BasicX509Credential credential = new BasicX509Credential();
            credential.setEntityCertificate(certificate);
            credential.setPrivateKey(privateKey);
            return credential;
        } catch (Exception e) {
            throw IDPSAMLErrorException.newInstance(e);
        }
    }

    public XMLObject decode(String encodedObject) {
        try {
            final byte[] decoded = Base64.decode(encodedObject);
            Document messageDoc = parserPool.parse(new ByteArrayInputStream(decoded));
            Element messageElem = messageDoc.getDocumentElement();
            Unmarshaller unmarshaller = org.opensaml.xml.Configuration.getUnmarshallerFactory().getUnmarshaller(messageElem);
            return unmarshaller.unmarshall(messageElem);
        } catch (Exception e) {
            throw IDPSAMLErrorException.newInstance(e);
        }
    }

    public String toString(XMLObject xmlObject) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            Marshaller out = org.opensaml.xml.Configuration.getMarshallerFactory().getMarshaller(xmlObject);
            out.marshall(xmlObject, document);
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            StringWriter stringWriter = new StringWriter();
            StreamResult streamResult = new StreamResult(stringWriter);
            DOMSource source = new DOMSource(document);
            transformer.transform(source, streamResult);
            stringWriter.close();
            return stringWriter.toString();
        } catch (Exception e) {
            throw IDPSAMLErrorException.newInstance(e);
        }
    }
}