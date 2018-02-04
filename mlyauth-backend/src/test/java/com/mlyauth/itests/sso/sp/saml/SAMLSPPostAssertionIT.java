package com.mlyauth.itests.sso.sp.saml;

import com.mlyauth.itests.AbstractIntegrationTest;
import com.mlyauth.security.saml.OpenSAMLUtils;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.*;
import org.opensaml.saml2.encryption.Encrypter;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.encryption.EncryptionConstants;
import org.opensaml.xml.encryption.EncryptionException;
import org.opensaml.xml.encryption.EncryptionParameters;
import org.opensaml.xml.encryption.KeyEncryptionParameters;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.credential.UsageType;
import org.opensaml.xml.security.keyinfo.KeyInfoGenerator;
import org.opensaml.xml.security.x509.X509KeyInfoGeneratorFactory;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureConstants;
import org.opensaml.xml.signature.SignatureException;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

public class SAMLSPPostAssertionIT extends AbstractIntegrationTest {

    public static final String SECU_EXCP_ATTR = "SPRING_SECURITY_LAST_EXCEPTION";
    public static final String TESTING_IDP_ENTITY_ID = "testingIDP";
    public static final String SP_ENTITY_ID = "primainsure4sgi";
    public static final String SP_ASSERTION_CONSUMER_ENDPOINT = "http://localhost/saml/sp/SSO";
    public static final String SP_SSO_ENDPOINT = "/saml/sp/SSO";

    @Autowired
    private Filter samlFilter;

    @Autowired
    private Filter metadataGeneratorFilter;

    @Autowired
    private WebApplicationContext wac;


    @Autowired
    private KeyManager keyManeger;

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

    @Before
    public void setup() {
        this.mockMvc = webAppContextSetup(this.wac).addFilters(metadataGeneratorFilter, samlFilter).build();

    }

    @Test
    public void when_post_null_Then_Error() throws Exception {
        ResultActions resultActions = mockMvc.perform(post(SP_SSO_ENDPOINT));
        resultActions.andExpect(forwardedUrl("/error.html"))
                .andExpect(unauthenticated())
                .andExpect(request().attribute(SECU_EXCP_ATTR, hasProperty("message", equalToIgnoringCase("Incoming SAML message is invalid"))));

    }


    //    When POST true assertion then OK
    @Test
    public void when_post_an_true_assertion_from_a_defined_idp_then_OK() throws Exception {
        given_idp_metadata();
        given_response();
        given_response_is_success();
        given_assertion();
        given_assertion_subject();
        given_assertion_auth_statement();
        given_assertion_audience();
        given_assertnion_is_encrypted();
        given_response_is_signed();

        ResultActions resultActions = mockMvc.perform(post(SP_SSO_ENDPOINT)
                .contentType(APPLICATION_FORM_URLENCODED_VALUE)
                .param("SAMLResponse", Base64.encodeBytes(OpenSAMLUtils.toString(response).getBytes())));

        resultActions
                .andExpect(request().attribute(SECU_EXCP_ATTR, nullValue()))
                .andExpect(redirectedUrl("/home.html"));

    }

    private void given_idp_metadata() throws SecurityException, MetadataProviderException {
        final XMLObjectBuilderFactory builderFactory = Configuration.getBuilderFactory();
        EntityDescriptor metadata = OpenSAMLUtils.buildSAMLObject(EntityDescriptor.class);
        metadata.setEntityID(TESTING_IDP_ENTITY_ID);
        IDPSSODescriptor spSSODescriptor = OpenSAMLUtils.buildSAMLObject(IDPSSODescriptor.class);
        X509KeyInfoGeneratorFactory keyInfoGeneratorFactory = new X509KeyInfoGeneratorFactory();
        keyInfoGeneratorFactory.setEmitEntityCertificate(true);
        KeyInfoGenerator keyInfoGenerator = keyInfoGeneratorFactory.newInstance();
        KeyDescriptor encKeyDescriptor = OpenSAMLUtils.buildSAMLObject(KeyDescriptor.class);
        encKeyDescriptor.setUse(UsageType.ENCRYPTION);
        encKeyDescriptor.setKeyInfo(keyInfoGenerator.generate(keyManeger.getDefaultCredential()));
        spSSODescriptor.getKeyDescriptors().add(encKeyDescriptor);
        KeyDescriptor signKeyDescriptor = OpenSAMLUtils.buildSAMLObject(KeyDescriptor.class);
        signKeyDescriptor.setUse(UsageType.SIGNING);
        signKeyDescriptor.setKeyInfo(keyInfoGenerator.generate(keyManeger.getDefaultCredential()));
        spSSODescriptor.getKeyDescriptors().add(signKeyDescriptor);
        spSSODescriptor.addSupportedProtocol(SAMLConstants.SAML20P_NS);
        metadata.getRoleDescriptors().add(spSSODescriptor);
        metadataManager.addMetadataProvider(new MetadataMemoryProvider(metadata));
        metadataManager.refreshMetadata();
    }

    private void given_response() {
        artifactResponse = OpenSAMLUtils.buildSAMLObject(ArtifactResponse.class);
        Issuer issuer = OpenSAMLUtils.buildSAMLObject(Issuer.class);
        issuer.setValue(TESTING_IDP_ENTITY_ID);
        artifactResponse.setIssuer(issuer);
        artifactResponse.setIssueInstant(new DateTime());
        artifactResponse.setDestination(SP_ASSERTION_CONSUMER_ENDPOINT);
        artifactResponse.setID(OpenSAMLUtils.generateRandomId());


        response = OpenSAMLUtils.buildSAMLObject(Response.class);
        response.setDestination(SP_ASSERTION_CONSUMER_ENDPOINT);
        response.setIssueInstant(new DateTime());
        response.setID(OpenSAMLUtils.generateRandomId());
        Issuer issuer2 = OpenSAMLUtils.buildSAMLObject(Issuer.class);
        issuer2.setValue(TESTING_IDP_ENTITY_ID);
        response.setIssuer(issuer2);
        artifactResponse.setMessage(response);
    }

    private void given_response_is_success() {
        responseStatus = OpenSAMLUtils.buildSAMLObject(Status.class);
        StatusCode statusCode = OpenSAMLUtils.buildSAMLObject(StatusCode.class);
        statusCode.setValue(StatusCode.SUCCESS_URI);
        responseStatus.setStatusCode(statusCode);
        response.setStatus(responseStatus);
    }

    private void given_assertion() {
        assertion = OpenSAMLUtils.buildSAMLObject(Assertion.class);
        Issuer issuer3 = OpenSAMLUtils.buildSAMLObject(Issuer.class);
        issuer3.setValue(TESTING_IDP_ENTITY_ID);
        assertion.setIssuer(issuer3);
        assertion.setIssueInstant(new DateTime());
        assertion.setID(OpenSAMLUtils.generateRandomId());
    }

    private void given_assertion_subject() {
        assertionSubject = OpenSAMLUtils.buildSAMLObject(Subject.class);
        NameID nameID = OpenSAMLUtils.buildSAMLObject(NameID.class);
        nameID.setFormat(NameIDType.TRANSIENT);
        assertionSubject.setNameID(nameID);
        nameID.setValue("ahmed.elidrissi.attach@gmail.com");

        subjectConfirmation = OpenSAMLUtils.buildSAMLObject(SubjectConfirmation.class);
        subjectConfirmation.setMethod(SubjectConfirmation.METHOD_BEARER);
        SubjectConfirmationData subjectConfirmationData = OpenSAMLUtils.buildSAMLObject(SubjectConfirmationData.class);
        subjectConfirmationData.setInResponseTo(SP_ENTITY_ID);
        subjectConfirmationData.setNotOnOrAfter(new DateTime().plusDays(2));
        subjectConfirmationData.setRecipient(SP_ASSERTION_CONSUMER_ENDPOINT);
        subjectConfirmation.setSubjectConfirmationData(subjectConfirmationData);
        assertionSubject.getSubjectConfirmations().add(subjectConfirmation);
        assertion.setSubject(assertionSubject);
    }

    private void given_assertion_auth_statement() {
        assertionAuthnStatement = OpenSAMLUtils.buildSAMLObject(AuthnStatement.class);
        AuthnContext authnContext = OpenSAMLUtils.buildSAMLObject(AuthnContext.class);
        AuthnContextClassRef authnContextClassRef = OpenSAMLUtils.buildSAMLObject(AuthnContextClassRef.class);
        authnContextClassRef.setAuthnContextClassRef(AuthnContext.PASSWORD_AUTHN_CTX);
        authnContext.setAuthnContextClassRef(authnContextClassRef);
        assertionAuthnStatement.setAuthnContext(authnContext);
        assertionAuthnStatement.setAuthnInstant(new DateTime());
        assertion.getAuthnStatements().add(assertionAuthnStatement);
    }

    private void given_assertion_audience() {
        assertionConditions = OpenSAMLUtils.buildSAMLObject(Conditions.class);
        assertionConditions.setNotOnOrAfter(new DateTime().plusDays(2));
        AudienceRestriction audienceRestriction = OpenSAMLUtils.buildSAMLObject(AudienceRestriction.class);
        assertionAudience = OpenSAMLUtils.buildSAMLObject(Audience.class);
        assertionAudience.setAudienceURI(SP_ENTITY_ID);
        audienceRestriction.getAudiences().add(assertionAudience);
        assertionConditions.getAudienceRestrictions().add(audienceRestriction);
        assertion.setConditions(assertionConditions);
    }

    private void given_assertnion_is_encrypted() throws EncryptionException {
        EncryptionParameters encryptionParameters = new EncryptionParameters();
        encryptionParameters.setAlgorithm(EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES128);
        KeyEncryptionParameters keyEncryptionParameters = new KeyEncryptionParameters();
        keyEncryptionParameters.setEncryptionCredential(keyManeger.getDefaultCredential());
        keyEncryptionParameters.setAlgorithm(EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSAOAEP);
        Encrypter encrypter = new Encrypter(encryptionParameters, keyEncryptionParameters);
        encrypter.setKeyPlacement(Encrypter.KeyPlacement.INLINE);
        EncryptedAssertion encryptedAssertion = encrypter.encrypt(assertion);
        response.getEncryptedAssertions().add(encryptedAssertion);
    }

    private void given_response_is_signed() throws MarshallingException, SignatureException {
        Signature signature = OpenSAMLUtils.buildSAMLObject(Signature.class);
        signature.setSigningCredential(keyManeger.getDefaultCredential());
        signature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1);
        signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        response.setSignature(signature);
        Configuration.getMarshallerFactory().getMarshaller(response).marshall(response);
        Signer.signObject(signature);
    }


//    When POST true assertion and error on attributes then error


//    when POST false assertion then error
//    When REDIRECT, true or false, assertion then error
//    When PAOS assertion then error

}
