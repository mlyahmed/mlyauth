package com.primasolutions.idp.sp.saml;

import com.primasolutions.idp.constants.AspectAttribute;
import com.primasolutions.idp.constants.AspectType;
import com.primasolutions.idp.dao.ApplicationAspectAttributeDAO;
import com.primasolutions.idp.domain.AppAspAttr;
import com.primasolutions.idp.domain.Application;
import com.primasolutions.idp.domain.ApplicationAspectAttributeId;
import com.primasolutions.idp.exception.BadSPSAMLAspectAttributeValueException;
import com.primasolutions.idp.exception.MissingSPSAMLAspectAttributeException;
import com.primasolutions.idp.exception.NotSPSAMLApplicationException;
import com.primasolutions.idp.token.saml.SAMLHelper;
import com.primasolutions.idp.tools.KeysForTests;
import javafx.util.Pair;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opensaml.xml.util.Base64;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import static com.primasolutions.idp.constants.AspectType.SP_SAML;
import static org.mockito.Mockito.when;

public class SPSAMLAspectValidatorTest {

    public static final long APPLICATION_ID = 2125485L;

    @InjectMocks
    private SPSAMLAspectValidator validator;

    @Mock
    private ApplicationAspectAttributeDAO appAspectAttrDAO;

    private Application application;
    private List<AppAspAttr> attributes;
    private AppAspAttr certificateAttribute;
    private AppAspAttr ssoUrlAttribute;
    private AppAspAttr entityIdAttribute;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(validator, "samlHelper", new SAMLHelper());
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
                .setAspectCode(AspectType.SP_SAML.name())
                .setAttributeCode(AspectAttribute.SP_SAML_ENCRYPTION_CERTIFICATE.getValue());
        final Pair<PrivateKey, X509Certificate> credentialPair = KeysForTests.generateRSACredential();
        final String certificate = Base64.encodeBytes(credentialPair.getValue().getEncoded());
        certificateAttribute = AppAspAttr.newInstance()
                .setId(encryptionCertificateId)
                .setValue(certificate);
        attributes.add(certificateAttribute);
    }

    private void given_the_application_holds_the_entity_id() {
        ApplicationAspectAttributeId ssoEntityIdAttribute = ApplicationAspectAttributeId.newInstance()
                .setApplicationId(APPLICATION_ID)
                .setAspectCode(AspectType.SP_SAML.name())
                .setAttributeCode(AspectAttribute.SP_SAML_ENTITY_ID.getValue());
        entityIdAttribute = AppAspAttr.newInstance()
                .setId(ssoEntityIdAttribute)
                .setValue("APP_SP_SAML_ID");
        attributes.add(entityIdAttribute);
    }

    private void given_the_application_holds_sso_url() {
        final ApplicationAspectAttributeId ssoUrlId = ApplicationAspectAttributeId.newInstance()
                .setApplicationId(APPLICATION_ID)
                .setAspectCode(AspectType.SP_SAML.name())
                .setAttributeCode(AspectAttribute.SP_SAML_SSO_URL.getValue());
        ssoUrlAttribute = AppAspAttr.newInstance()
                .setId(ssoUrlId)
                .setValue("http://localhost/sp/saml/sso");
        attributes.add(ssoUrlAttribute);
    }

}
