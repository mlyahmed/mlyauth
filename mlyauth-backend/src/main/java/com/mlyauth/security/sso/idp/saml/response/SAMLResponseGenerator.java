package com.mlyauth.security.sso.idp.saml.response;

import com.mlyauth.dao.ApplicationAspectAttributeDAO;
import com.mlyauth.domain.Application;
import com.mlyauth.domain.ApplicationAspectAttribute;
import com.mlyauth.exception.AuthError;
import com.mlyauth.exception.IDPException;
import com.mlyauth.security.context.IContext;
import com.mlyauth.security.sso.SAMLHelper;
import org.joda.time.DateTime;
import org.opensaml.saml2.core.*;
import org.opensaml.saml2.encryption.Encrypter;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.encryption.EncryptionConstants;
import org.opensaml.xml.encryption.EncryptionException;
import org.opensaml.xml.encryption.EncryptionParameters;
import org.opensaml.xml.encryption.KeyEncryptionParameters;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureConstants;
import org.opensaml.xml.signature.SignatureException;
import org.opensaml.xml.signature.Signer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.saml.key.KeyManager;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

import static com.mlyauth.constants.AuthAspectType.SP_SAML;
import static com.mlyauth.constants.SPSAMLAuthAttributes.*;

@Component
public class SAMLResponseGenerator {

    @Autowired
    private IContext context;

    @Autowired
    private KeyManager keyManager;

    @Autowired
    private SAMLHelper samlHelper;

    @Autowired
    private ApplicationAspectAttributeDAO appAspectAttrDAO;

    @Value("${idp.saml.entityId}")
    private String idpEntityId;

    public Response generate(Application app) {
        Assert.notNull(app, "The application parameter is null");

        try {
            final List<ApplicationAspectAttribute> attributes = loadAttributes(app);
            return buildResponse(attributes);
        } catch (Exception e) {
            throw IDPException.newInstance().setErrors(Arrays.asList(AuthError.newInstance("SAML_RESPONSE_ERR")));
        }

    }

    private Response buildResponse(List<ApplicationAspectAttribute> attributes) throws Exception {
        final Response response = newResponse();
        setStatus(response);
        setTarget(response, attributes);
        response.getEncryptedAssertions().add(encryptAssertion(attributes, buildAssertion(attributes)));
        signResponse(response);
        return response;
    }

    private Response newResponse() {
        final Response response = samlHelper.buildSAMLObject(Response.class);
        response.setID(samlHelper.generateRandomId());
        response.setIssueInstant(DateTime.now().minusSeconds(30));
        Issuer issuer = samlHelper.buildSAMLObject(Issuer.class);
        issuer.setValue(idpEntityId);
        response.setIssuer(issuer);
        return response;
    }

    private void setTarget(Response response, List<ApplicationAspectAttribute> attributes) {
        response.setDestination(getTargetURL(attributes).getValue());
    }

    private void setStatus(Response response) {
        Status status = samlHelper.buildSAMLObject(Status.class);
        StatusCode statusCode = samlHelper.buildSAMLObject(StatusCode.class);
        statusCode.setValue(StatusCode.SUCCESS_URI);
        status.setStatusCode(statusCode);
        response.setStatus(status);
    }

    private void signResponse(Response response) throws MarshallingException, SignatureException {
        Signature signature = samlHelper.buildSAMLObject(Signature.class);
        signature.setSigningCredential(keyManager.getDefaultCredential());
        signature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1);
        signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        response.setSignature(signature);
        Configuration.getMarshallerFactory().getMarshaller(response).marshall(response);
        Signer.signObject(signature);
    }


    private Assertion buildAssertion(List<ApplicationAspectAttribute> attributes) {
        final Assertion assertion = newAssertion();
        setSubject(assertion, attributes);
        setAuthnStatement(assertion);
        setConditions(attributes, assertion);
        setAssertionAttributes(assertion);
        return assertion;
    }

    private Assertion newAssertion() {
        final Assertion assertion = samlHelper.buildSAMLObject(Assertion.class);
        assertion.setID(samlHelper.generateRandomId());
        Issuer issuer = samlHelper.buildSAMLObject(Issuer.class);
        issuer.setValue(idpEntityId);
        assertion.setIssuer(issuer);
        assertion.setIssueInstant(DateTime.now().minusSeconds(30));
        return assertion;
    }

    private void setSubject(Assertion assertion, List<ApplicationAspectAttribute> attributes) {
        final Subject subject = samlHelper.buildSAMLObject(Subject.class);
        NameID nameID = samlHelper.buildSAMLObject(NameID.class);
        nameID.setFormat(NameIDType.TRANSIENT);
        subject.setNameID(nameID);
        setSubjectConfirmation(subject, attributes);
        assertion.setSubject(subject);
    }

    private void setSubjectConfirmation(Subject subject, List<ApplicationAspectAttribute> attributes) {
        final SubjectConfirmation subjectConfirmation = samlHelper.buildSAMLObject(SubjectConfirmation.class);
        final SubjectConfirmationData subjectConfirmationData = samlHelper.buildSAMLObject(SubjectConfirmationData.class);
        subjectConfirmation.setMethod(SubjectConfirmation.METHOD_BEARER);
        subjectConfirmationData.setInResponseTo(getEntityId(attributes).getValue());
        subjectConfirmationData.setRecipient(getTargetURL(attributes).getValue());
        subjectConfirmationData.setNotOnOrAfter((new DateTime()).plusMinutes(20));
        subjectConfirmation.setSubjectConfirmationData(subjectConfirmationData);
        subject.getSubjectConfirmations().add(subjectConfirmation);
    }

    private void setAuthnStatement(Assertion assertion) {
        final AuthnStatement authnStatement = samlHelper.buildSAMLObject(AuthnStatement.class);
        final AuthnContext authnContext = samlHelper.buildSAMLObject(AuthnContext.class);
        AuthnContextClassRef authnContextClassRef = samlHelper.buildSAMLObject(AuthnContextClassRef.class);
        authnContextClassRef.setAuthnContextClassRef(AuthnContext.PASSWORD_AUTHN_CTX);
        authnContext.setAuthnContextClassRef(authnContextClassRef);
        authnStatement.setAuthnInstant(DateTime.now().minusSeconds(30));
        authnStatement.setAuthnContext(authnContext);
        assertion.getAuthnStatements().add(authnStatement);
    }

    private void setConditions(List<ApplicationAspectAttribute> attributes, Assertion assertion) {
        final Conditions conditions = samlHelper.buildSAMLObject(Conditions.class);
        conditions.setNotOnOrAfter((new DateTime()).plusMinutes(20));
        AudienceRestriction audienceRestriction = samlHelper.buildSAMLObject(AudienceRestriction.class);
        final Audience audience = samlHelper.buildSAMLObject(Audience.class);
        audience.setAudienceURI(getEntityId(attributes).getValue());
        audienceRestriction.getAudiences().add(audience);
        conditions.getAudienceRestrictions().add(audienceRestriction);
        assertion.setConditions(conditions);
    }

    private void setAssertionAttributes(Assertion assertion) {
        AttributeStatement attributeStatement = samlHelper.buildSAMLObject(AttributeStatement.class);
        final List<Attribute> assertionAttributes = attributeStatement.getAttributes();
        context.getAttributes().forEach((k, v) -> assertionAttributes.add(samlHelper.buildStringAttribute(k, v)));
        assertion.getAttributeStatements().add(attributeStatement);
    }

    private EncryptedAssertion encryptAssertion(List<ApplicationAspectAttribute> attributes, Assertion assertion) throws CertificateException, EncryptionException {
        BasicX509Credential credential = new BasicX509Credential();
        credential.setEntityCertificate(loadApplicationEncryptionCertificate(attributes));
        EncryptionParameters encryptionParameters = new EncryptionParameters();
        encryptionParameters.setAlgorithm(EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES128);
        KeyEncryptionParameters keyEncryptionParameters = new KeyEncryptionParameters();
        keyEncryptionParameters.setEncryptionCredential(credential);
        keyEncryptionParameters.setAlgorithm(EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSAOAEP);
        Encrypter encrypter = new Encrypter(encryptionParameters, keyEncryptionParameters);
        encrypter.setKeyPlacement(Encrypter.KeyPlacement.INLINE);
        return encrypter.encrypt(assertion);
    }

    private X509Certificate loadApplicationEncryptionCertificate(List<ApplicationAspectAttribute> attributes) throws CertificateException {
        return samlHelper.toX509Certificate(getEncryptionCertificate(attributes).getValue());
    }

    private ApplicationAspectAttribute getEntityId(List<ApplicationAspectAttribute> attributes) {
        return attributes.stream()
                .filter(att -> SP_SAML_ENTITY_ID.equals(att.getId().getAttributeCode()))
                .findFirst().orElseGet(null);
    }

    private ApplicationAspectAttribute getEncryptionCertificate(List<ApplicationAspectAttribute> attributes) {
        return attributes.stream()
                .filter(att -> SP_SAML_ENCRYPTION_CERTIFICATE.equals(att.getId().getAttributeCode()))
                .findFirst().orElseGet(null);
    }

    private ApplicationAspectAttribute getTargetURL(List<ApplicationAspectAttribute> attributes) {
        return attributes.stream()
                .filter(att -> SP_SAML_SSO_URL.equals(att.getId().getAttributeCode()))
                .findFirst().orElseGet(null);
    }

    private List<ApplicationAspectAttribute> loadAttributes(Application app) {
        return appAspectAttrDAO.findByAppAndAspect(app.getId(), SP_SAML.name());
    }
}
