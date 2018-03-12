package com.mlyauth.token.saml;

import com.mlyauth.constants.TokenStatus;
import com.mlyauth.exception.IDPSAMLErrorException;
import com.mlyauth.tools.KeysForTests;
import javafx.util.Pair;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.*;
import org.opensaml.saml2.encryption.Encrypter;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.encryption.EncryptionConstants;
import org.opensaml.xml.encryption.EncryptionParameters;
import org.opensaml.xml.encryption.KeyEncryptionParameters;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureConstants;
import org.opensaml.xml.signature.Signer;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

import static com.mlyauth.token.IDPClaims.*;
import static com.mlyauth.tools.RandomForTests.randomString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.opensaml.xml.util.Base64.encodeBytes;

public class SAMLCypheredAccessTokenTest {

    private SAMLAccessToken token;
    private SAMLHelper samlHelper;
    private Credential cypherCred;
    private Credential decipherCred;
    private Response response;
    private Assertion assertion;
    private Subject subject;
    private Audience audience;
    private String serialized;
    private Attribute state;
    private Attribute scopes;
    private Attribute bp;
    private Attribute delegator;
    private Attribute delegate;

    @Before
    public void setup() throws ConfigurationException {
        DefaultBootstrap.bootstrap();
        samlHelper = new SAMLHelper();
        set_up_credentials();
        set_up_token();
        set_up_assertion();
        set_up_attributes();
        set_up_conditions();
        set_up_subject();
        set_up_response();
        given_the_claims_are_cyphered_and_serialized();
    }

    @Test
    public void the_token_status_must_be_cyphered() {
        token = new SAMLAccessToken(serialized, decipherCred);
        assertThat(token.getStatus(), equalTo(TokenStatus.CYPHERED));
    }


    @Test
    public void when_decipher_then_it_must_be_deciphered() {
        when_decipher_the_token();
        assertThat(token.getStatus(), equalTo(TokenStatus.DECIPHERED));
    }


    @Test
    public void when_given_cyphered_token_then_the_stamp_is_loaded() {
        when_decipher_the_token();
        assertThat(token.getStamp(), equalTo(response.getID()));
    }

    @Test
    public void when_given_cyphered_token_then_the_subject_is_loaded() {
        when_decipher_the_token();
        assertThat(token.getSubject(), equalTo(subject.getNameID().getValue()));
    }

    private void set_up_token() {
        token = new SAMLAccessToken(cypherCred);
        ReflectionTestUtils.setField(token, "samlHelper", samlHelper);
    }

    private void set_up_credentials() {
        final Pair<PrivateKey, X509Certificate> pair1 = KeysForTests.generateRSACredential();
        final Pair<PrivateKey, X509Certificate> pair2 = KeysForTests.generateRSACredential();
        cypherCred = samlHelper.toCredential(pair1.getKey(), pair2.getValue());
        decipherCred = samlHelper.toCredential(pair2.getKey(), pair1.getValue());
    }

    private void set_up_assertion() {
        assertion = samlHelper.buildSAMLObject(Assertion.class);
        final AuthnStatement authnStatement = samlHelper.buildSAMLObject(AuthnStatement.class);
        final AuthnContext authnContext = samlHelper.buildSAMLObject(AuthnContext.class);
        AuthnContextClassRef authnContextClassRef = samlHelper.buildSAMLObject(AuthnContextClassRef.class);
        authnContextClassRef.setAuthnContextClassRef(AuthnContext.PASSWORD_AUTHN_CTX);
        authnContext.setAuthnContextClassRef(authnContextClassRef);
        authnStatement.setAuthnContext(authnContext);
        authnStatement.setAuthnInstant(DateTime.now());
        assertion.getAuthnStatements().add(authnStatement);
        assertion.setIssuer(samlHelper.buildSAMLObject(Issuer.class));
        assertion.setIssueInstant(DateTime.now());
        assertion.setID(randomString());
        assertion.getIssuer().setValue(randomString());
    }

    private void set_up_attributes() {
        AttributeStatement attributeStatement = samlHelper.buildSAMLObject(AttributeStatement.class);
        final List<Attribute> assertionAttributes = attributeStatement.getAttributes();
        state = samlHelper.buildStringAttribute(STATE.getValue(), randomString());
        scopes = samlHelper.buildStringAttribute(SCOPES.getValue(), randomString());
        bp = samlHelper.buildStringAttribute(BP.getValue(), randomString());
        delegator = samlHelper.buildStringAttribute(DELEGATOR.getValue(), randomString());
        delegate = samlHelper.buildStringAttribute(DELEGATE.getValue(), randomString());
        assertionAttributes.addAll(Arrays.asList(state, scopes, bp, delegator, delegate));
        assertion.getAttributeStatements().add(attributeStatement);
    }

    private void set_up_conditions() {
        audience = samlHelper.buildSAMLObject(Audience.class);
        final Conditions conditions = samlHelper.buildSAMLObject(Conditions.class);
        AudienceRestriction audienceRestriction = samlHelper.buildSAMLObject(AudienceRestriction.class);
        audienceRestriction.getAudiences().add(audience);
        conditions.getAudienceRestrictions().add(audienceRestriction);
        conditions.setNotOnOrAfter(DateTime.now().plusMinutes(20));
        assertion.setConditions(conditions);
    }

    private void set_up_subject() {
        subject = samlHelper.buildSAMLObject(Subject.class);
        NameID nameID = samlHelper.buildSAMLObject(NameID.class);
        nameID.setValue(randomString());
        nameID.setFormat(NameIDType.TRANSIENT);
        subject.setNameID(nameID);
        final SubjectConfirmation subjectConfirmation = samlHelper.buildSAMLObject(SubjectConfirmation.class);
        final SubjectConfirmationData subjectConfirmationData = samlHelper.buildSAMLObject(SubjectConfirmationData.class);
        subjectConfirmationData.setNotOnOrAfter(DateTime.now().plusMinutes(25));
        subjectConfirmationData.setInResponseTo(randomString());
        subjectConfirmationData.setRecipient(randomString());
        subjectConfirmation.setMethod(SubjectConfirmation.METHOD_BEARER);
        subjectConfirmation.setSubjectConfirmationData(subjectConfirmationData);
        subject.getSubjectConfirmations().add(subjectConfirmation);
        assertion.setSubject(subject);
    }

    private void set_up_response() {
        response = samlHelper.buildSAMLObject(Response.class);
        response.setIssuer(samlHelper.buildSAMLObject(Issuer.class));
        Status responseStatus = samlHelper.buildSAMLObject(Status.class);
        responseStatus.setStatusCode(samlHelper.buildSAMLObject(StatusCode.class));
        response.setStatus(responseStatus);
        response.setIssueInstant(DateTime.now());
        response.setID(assertion.getID());
        response.getIssuer().setValue(assertion.getIssuer().getValue());
        response.getStatus().getStatusCode().setValue(randomString());
    }

    private void given_the_claims_are_cyphered_and_serialized() {
        try {
            EncryptionParameters encryptionParameters = new EncryptionParameters();
            encryptionParameters.setAlgorithm(EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES128);
            KeyEncryptionParameters keyEncryptionParameters = new KeyEncryptionParameters();
            keyEncryptionParameters.setEncryptionCredential(cypherCred);
            keyEncryptionParameters.setAlgorithm(EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSAOAEP);
            Encrypter encrypter = new Encrypter(encryptionParameters, keyEncryptionParameters);
            encrypter.setKeyPlacement(Encrypter.KeyPlacement.INLINE);
            EncryptedAssertion encryptedAssertion = encrypter.encrypt(assertion);
            response.getEncryptedAssertions().add(encryptedAssertion);

            Signature signature = samlHelper.buildSAMLObject(Signature.class);
            signature.setSigningCredential(cypherCred);
            signature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1);
            signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
            response.setSignature(signature);
            Configuration.getMarshallerFactory().getMarshaller(response).marshall(response);
            Signer.signObject(signature);

            serialized = encodeBytes(samlHelper.toString(response).getBytes());
        } catch (Exception e) {
            throw IDPSAMLErrorException.newInstance(e);
        }
    }

    private void when_decipher_the_token() {
        token = new SAMLAccessToken(serialized, decipherCred);
        token.decipher();
    }

}
