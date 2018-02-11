package com.mlyauth.utests.security.sso.idp.saml;

import com.google.common.collect.Sets;
import com.mlyauth.constants.AuthAspectType;
import com.mlyauth.constants.SPSAMLAuthAttributes;
import com.mlyauth.dao.ApplicationAspectAttributeDAO;
import com.mlyauth.domain.Application;
import com.mlyauth.domain.ApplicationAspectAttribute;
import com.mlyauth.domain.ApplicationAspectAttributeId;
import com.mlyauth.security.sso.SAMLHelper;
import com.mlyauth.security.sso.idp.saml.response.IDPSAMLResponseGenerator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.StatusCode;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class IDPSAMLResponseGeneratorTest {
    private static final long APPLICATION_ID = 2522l;
    private static final String SP_SAMLSSO_URL = "http://localhost:8889/primainsure/S/S/O/saml/SSO";
    private static final String IDP_ENTITY_ID = "primainsureIDP";

    @InjectMocks
    private IDPSAMLResponseGenerator generator;

    @Mock
    private ApplicationAspectAttributeDAO appAspectAttrDAO;


    @Spy
    private SAMLHelper samlHelper = new SAMLHelper();

    private List<ApplicationAspectAttribute> appAspectAttrobutes;
    private ApplicationAspectAttribute ssoUrlAttribute;
    private ApplicationAspectAttribute ssoEntityIdAttribute;
    private ApplicationAspectAttribute ssoEncryptionCertificateAttribute;
    private Application application;
    private Response response;

    @Before
    public void setup() throws Exception {
        appAspectAttrobutes = new LinkedList<>();
        DefaultBootstrap.bootstrap();
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(generator, "idpEntityId", IDP_ENTITY_ID);
        when(appAspectAttrDAO.findByAppAndAspect(APPLICATION_ID, AuthAspectType.SP_SAML.name())).thenReturn(appAspectAttrobutes);
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_generate_response_from_null_then_error() throws Exception {
        generator.generate(null);
    }

    @Test
    public void given_a_saml_sp_app_when_generate_a_response_then_generate_it() throws Exception {
        given_an_application(APPLICATION_ID, new AuthAspectType[]{AuthAspectType.SP_SAML});
        given_the_application_sp_sso_url(APPLICATION_ID, SP_SAMLSSO_URL);
        given_the_application_sp_entity_id(APPLICATION_ID, IDP_ENTITY_ID);
        givent_the_application_sp_encryption_certificate(APPLICATION_ID);
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
    public void given_a_saml_sp_app_when_generate_a_response_the_the_assertion_must_be_encrypted() throws Exception {
        given_an_application(APPLICATION_ID, new AuthAspectType[]{AuthAspectType.SP_SAML});
        given_the_application_sp_sso_url(APPLICATION_ID, SP_SAMLSSO_URL);
        given_the_application_sp_entity_id(APPLICATION_ID, IDP_ENTITY_ID);
        givent_the_application_sp_encryption_certificate(APPLICATION_ID);
        when_generate_a_response();

        assertThat(response.getAssertions(), is(empty()));
        assertThat(response.getEncryptedAssertions(), hasSize(1));
    }

    private void given_an_application(long applicationId, AuthAspectType[] supportedAspects) {
        application = Application.newInstance()
                .setAppname("Policy").setId(applicationId)
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
        ssoEncryptionCertificateAttribute = ApplicationAspectAttribute.newInstance().setId(ssoEncryptionCertificate).setValue("MIIFFTCCA/2gAwIBAgIRAJC77w46ZihMZ1XjYS8RfRYwDQYJKoZIhvcNAQELBQAwXzELMAkGA1UEBhMCRlIxDjAMBgNVBAgTBVBhcmlzMQ4wDAYDVQQHEwVQYXJpczEOMAwGA1UEChMFR2FuZGkxIDAeBgNVBAMTF0dhbmRpIFN0YW5kYXJkIFNTTCBDQSAyMB4XDTE3MDUyNDAwMDAwMFoXDTE4MDUyNDIzNTk1OVowYjEhMB8GA1UECxMYRG9tYWluIENvbnRyb2wgVmFsaWRhdGVkMRswGQYDVQQLExJHYW5kaSBTdGFuZGFyZCBTU0wxIDAeBgNVBAMTF3NnaS5wcmltYS1zb2x1dGlvbnMuY29tMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnpSeVdowAqtclGgAHlFQ+rruYgsNYZ+7xeFStOB0Rrr7FCQAKvPXNoAD1lgKny/4Bs+UWtrhXwNxpibVvo0yCVSXctn+yQRBnLKJLsK8+2IfWHZrBHQiOAe2bc8mtW90XTRc2Jeb6ljPu61Uai17lXKXvHCafDkK6Xr5F0SQKGMA65sqqnlVZyT45ZUO8Jgypqd/94COB+9nBeIsVrKBlSPbwFhd2olyGqQr/yIlyNU7RnHtpSP+8JdNVH6S7dQR7wQt3oK907TfNSPa6RcD4yykrWDmz3yzqMLjwsh7j6LCqjC37PEMk45Bq4r9ei2c6xx6AjyNYypo8KbYktctTwIDAQABo4IBxzCCAcMwHwYDVR0jBBgwFoAUs5Cn2MmvTs1hPJ98rV1/Qf1pMOowHQYDVR0OBBYEFGJyeFB1ZFxi8Oiu7k7VuJb2Z/fBMA4GA1UdDwEB/wQEAwIFoDAMBgNVHRMBAf8EAjAAMB0GA1UdJQQWMBQGCCsGAQUFBwMBBggrBgEFBQcDAjBLBgNVHSAERDBCMDYGCysGAQQBsjEBAgIaMCcwJQYIKwYBBQUHAgEWGWh0dHBzOi8vY3BzLnVzZXJ0cnVzdC5jb20wCAYGZ4EMAQIBMEEGA1UdHwQ6MDgwNqA0oDKGMGh0dHA6Ly9jcmwudXNlcnRydXN0LmNvbS9HYW5kaVN0YW5kYXJkU1NMQ0EyLmNybDBzBggrBgEFBQcBAQRnMGUwPAYIKwYBBQUHMAKGMGh0dHA6Ly9jcnQudXNlcnRydXN0LmNvbS9HYW5kaVN0YW5kYXJkU1NMQ0EyLmNydDAlBggrBgEFBQcwAYYZaHR0cDovL29jc3AudXNlcnRydXN0LmNvbTA/BgNVHREEODA2ghdzZ2kucHJpbWEtc29sdXRpb25zLmNvbYIbd3d3LnNnaS5wcmltYS1zb2x1dGlvbnMuY29tMA0GCSqGSIb3DQEBCwUAA4IBAQCBHTW3H+WNfMMEBVj93GshddJ+MgoGht6GBGSaBG09bAKmuiXOhNZU4QkOLBrNsUdg6NfbUytD9m3GVo4TjJoEPFk+889Bz4kTQ4bwwPUa5BCkXsWUyPf8al2rTCjVCi8jkjzlo2++ts3//2XzUUuFQpLzs47Qf8fUw+QPUDSSqYmG8Cw7AiTfyHkAXJwMfb1GxDcG+fLEi76m5TOU6OWZoxXioggVufmge6rehuHQ4GHsM7qJUdBEekZNiDAauA+KgeT7UzbdpvPK19Sdo2FhgJvQvP34S7GsT7/7W7BeO8xNY1YiyIrcqzxwCB8kIsCTGCP5HLglYlSjs+QT20el");
        appAspectAttrobutes.add(ssoEncryptionCertificateAttribute);
    }

    private void when_generate_a_response() throws Exception {
        response = generator.generate(application);
    }
}
