package com.mlyauth.utests.validators;

import com.mlyauth.constants.AuthAspectType;
import com.mlyauth.constants.SPSAMLAuthAttributes;
import com.mlyauth.dao.ApplicationAspectAttributeDAO;
import com.mlyauth.domain.Application;
import com.mlyauth.domain.ApplicationAspectAttribute;
import com.mlyauth.domain.ApplicationAspectAttributeId;
import com.mlyauth.exception.BadSPSAMLAspectAttributeValueException;
import com.mlyauth.exception.MissingSPSAMLAspectAttributeException;
import com.mlyauth.exception.NotSPSAMLApplicationException;
import com.mlyauth.security.sso.SAMLHelper;
import com.mlyauth.tools.KeysForTests;
import com.mlyauth.validators.SPSAMLAspectValidator;
import javafx.util.Pair;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.opensaml.xml.util.Base64;

import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import static com.mlyauth.constants.AuthAspectType.SP_SAML;
import static org.mockito.Mockito.when;

public class SPSAMLAspectValidatorTest {

    public static final long APPLICATION_ID = 2125485l;

    @InjectMocks
    SPSAMLAspectValidator validator;

    @Mock
    private ApplicationAspectAttributeDAO appAspectAttrDAO;

    @Spy
    private SAMLHelper samlHelper = new SAMLHelper();

    private Application application;
    private List<ApplicationAspectAttribute> attributes;
    private ApplicationAspectAttribute certificateAttribute;
    private ApplicationAspectAttribute ssoUrlAttribute;
    private ApplicationAspectAttribute entityIdAttribute;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        attributes = new LinkedList<>();
        application = Application.newInstance().setId(APPLICATION_ID).setAspects(new HashSet<>(Arrays.asList(SP_SAML)));
        when(appAspectAttrDAO.findByAppAndAspect(APPLICATION_ID, SP_SAML.name())).thenReturn(attributes);
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_application_is_null_then_error() {
        validator.validate(null);
    }

    @Test(expected = NotSPSAMLApplicationException.class)
    public void when_the_application_has_not_the_SP_SAML_aspect_then_error() {
        validator.validate(Application.newInstance());
    }


    @Test
    public void when_the_application_is_a_well_defined_SP_SAML_application_then_ok() throws Exception {
        given_the_application_holds_all_the_required_attributes();
        validator.validate(application);
    }

    @Test(expected = MissingSPSAMLAspectAttributeException.class)
    public void when_the_entity_id_is_absent_then_error() throws Exception {
        given_the_application_holds_encryption_certificate();
        given_the_application_holds_sso_url();
        validator.validate(application);
    }

    @Test(expected = BadSPSAMLAspectAttributeValueException.class)
    public void when_the_entity_id_attribute_value_is_null_then_error() throws Exception {
        given_the_application_holds_all_the_required_attributes();
        entityIdAttribute.setValue(null);
        validator.validate(application);
    }

    @Test(expected = BadSPSAMLAspectAttributeValueException.class)
    public void when_the_entity_id_attribute_value_is_blank_then_error() throws Exception {
        given_the_application_holds_all_the_required_attributes();
        entityIdAttribute.setValue("");
        validator.validate(application);
    }


    @Test(expected = MissingSPSAMLAspectAttributeException.class)
    public void when_the_sso_url_is_absent_then_error() throws Exception {
        given_the_application_holds_encryption_certificate();
        given_the_application_holds_the_entity_id();
        validator.validate(application);
    }

    @Test(expected = BadSPSAMLAspectAttributeValueException.class)
    public void when_the_sso_url_attribute_value_is_null_then_error() throws Exception {
        given_the_application_holds_all_the_required_attributes();
        ssoUrlAttribute.setValue(null);
        validator.validate(application);
    }

    @Test(expected = BadSPSAMLAspectAttributeValueException.class)
    public void when_the_sso_url_attribute_value_is_blank_then_error() throws Exception {
        given_the_application_holds_all_the_required_attributes();
        ssoUrlAttribute.setValue("");
        validator.validate(application);
    }

    @Test(expected = BadSPSAMLAspectAttributeValueException.class)
    public void when_the_sso_url_attribute_value_is_not_valid_url_then_error() throws Exception {
        given_the_application_holds_all_the_required_attributes();
        ssoUrlAttribute.setValue("badurl");
        validator.validate(application);
    }

    @Test(expected = BadSPSAMLAspectAttributeValueException.class)
    public void when_the_encryption_certificate_attribute_value_is_null_then_error() throws Exception {
        given_the_application_holds_all_the_required_attributes();
        certificateAttribute.setValue(null);
        validator.validate(application);
    }

    @Test(expected = BadSPSAMLAspectAttributeValueException.class)
    public void when_the_encryption_certificate_attribute_value_is_blank_then_error() throws Exception {
        given_the_application_holds_all_the_required_attributes();
        certificateAttribute.setValue("");
        validator.validate(application);
    }

    @Test(expected = BadSPSAMLAspectAttributeValueException.class)
    public void when_the_encryption_certificate_attribute_value_is_bad_then_error() throws Exception {
        given_the_application_holds_all_the_required_attributes();
        certificateAttribute.setValue("dsdsdsdsds");
        validator.validate(application);
    }

    private void given_the_application_holds_all_the_required_attributes() throws CertificateEncodingException {
        given_the_application_holds_encryption_certificate();
        given_the_application_holds_sso_url();
        given_the_application_holds_the_entity_id();
    }

    private void given_the_application_holds_encryption_certificate() throws CertificateEncodingException {
        final ApplicationAspectAttributeId encryptionCertificateId = ApplicationAspectAttributeId.newInstance()
                .setApplicationId(APPLICATION_ID)
                .setAspectCode(AuthAspectType.SP_SAML.name())
                .setAttributeCode(SPSAMLAuthAttributes.SP_SAML_ENCRYPTION_CERTIFICATE.getValue());
        final Pair<PrivateKey, X509Certificate> credentialPair = KeysForTests.generateCredential();
        final String certificate = Base64.encodeBytes(credentialPair.getValue().getEncoded());
        certificateAttribute = ApplicationAspectAttribute.newInstance()
                .setId(encryptionCertificateId)
                .setValue(certificate);
        attributes.add(certificateAttribute);
    }

    private void given_the_application_holds_the_entity_id() {
        ApplicationAspectAttributeId ssoEntityIdAttribute = ApplicationAspectAttributeId.newInstance()
                .setApplicationId(APPLICATION_ID)
                .setAspectCode(AuthAspectType.SP_SAML.name())
                .setAttributeCode(SPSAMLAuthAttributes.SP_SAML_ENTITY_ID.getValue());
        entityIdAttribute = ApplicationAspectAttribute.newInstance()
                .setId(ssoEntityIdAttribute)
                .setValue("APP_SP_SAML_ID");
        attributes.add(entityIdAttribute);
    }

    private void given_the_application_holds_sso_url() {
        final ApplicationAspectAttributeId ssoUrlId = ApplicationAspectAttributeId.newInstance()
                .setApplicationId(APPLICATION_ID)
                .setAspectCode(AuthAspectType.SP_SAML.name())
                .setAttributeCode(SPSAMLAuthAttributes.SP_SAML_SSO_URL.getValue());
        ssoUrlAttribute = ApplicationAspectAttribute.newInstance()
                .setId(ssoUrlId)
                .setValue("http://localhost/sp/saml/sso");
        attributes.add(ssoUrlAttribute);
    }

}
