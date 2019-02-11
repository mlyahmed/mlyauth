package com.hohou.federation.idp.sample.idp.saml;

import com.hohou.federation.idp.sample.idp.SampleIDPToken;
import org.joda.time.DateTime;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.Audience;
import org.opensaml.saml2.core.AudienceRestriction;
import org.opensaml.saml2.core.AuthnContext;
import org.opensaml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.EncryptedAssertion;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.NameIDType;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Status;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.core.SubjectConfirmation;
import org.opensaml.saml2.core.SubjectConfirmationData;
import org.opensaml.saml2.encryption.Encrypter;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.encryption.EncryptionParameters;
import org.opensaml.xml.encryption.KeyEncryptionParameters;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.schema.impl.XSStringBuilder;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureConstants;
import org.opensaml.xml.signature.Signer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.saml.key.KeyManager;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;

import static java.util.Arrays.asList;
import static org.opensaml.saml2.core.AttributeValue.DEFAULT_ELEMENT_NAME;
import static org.opensaml.saml2.core.StatusCode.AUTHN_FAILED_URI;
import static org.opensaml.saml2.core.StatusCode.SUCCESS_URI;
import static org.opensaml.xml.encryption.EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES128;
import static org.opensaml.xml.encryption.EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSAOAEP;
import static org.opensaml.xml.signature.SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS;
import static org.opensaml.xml.util.Base64.encodeBytes;

@Component
public class IDPSamlService {


    @Autowired
    private KeyManager keyManager;

    public String generateSAMLAccess(SampleIDPToken token) {
        try {
            DefaultBootstrap.bootstrap();

            Assertion assertion = buildSAMLObject(Assertion.class);
            final AuthnStatement authnStatement = buildSAMLObject(AuthnStatement.class);
            final AuthnContext authnContext = buildSAMLObject(AuthnContext.class);
            AuthnContextClassRef authnContextClassRef = buildSAMLObject(AuthnContextClassRef.class);
            authnContextClassRef.setAuthnContextClassRef(AuthnContext.PASSWORD_AUTHN_CTX);
            authnContext.setAuthnContextClassRef(authnContextClassRef);
            authnStatement.setAuthnContext(authnContext);
            authnStatement.setAuthnInstant(DateTime.now());
            assertion.getAuthnStatements().add(authnStatement);
            assertion.setIssuer(buildSAMLObject(Issuer.class));
            assertion.getIssuer().setValue(token.getIssuer());
            assertion.setIssueInstant(DateTime.now());
            assertion.setID(token.getId());

            AttributeStatement attributeStatement = buildSAMLObject(AttributeStatement.class);
            final List<Attribute> assertionAttributes = attributeStatement.getAttributes();
            final Attribute state = buildAttribute("state", token.getState());
            final Attribute scopes = buildAttribute("scopes", token.getScopes());
            final Attribute bp = buildAttribute("bp", token.getBp());
            final Attribute delegator = buildAttribute("delegator", token.getDelegator());
            final Attribute delegate = buildAttribute("delegate", token.getDelegate());
            final Attribute idClient = buildAttribute("idClient", token.getClientId());
            final Attribute clientProfile = buildAttribute("profilUtilisateur", token.getClientProfile());
            final Attribute entityId = buildAttribute("idPrestation", token.getEntityId());
            final Attribute action = buildAttribute("action", token.getAction());
            final Attribute application = buildAttribute("application", token.getApplication());
            assertionAttributes.addAll(asList(state, scopes, bp, delegator, delegate, idClient, clientProfile, entityId, action, application));
            assertion.getAttributeStatements().add(attributeStatement);

            Subject subject = buildSAMLObject(Subject.class);
            NameID nameID = buildSAMLObject(NameID.class);
            nameID.setFormat(NameIDType.TRANSIENT);
            nameID.setValue(token.getSubject());
            subject.setNameID(nameID);
            final SubjectConfirmation subjectConfirmation = buildSAMLObject(SubjectConfirmation.class);
            final SubjectConfirmationData subjectConfirmationData = buildSAMLObject(SubjectConfirmationData.class);
            subjectConfirmationData.setInResponseTo(token.getAudience());
            subjectConfirmationData.setRecipient(token.getTargetURL());
            subjectConfirmationData.setNotOnOrAfter(DateTime.now().plusMinutes(2));
            subjectConfirmation.setMethod(SubjectConfirmation.METHOD_BEARER);
            subjectConfirmation.setSubjectConfirmationData(subjectConfirmationData);
            subject.getSubjectConfirmations().add(subjectConfirmation);
            assertion.setSubject(subject);

            Audience audience = buildSAMLObject(Audience.class);
            audience.setAudienceURI(token.getAudience());
            final Conditions conditions = buildSAMLObject(Conditions.class);
            AudienceRestriction audienceRestriction = buildSAMLObject(AudienceRestriction.class);
            audienceRestriction.getAudiences().add(audience);
            conditions.getAudienceRestrictions().add(audienceRestriction);
            conditions.setNotOnOrAfter(DateTime.now().plusMinutes(2));
            assertion.setConditions(conditions);

            Response response = buildSAMLObject(Response.class);
            response.setIssuer(buildSAMLObject(Issuer.class));
            response.getIssuer().setValue(token.getIssuer());
            Status responseStatus = buildSAMLObject(Status.class);
            responseStatus.setStatusCode(buildSAMLObject(StatusCode.class));
            responseStatus.getStatusCode().setValue("SUCCESS".equals(token.getVerdict()) ? SUCCESS_URI : AUTHN_FAILED_URI);
            response.setStatus(responseStatus);
            response.setIssueInstant(DateTime.now());
            response.setID(token.getId());
            response.setDestination(token.getTargetURL());


            final Credential credential = toCredential(keyManager.getDefaultCredential().getPrivateKey(), keyManager.getCertificate("sgi.prima-solutions.com"));
            response.getEncryptedAssertions().add(encryptAssertion(assertion, credential));
            signResponse(response, credential);


            return serializeResponse(response);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private <C> C buildSAMLObject(final Class<C> clazz) {
        try {
            XMLObjectBuilderFactory builderFactory = Configuration.getBuilderFactory();
            QName defaultElementName = (QName) clazz.getDeclaredField("DEFAULT_ELEMENT_NAME").get(null);
            return (C) builderFactory.getBuilder(defaultElementName).buildObject(defaultElementName);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new IllegalArgumentException("Could not create SAML object");
        }
    }

    private Attribute buildAttribute(String attName, String attValue) {
        Attribute attribute = buildSAMLObject(Attribute.class);
        attribute.setName(attName);
        XSStringBuilder stringBuilder = (XSStringBuilder) org.opensaml.xml.Configuration.getBuilderFactory().getBuilder(XSString.TYPE_NAME);
        XSString value = stringBuilder.buildObject(DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
        value.setValue(attValue);
        attribute.getAttributeValues().add(value);
        return attribute;
    }

    private EncryptedAssertion encryptAssertion(Assertion assertion, Credential credential) {
        try {
            EncryptionParameters encryptionParameters = new EncryptionParameters();
            encryptionParameters.setAlgorithm(ALGO_ID_BLOCKCIPHER_AES128);
            KeyEncryptionParameters keyEncryptionParameters = new KeyEncryptionParameters();
            keyEncryptionParameters.setEncryptionCredential(credential);
            keyEncryptionParameters.setAlgorithm(ALGO_ID_KEYTRANSPORT_RSAOAEP);
            Encrypter encrypter = new Encrypter(encryptionParameters, keyEncryptionParameters);
            encrypter.setKeyPlacement(Encrypter.KeyPlacement.INLINE);
            return encrypter.encrypt(assertion);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void signResponse(Response response, Credential credential) {
        try {
            Signature signature = this.buildSAMLObject(Signature.class);
            signature.setSigningCredential(credential);
            signature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1);
            signature.setCanonicalizationAlgorithm(ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
            response.setSignature(signature);
            org.opensaml.xml.Configuration.getMarshallerFactory().getMarshaller(response).marshall(response);
            Signer.signObject(signature);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Credential toCredential(PrivateKey privateKey, X509Certificate certificate) {
        try {
            BasicX509Credential credential = new BasicX509Credential();
            credential.setEntityCertificate(certificate);
            credential.setPrivateKey(privateKey);
            return credential;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String serializeResponse(Response response) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            Marshaller out = org.opensaml.xml.Configuration.getMarshallerFactory().getMarshaller(response);
            out.marshall(response, document);
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            StringWriter stringWriter = new StringWriter();
            StreamResult streamResult = new StreamResult(stringWriter);
            DOMSource source = new DOMSource(document);
            transformer.transform(source, streamResult);
            stringWriter.close();
            return encodeBytes(stringWriter.toString().getBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
