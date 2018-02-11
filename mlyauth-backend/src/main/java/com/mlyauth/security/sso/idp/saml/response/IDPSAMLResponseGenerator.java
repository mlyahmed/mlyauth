package com.mlyauth.security.sso.idp.saml.response;

import com.mlyauth.dao.ApplicationAspectAttributeDAO;
import com.mlyauth.domain.Application;
import com.mlyauth.domain.ApplicationAspectAttribute;
import com.mlyauth.security.sso.SAMLHelper;
import org.joda.time.DateTime;
import org.opensaml.saml2.core.*;
import org.opensaml.saml2.encryption.Encrypter;
import org.opensaml.xml.encryption.EncryptionConstants;
import org.opensaml.xml.encryption.EncryptionException;
import org.opensaml.xml.encryption.EncryptionParameters;
import org.opensaml.xml.encryption.KeyEncryptionParameters;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import static com.mlyauth.constants.AuthAspectType.SP_SAML;
import static com.mlyauth.constants.SPSAMLAuthAttributes.SP_SAML_ENCRYPTION_CERTIFICATE;
import static com.mlyauth.constants.SPSAMLAuthAttributes.SP_SAML_SSO_URL;

@Component
public class IDPSAMLResponseGenerator {

    @Autowired
    private SAMLHelper samlHelper;

    @Autowired
    private ApplicationAspectAttributeDAO appAspectAttrDAO;

    @Value("${idp.saml.entityId}")
    private String idpEntityId;

    public Response generate(Application app) throws EncryptionException, CertificateException {
        Assert.notNull(app, "The application parameter is null");
        final List<ApplicationAspectAttribute> attributes = loadAttributes(app);
        final Response response = newResponse();
        setTarget(response, attributes);
        setIssuer(response);
        setStatus(response);
        setIssuanceTime(response);
        response.getEncryptedAssertions().add(encryptAssertion(attributes, buildAssertion()));

        return response;
    }

    private Response newResponse() {
        final Response response = samlHelper.buildSAMLObject(Response.class);
        response.setID(samlHelper.generateRandomId());
        return response;
    }

    private void setTarget(Response response, List<ApplicationAspectAttribute> attributes) {
        final ApplicationAspectAttribute ssoURL = attributes.stream()
                .filter(att -> SP_SAML_SSO_URL.equals(att.getId().getAttributeCode()))
                .findFirst().orElseGet(null);
        response.setDestination(ssoURL.getValue());
    }

    private void setIssuer(Response response) {
        Issuer issuer = samlHelper.buildSAMLObject(Issuer.class);
        issuer.setValue(idpEntityId);
        response.setIssuer(issuer);
    }

    private void setStatus(Response response) {
        Status status = samlHelper.buildSAMLObject(Status.class);
        StatusCode statusCode = samlHelper.buildSAMLObject(StatusCode.class);
        statusCode.setValue(StatusCode.SUCCESS_URI);
        status.setStatusCode(statusCode);
        response.setStatus(status);
    }

    private void setIssuanceTime(Response response) {
        response.setIssueInstant(DateTime.now().minusMinutes(1));
    }


    private Assertion buildAssertion() {
        return samlHelper.buildSAMLObject(Assertion.class);
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
        final ApplicationAspectAttribute encryptionCertificate = attributes.stream()
                .filter(att -> SP_SAML_ENCRYPTION_CERTIFICATE.equals(att.getId().getAttributeCode()))
                .findFirst().orElseGet(null);
        return samlHelper.toX509Certificate(encryptionCertificate.getValue());
    }

    private List<ApplicationAspectAttribute> loadAttributes(Application app) {
        return appAspectAttrDAO.findByAppAndAspect(app.getId(), SP_SAML.name());
    }
}
