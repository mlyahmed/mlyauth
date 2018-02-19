package com.mlyauth.utests.security.sso.idp.saml;

import com.google.common.collect.Sets;
import com.mlyauth.constants.AuthAspectType;
import com.mlyauth.constants.SPSAMLAuthAttributes;
import com.mlyauth.dao.ApplicationAspectAttributeDAO;
import com.mlyauth.domain.Application;
import com.mlyauth.domain.ApplicationAspectAttribute;
import com.mlyauth.domain.ApplicationAspectAttributeId;
import com.mlyauth.exception.IDPException;
import com.mlyauth.security.context.IContext;
import com.mlyauth.security.sso.SAMLHelper;
import com.mlyauth.security.sso.idp.saml.response.SAMLResponseGenerator;
import com.mlyauth.tools.KeysForTests;
import com.mlyauth.utests.security.context.MockContext;
import com.mlyauth.validators.ISPSAMLAspectValidator;
import javafx.util.Pair;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.*;
import org.opensaml.security.SAMLSignatureProfileValidator;
import org.opensaml.xml.encryption.DecryptionException;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.signature.SignatureValidator;
import org.opensaml.xml.util.Base64;
import org.springframework.security.saml.key.JKSKeyManager;
import org.springframework.security.saml.key.KeyManager;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.opensaml.saml2.core.SubjectConfirmation.METHOD_BEARER;

public class SAMLResponseGeneratorTest {
    private static final long APPLICATION_ID = 2522l;
    private static final String SP_SAMLSSO_URL = "http://localhost:8889/primainsure/S/S/O/saml/SSO";
    private static final String IDP_ENTITY_ID = "primainsureIDP";
    public static final String SP_ENTITY_ID = "SPPolicy";
    public static final String APP_NAME = "Policy";
    public static final String KEYSTORE_TEST_ALIAS = "sgi.prima-solutions.com";

    @InjectMocks
    private SAMLResponseGenerator generator;

    @Mock
    private ISPSAMLAspectValidator spsamlAspectValidator;

    @Mock
    private ApplicationAspectAttributeDAO appAspectAttrDAO;

    @Spy
    private SAMLHelper samlHelper = new SAMLHelper();

    @Spy
    private KeyManager keyManager;

    @Spy
    private IContext context = new MockContext();

    private List<ApplicationAspectAttribute> appAspectAttrobutes;
    private ApplicationAspectAttribute ssoUrlAttribute;
    private ApplicationAspectAttribute ssoEntityIdAttribute;
    private ApplicationAspectAttribute ssoEncryptionCertificateAttribute;
    private Application application;
    private Response response;
    private Assertion assertion;

    private Pair<PrivateKey, X509Certificate> credentialPair;
    private String encodedCertificate;

    @Before
    public void setup() throws Exception {
        given_application_credentials();
        given_current_idp_key_manager();
        DefaultBootstrap.bootstrap();
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(generator, "idpEntityId", IDP_ENTITY_ID);
        appAspectAttrobutes = new LinkedList<>();
        when(appAspectAttrDAO.findByAppAndAspect(APPLICATION_ID, AuthAspectType.SP_SAML.name())).thenReturn(appAspectAttrobutes);
        given_an_application(APPLICATION_ID, new AuthAspectType[]{AuthAspectType.SP_SAML});
        given_the_application_sp_sso_url(APPLICATION_ID, SP_SAMLSSO_URL);
        given_the_application_sp_entity_id(APPLICATION_ID, SP_ENTITY_ID);
        givent_the_application_sp_encryption_certificate(APPLICATION_ID);
    }


    @After
    public void tearsDown() {
        ((MockContext) context).resetMock();
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_generate_response_from_null_then_error() {
        generator.generate(null);
    }

    @Test
    public void given_a_saml_sp_app_when_generate_a_response_then_generate_it() throws Exception {
        when_generate_a_response();
        assertThat(response, notNullValue());
        assertThat(response.getID(), notNullValue());
        assertThat(response.getDestination(), equalTo(ssoUrlAttribute.getValue()));
        assertThat(response.getIssuer(), notNullValue());
        assertThat(response.getIssuer().getValue(), equalTo(IDP_ENTITY_ID));
        assertThat(response.getIssueInstant(), notNullValue());
        assertThat(response.getIssueInstant().isBeforeNow(), equalTo(true));
        assertThat(response.getStatus(), notNullValue());
        assertThat(response.getStatus().getStatusCode(), notNullValue());
        assertThat(response.getStatus().getStatusCode().getValue(), equalTo(StatusCode.SUCCESS_URI));
    }

    @Test
    public void given_a_saml_sp_app_when_generate_a_response_then_the_the_assertion_must_be_encrypted() throws Exception {
        when_generate_a_response();
        assertThat(response.getAssertions(), is(empty()));
        assertThat(response.getEncryptedAssertions(), hasSize(1));
    }


    @Test
    public void given_a_saml_sp_app_when_generate_a_response_then_the_assertion_must_be_well_encrypted() throws Exception {
        when_generate_a_response();
        and_decrypt_Assertion();
        assertThat(assertion, notNullValue());
    }


    @Test
    public void given_a_saml_sp_app_when_generate_a_response_then_the_assertion_must_be_a_successful() throws Exception {
        when_generate_a_response();
        and_decrypt_Assertion();
        assertThat(assertion.getID(), notNullValue());
        assertThat(assertion.getIssuer(), notNullValue());
        assertThat(assertion.getIssuer().getValue(), equalTo(IDP_ENTITY_ID));
        assertThat(assertion.getIssueInstant(), notNullValue());
        assertThat(assertion.getIssueInstant().isBeforeNow(), equalTo(true));
    }

    @Test
    public void given_a_saml_sp_app_when_generate_a_response_then_the_assertion_must_hold_a_subject_and_confirmation() throws Exception {
        when_generate_a_response();
        and_decrypt_Assertion();
        final Subject subject = assertion.getSubject();
        assertThat(subject, notNullValue());
        assertThat(subject.getNameID(), notNullValue());
        assertThat(subject.getNameID().getFormat(), notNullValue());
        assertThat(subject.getSubjectConfirmations(), hasSize(1));
        assertThat(subject.getSubjectConfirmations().get(0).getMethod(), equalTo(METHOD_BEARER));
        assertThat(subject.getSubjectConfirmations().get(0).getSubjectConfirmationData(), notNullValue());
        assertThat(subject.getSubjectConfirmations().get(0).getSubjectConfirmationData().getInResponseTo(), equalTo(SP_ENTITY_ID));
        assertThat(subject.getSubjectConfirmations().get(0).getSubjectConfirmationData().getRecipient(), equalTo(SP_SAMLSSO_URL));
        assertThat(subject.getSubjectConfirmations().get(0).getSubjectConfirmationData().getNotBefore(), nullValue());
        assertThat(subject.getSubjectConfirmations().get(0).getSubjectConfirmationData().getNotOnOrAfter(), notNullValue());
        assertThat(subject.getSubjectConfirmations().get(0).getSubjectConfirmationData().getNotOnOrAfter().isBefore((new DateTime()).plusMinutes(30).toInstant()), equalTo(true));
    }


    @Test
    public void given_a_saml_sp_app_when_generate_a_response_then_the_assertion_must_hold_an_auth_statement() throws Exception {
        when_generate_a_response();
        and_decrypt_Assertion();
        assertThat(assertion.getAuthnStatements(), hasSize(1));
        assertThat(assertion.getAuthnStatements().get(0).getAuthnInstant(), notNullValue());
        assertThat(assertion.getAuthnStatements().get(0).getAuthnInstant().isBeforeNow(), equalTo(true));
        assertThat(assertion.getAuthnStatements().get(0).getAuthnContext(), notNullValue());
        assertThat(assertion.getAuthnStatements().get(0).getAuthnContext().getAuthnContextClassRef(), notNullValue());
        assertThat(assertion.getAuthnStatements().get(0).getAuthnContext().getAuthnContextClassRef().getAuthnContextClassRef(), equalTo(AuthnContext.PASSWORD_AUTHN_CTX));
    }

    @Test
    public void given_a_saml_sp_app_when_generate_a_response_then_the_assertion_must_hold_conditions() throws Exception {
        when_generate_a_response();
        and_decrypt_Assertion();
        assertThat(assertion.getConditions(), notNullValue());
        assertThat(assertion.getConditions().getNotBefore(), nullValue());
        assertThat(assertion.getConditions().getNotOnOrAfter(), notNullValue());
        assertThat(assertion.getConditions().getNotOnOrAfter().isBefore((new DateTime()).plusMinutes(30).toInstant()), equalTo(true));
        assertThat(assertion.getConditions().getAudienceRestrictions(), hasSize(1));
        assertThat(assertion.getConditions().getAudienceRestrictions().get(0).getAudiences(), hasSize(1));
        assertThat(assertion.getConditions().getAudienceRestrictions().get(0).getAudiences().get(0).getAudienceURI(), equalTo(SP_ENTITY_ID));
    }

    @Test
    public void given_a_saml_sp_app_when_generate_a_response_then_the_assertion_must_hold_context_attributes() throws Exception {
        context.putAttribute("action", "A");
        context.putAttribute("profile", "CL");
        when_generate_a_response();
        and_decrypt_Assertion();
        assertThat(assertion.getAttributeStatements(), notNullValue());
        assertThat(assertion.getAttributeStatements(), hasSize(1));
        assertThat(assertion.getAttributeStatements().get(0).getAttributes(), hasSize(2));
        assertThat(getAttributeValue("action"), equalTo("A"));
        assertThat(getAttributeValue("profile"), equalTo("CL"));
    }

    private String getAttributeValue(String attributeName) {
        final Attribute actual = assertion.getAttributeStatements().get(0).getAttributes().stream().filter(attr -> attributeName.equals(attr.getName())).findFirst().get();
        return ((XSString) actual.getAttributeValues().get(0)).getValue();
    }

    @Test
    public void given_a_saml_sp_app_when_generate_a_response_then_it_must_be_signed() throws Exception {
        when_generate_a_response();
        assertThat(response.getSignature(), notNullValue());
        SAMLSignatureProfileValidator signatureProfileValidator = new SAMLSignatureProfileValidator();
        signatureProfileValidator.validate(response.getSignature());
        SignatureValidator sigValidator = new SignatureValidator(keyManager.getDefaultCredential());
        sigValidator.validate(response.getSignature());
    }

    @Test(expected = IDPException.class)
    public void when_the_sso_url_is_absent_then_error() throws Exception {
        Mockito.doThrow(IDPException.newInstance()).when(spsamlAspectValidator).validate(application);
        when_generate_a_response();
    }

    private void given_application_credentials() throws Exception {
        credentialPair = KeysForTests.generateCredential();
        encodedCertificate = Base64.encodeBytes(credentialPair.getValue().getEncoded());
    }

    private void given_an_application(long applicationId, AuthAspectType[] supportedAspects) {
        application = Application.newInstance()
                .setAppname(APP_NAME).setId(applicationId)
                .setAspects(Sets.newHashSet(supportedAspects));
    }

    private void given_the_application_sp_sso_url(long applicationId, String spSAMLSSOUrl) {
        final ApplicationAspectAttributeId ssoUrl = ApplicationAspectAttributeId.newInstance()
                .setApplicationId(applicationId)
                .setAspectCode(AuthAspectType.SP_SAML.name())
                .setAttributeCode(SPSAMLAuthAttributes.SP_SAML_SSO_URL.getValue());
        ssoUrlAttribute = ApplicationAspectAttribute.newInstance().setId(ssoUrl).setValue(spSAMLSSOUrl);
        appAspectAttrobutes.add(ssoUrlAttribute);
    }

    private void given_the_application_sp_entity_id(long applicationId, String entityId) {
        final ApplicationAspectAttributeId ssoEntityId = ApplicationAspectAttributeId.newInstance().setApplicationId(applicationId)
                .setAspectCode(AuthAspectType.SP_SAML.name())
                .setAttributeCode(SPSAMLAuthAttributes.SP_SAML_ENTITY_ID.getValue());
        ssoEntityIdAttribute = ApplicationAspectAttribute.newInstance().setId(ssoEntityId).setValue(entityId);
        appAspectAttrobutes.add(ssoEntityIdAttribute);
    }

    private void givent_the_application_sp_encryption_certificate(long applicationId) {
        final ApplicationAspectAttributeId ssoEncryptionCertificate = ApplicationAspectAttributeId.newInstance().setApplicationId(applicationId)
                .setAspectCode(AuthAspectType.SP_SAML.name())
                .setAttributeCode(SPSAMLAuthAttributes.SP_SAML_ENCRYPTION_CERTIFICATE.getValue());
        ssoEncryptionCertificateAttribute = ApplicationAspectAttribute.newInstance().setId(ssoEncryptionCertificate).setValue(encodedCertificate);
        appAspectAttrobutes.add(ssoEncryptionCertificateAttribute);
    }

    private void when_generate_a_response() throws Exception {
        response = generator.generate(application);
    }

    private void and_decrypt_Assertion() throws CertificateException, DecryptionException {
        BasicX509Credential credential = samlHelper.toBasicX509Credential(encodedCertificate);
        credential.setPrivateKey(credentialPair.getKey());
        assertion = samlHelper.decryptAssertion(response.getEncryptedAssertions().get(0), credential);
    }

    private void given_current_idp_key_manager() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        Map<String, String> passwords = new HashMap<String, String>();
        passwords.put(KEYSTORE_TEST_ALIAS, "Bourso$17");
        keyManager = new JKSKeyManager(loadKeyStore(), passwords, KEYSTORE_TEST_ALIAS);
    }

    private KeyStore loadKeyStore() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(getClass().getResourceAsStream("/keysstores/saml/samlKS.jks"), "Bourso$17".toCharArray());
        return ks;
    }
}
