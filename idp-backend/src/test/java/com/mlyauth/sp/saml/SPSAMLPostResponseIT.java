package com.mlyauth.sp.saml;

import com.google.common.collect.Iterators;
import com.mlyauth.AbstractIntegrationTest;
import com.mlyauth.dao.NavigationDAO;
import com.mlyauth.domain.Navigation;
import com.mlyauth.exception.IDPSAMLErrorException;
import com.mlyauth.token.TokenIdGenerator;
import com.mlyauth.token.saml.SAMLHelper;
import org.apache.commons.codec.digest.DigestUtils;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.ArtifactResponse;
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
import org.opensaml.saml2.core.StatusMessage;
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.core.SubjectConfirmation;
import org.opensaml.saml2.core.SubjectConfirmationData;
import org.opensaml.saml2.encryption.Encrypter;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.encryption.EncryptionConstants;
import org.opensaml.xml.encryption.EncryptionParameters;
import org.opensaml.xml.encryption.KeyEncryptionParameters;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.credential.UsageType;
import org.opensaml.xml.security.keyinfo.KeyInfoGenerator;
import org.opensaml.xml.security.x509.X509KeyInfoGeneratorFactory;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureConstants;
import org.opensaml.xml.signature.Signer;
import org.opensaml.xml.util.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.saml.key.KeyManager;
import org.springframework.security.saml.metadata.MetadataManager;
import org.springframework.security.saml.metadata.MetadataMemoryProvider;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.Filter;
import java.util.List;

import static com.mlyauth.constants.Direction.INBOUND;
import static com.mlyauth.token.Claims.ACTION;
import static com.mlyauth.token.Claims.APPLICATION;
import static com.mlyauth.token.Claims.CLIENT_ID;
import static com.mlyauth.token.Claims.CLIENT_PROFILE;
import static com.mlyauth.token.Claims.ENTITY_ID;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

public class SPSAMLPostResponseIT extends AbstractIntegrationTest {

    public static final String SECU_EXCP_ATTR = "SPRING_SECURITY_LAST_EXCEPTION";
    public static final String TESTING_IDP_ENTITY_ID = "testingIDP";
    public static final String SP_ENTITY_ID = "primainsure4sgi";
    public static final String SP_ASSERTION_CONSUMER_ENDPOINT = "http://localhost/sp/saml/sso";
    public static final String SP_SSO_ENDPOINT = "/sp/saml/sso";

    @Autowired
    private TokenIdGenerator idGenerator;

    @Autowired
    private NavigationDAO navigationDAO;

    @Autowired
    private SAMLHelper samlHelper;

    @Autowired
    private Filter samlFilter;

    @Autowired
    private Filter metadataGeneratorFilter;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private KeyManager keyManager;

    @Autowired
    private MetadataManager metadataManager;

    private MockMvc mockMvc;


    private ArtifactResponse artifactResponse;
    private Response response;
    private Status responseStatus;
    private Assertion assertion;
    private Subject assertionSubject;
    private SubjectConfirmation subjectConfirmation;
    private AuthnStatement assertionAuthnStatement;
    private Conditions assertionConditions;
    private Audience assertionAudience;
    private ResultActions resultActions;
    private AttributeStatement attributeStatement;
    private String serialized;

    @Before
    public void setup() throws Exception {
        this.mockMvc = webAppContextSetup(this.wac).addFilters(metadataGeneratorFilter, samlFilter).build();
        given_testing_idp_metadata();
        given_response();
        given_assertion();
    }

    @Test
    public void when_post_null_Then_Error() throws Exception {
        resultActions = mockMvc.perform(post(SP_SSO_ENDPOINT));
        then_error();

    }

    @Test
    public void when_post_a_true_response_from_a_defined_idp_then_OK() {
        given_response_is_success();
        given_assertion_subject();
        given_assertion_auth_statement();
        given_assertion_audience();
        given_the_assertion_minimum_valid_attributes();
        given_assertion_is_encrypted();
        given_response_is_signed();
        when_post_response();
        then_authenticated();
    }

    @Test
    public void when_post_a_true_response_from_a_defined_idp_and_target_an_existed_app_then_navigate_to_the_app() {
        given_response_is_success();
        given_assertion_subject();
        given_assertion_auth_statement();
        given_assertion_audience();
        given_the_assertion_minimum_valid_attributes();
        given_the_target_app();
        given_assertion_is_encrypted();
        given_response_is_signed();
        when_post_response();
        then_navigate_to_the_app();
    }

    @Test
    public void when_post_a_true_response_from_a_defined_idp_then_trace_a_navigation() {
        navigationDAO.deleteAll();
        given_response_is_success();
        given_assertion_subject();
        given_assertion_auth_statement();
        given_assertion_audience();
        given_the_assertion_minimum_valid_attributes();
        given_the_target_app();
        given_assertion_is_encrypted();
        given_response_is_signed();
        when_post_response();
        then_a_navigation_is_traced();
    }

    @Test
    public void when_post_a_true_response_from_undefined_idp_then_error() {
        given_response_is_success();
        given_assertion_subject();
        given_assertion_auth_statement();
        given_assertion_audience();
        and_the_response_issuer_is_undefined();
        given_assertion_is_encrypted();
        given_response_is_signed();
        when_post_response();
        then_error();
    }

    @Test
    public void when_post_a_false_response_then_error() {
        given_response_is_false();
        given_assertion_subject();
        given_assertion_auth_statement();
        given_assertion_audience();
        given_assertion_is_encrypted();
        given_response_is_signed();
        when_post_response();
        then_error();
    }


    @Test
    public void when_post_a_true_response_not_signed_then_error() {
        given_response_is_success();
        given_assertion_subject();
        given_assertion_auth_statement();
        given_assertion_audience();
        given_the_assertion_minimum_valid_attributes();
        given_assertion_is_encrypted();
        when_post_response();
        then_error();
    }


    @Test
    public void when_post_an_true_response_not_encrypted_then_error() {
        given_response_is_success();
        given_assertion_subject();
        given_assertion_auth_statement();
        given_assertion_audience();
        given_the_assertion_minimum_valid_attributes();
        given_response_is_signed();
        when_post_response();
        then_error();
    }

    private void and_the_response_issuer_is_undefined() {
        artifactResponse.getIssuer().setValue("undefined");
        response.getIssuer().setValue("undefined");
        assertion.getIssuer().setValue("undefined");
    }

    private void given_testing_idp_metadata() throws SecurityException, MetadataProviderException {
        EntityDescriptor metadata = samlHelper.buildSAMLObject(EntityDescriptor.class);
        metadata.setEntityID(TESTING_IDP_ENTITY_ID);
        IDPSSODescriptor idpDescriptor = samlHelper.buildSAMLObject(IDPSSODescriptor.class);
        X509KeyInfoGeneratorFactory keyInfoGeneratorFactory = new X509KeyInfoGeneratorFactory();
        keyInfoGeneratorFactory.setEmitEntityCertificate(true);
        KeyInfoGenerator keyInfoGenerator = keyInfoGeneratorFactory.newInstance();
        KeyDescriptor encKeyDescriptor = samlHelper.buildSAMLObject(KeyDescriptor.class);
        encKeyDescriptor.setUse(UsageType.ENCRYPTION);
        encKeyDescriptor.setKeyInfo(keyInfoGenerator.generate(keyManager.getDefaultCredential()));
        idpDescriptor.getKeyDescriptors().add(encKeyDescriptor);
        KeyDescriptor signKeyDescriptor = samlHelper.buildSAMLObject(KeyDescriptor.class);
        signKeyDescriptor.setUse(UsageType.SIGNING);
        signKeyDescriptor.setKeyInfo(keyInfoGenerator.generate(keyManager.getDefaultCredential()));
        idpDescriptor.getKeyDescriptors().add(signKeyDescriptor);
        idpDescriptor.addSupportedProtocol(SAMLConstants.SAML20P_NS);
        metadata.getRoleDescriptors().add(idpDescriptor);
        metadataManager.addMetadataProvider(new MetadataMemoryProvider(metadata));
        metadataManager.setRefreshCheckInterval(0);
        metadataManager.refreshMetadata();
    }

    private void given_response() {
        artifactResponse = samlHelper.buildSAMLObject(ArtifactResponse.class);
        Issuer issuer = samlHelper.buildSAMLObject(Issuer.class);
        issuer.setValue(TESTING_IDP_ENTITY_ID);
        artifactResponse.setIssuer(issuer);
        artifactResponse.setIssueInstant(new DateTime());
        artifactResponse.setDestination(SP_ASSERTION_CONSUMER_ENDPOINT);
        artifactResponse.setID(idGenerator.generateId());


        response = samlHelper.buildSAMLObject(Response.class);
        response.setDestination(SP_ASSERTION_CONSUMER_ENDPOINT);
        response.setIssueInstant(new DateTime());
        response.setID(idGenerator.generateId());
        Issuer issuer2 = samlHelper.buildSAMLObject(Issuer.class);
        issuer2.setValue(TESTING_IDP_ENTITY_ID);
        response.setIssuer(issuer2);
        artifactResponse.setMessage(response);
    }

    private void given_response_is_success() {
        responseStatus = samlHelper.buildSAMLObject(Status.class);
        StatusCode statusCode = samlHelper.buildSAMLObject(StatusCode.class);
        statusCode.setValue(StatusCode.SUCCESS_URI);
        responseStatus.setStatusCode(statusCode);
        response.setStatus(responseStatus);
    }

    private void given_response_is_false() {
        responseStatus = samlHelper.buildSAMLObject(Status.class);
        StatusCode statusCode = samlHelper.buildSAMLObject(StatusCode.class);
        StatusMessage statusMessage = samlHelper.buildSAMLObject(StatusMessage.class);
        statusCode.setValue(StatusCode.AUTHN_FAILED_URI);
        statusMessage.setMessage("Failed");
        responseStatus.setStatusCode(statusCode);
        responseStatus.setStatusMessage(statusMessage);
        response.setStatus(responseStatus);
    }

    private void given_assertion() {
        assertion = samlHelper.buildSAMLObject(Assertion.class);
        Issuer issuer3 = samlHelper.buildSAMLObject(Issuer.class);
        issuer3.setValue(TESTING_IDP_ENTITY_ID);
        assertion.setIssuer(issuer3);
        assertion.setIssueInstant(new DateTime());
        assertion.setID(idGenerator.generateId());
    }

    private void given_assertion_subject() {
        assertionSubject = samlHelper.buildSAMLObject(Subject.class);
        NameID nameID = samlHelper.buildSAMLObject(NameID.class);
        nameID.setFormat(NameIDType.TRANSIENT);
        assertionSubject.setNameID(nameID);
        nameID.setValue("ahmed.elidrissi.attach@gmail.com");

        subjectConfirmation = samlHelper.buildSAMLObject(SubjectConfirmation.class);
        subjectConfirmation.setMethod(SubjectConfirmation.METHOD_BEARER);
        SubjectConfirmationData subjectConfirmationData = samlHelper.buildSAMLObject(SubjectConfirmationData.class);
        subjectConfirmationData.setInResponseTo(SP_ENTITY_ID);
        subjectConfirmationData.setNotOnOrAfter(new DateTime().plusDays(2));
        subjectConfirmationData.setRecipient(SP_ASSERTION_CONSUMER_ENDPOINT);
        subjectConfirmation.setSubjectConfirmationData(subjectConfirmationData);
        assertionSubject.getSubjectConfirmations().add(subjectConfirmation);
        assertion.setSubject(assertionSubject);
    }

    private void given_assertion_auth_statement() {
        assertionAuthnStatement = samlHelper.buildSAMLObject(AuthnStatement.class);
        AuthnContext authnContext = samlHelper.buildSAMLObject(AuthnContext.class);
        AuthnContextClassRef authnContextClassRef = samlHelper.buildSAMLObject(AuthnContextClassRef.class);
        authnContextClassRef.setAuthnContextClassRef(AuthnContext.PASSWORD_AUTHN_CTX);
        authnContext.setAuthnContextClassRef(authnContextClassRef);
        assertionAuthnStatement.setAuthnContext(authnContext);
        assertionAuthnStatement.setAuthnInstant(new DateTime());
        assertion.getAuthnStatements().add(assertionAuthnStatement);
    }

    private void given_assertion_audience() {
        assertionConditions = samlHelper.buildSAMLObject(Conditions.class);
        assertionConditions.setNotOnOrAfter(new DateTime().plusDays(2));
        AudienceRestriction audienceRestriction = samlHelper.buildSAMLObject(AudienceRestriction.class);
        assertionAudience = samlHelper.buildSAMLObject(Audience.class);
        assertionAudience.setAudienceURI(SP_ENTITY_ID);
        audienceRestriction.getAudiences().add(assertionAudience);
        assertionConditions.getAudienceRestrictions().add(audienceRestriction);
        assertion.setConditions(assertionConditions);
    }

    private void given_the_assertion_minimum_valid_attributes() {
        attributeStatement = samlHelper.buildSAMLObject(AttributeStatement.class);
        final List<Attribute> attributes = attributeStatement.getAttributes();
        attributes.add(samlHelper.buildStringAttribute(CLIENT_ID.getValue(), "9000")); //See person-examples.sql
        attributes.add(samlHelper.buildStringAttribute(CLIENT_PROFILE.getValue(), "GP"));
        attributes.add(samlHelper.buildStringAttribute(ENTITY_ID.getValue(), "BA0000000000001"));
        attributes.add(samlHelper.buildStringAttribute(ACTION.getValue(), "S"));
        assertion.getAttributeStatements().add(attributeStatement);
    }


    private void given_assertion_is_encrypted() {
        try {
            EncryptionParameters encryptionParameters = new EncryptionParameters();
            encryptionParameters.setAlgorithm(EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES128);
            KeyEncryptionParameters keyEncryptionParameters = new KeyEncryptionParameters();
            keyEncryptionParameters.setEncryptionCredential(keyManager.getDefaultCredential());
            keyEncryptionParameters.setAlgorithm(EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSAOAEP);
            Encrypter encrypter = new Encrypter(encryptionParameters, keyEncryptionParameters);
            encrypter.setKeyPlacement(Encrypter.KeyPlacement.INLINE);
            EncryptedAssertion encryptedAssertion = encrypter.encrypt(assertion);
            response.getEncryptedAssertions().add(encryptedAssertion);
        } catch (Exception e) {
            throw IDPSAMLErrorException.newInstance(e);
        }
    }

    private void given_response_is_signed() {
        try {
            Signature signature = samlHelper.buildSAMLObject(Signature.class);
            signature.setSigningCredential(keyManager.getDefaultCredential());
            signature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1);
            signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
            response.setSignature(signature);
            Configuration.getMarshallerFactory().getMarshaller(response).marshall(response);
            Signer.signObject(signature);
        } catch (Exception e) {
            throw IDPSAMLErrorException.newInstance(e);
        }
    }

    private void given_the_target_app() {
        final List<Attribute> attributes = attributeStatement.getAttributes();
        attributes.add(samlHelper.buildStringAttribute(APPLICATION.getValue(), "PolicyDev"));
    }

    private void when_post_response() {
        try {
            serialized = Base64.encodeBytes(samlHelper.toString(response).getBytes());
            resultActions = mockMvc.perform(post(SP_SSO_ENDPOINT)
                    .contentType(APPLICATION_FORM_URLENCODED_VALUE)
                    .param("SAMLResponse", serialized));
        } catch (Exception e) {
            throw IDPSAMLErrorException.newInstance(e);
        }
    }


    private void then_authenticated() {
        try {
            resultActions
                    .andExpect(request().attribute(SECU_EXCP_ATTR, nullValue()))
                    .andExpect(redirectedUrl("/home.html"));
        } catch (Exception e) {
            throw IDPSAMLErrorException.newInstance(e);
        }
    }

    private void then_navigate_to_the_app() {
        try {
            resultActions
                    .andExpect(request().attribute(SECU_EXCP_ATTR, nullValue()))
                    .andExpect(redirectedUrl("/navigate/forward/to/PolicyDev"));
        } catch (Exception e) {
            throw IDPSAMLErrorException.newInstance(e);
        }
    }

    private void then_error() {
        try {
            resultActions.andExpect(forwardedUrl("/401.html"))
                    .andExpect(unauthenticated())
                    .andExpect(request().attribute(SECU_EXCP_ATTR, hasProperty("message", notNullValue())));
        } catch (Exception e) {
            throw IDPSAMLErrorException.newInstance(e);
        }
    }

    private void then_a_navigation_is_traced() {
        final Iterable<Navigation> navigations = navigationDAO.findAll();
        assertThat(navigations, notNullValue());
        assertThat(Iterators.size(navigations.iterator()), equalTo(1));

        final Navigation navigation = Iterators.getLast(navigations.iterator());
        assertThat(navigation.getCreatedAt(), notNullValue());
        assertThat(navigation.getDirection(), equalTo(INBOUND));
        assertThat(navigation.getTargetURL(), equalTo(response.getDestination()));
        assertThat(navigation.getToken(), notNullValue());
        assertThat(navigation.getToken().getChecksum(), equalTo(DigestUtils.sha256Hex(serialized)));
        assertThat(navigation.getSession(), notNullValue());
    }


    //TODO    When PAOS assertion then error
    //TODO    When REDIRECT, true or false, assertion then error
}
