package com.mlyauth.token.saml;

import com.mlyauth.constants.TokenProcessingStatus;
import com.mlyauth.constants.TokenScope;
import com.mlyauth.constants.TokenVerdict;
import com.mlyauth.exception.IDPSAMLErrorException;
import com.mlyauth.exception.TokenUnmodifiableException;
import com.mlyauth.tools.KeysForTests;
import javafx.util.Pair;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
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
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureConstants;
import org.opensaml.xml.signature.Signer;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import static com.mlyauth.token.Claims.*;
import static com.mlyauth.tools.RandomForTests.randomString;
import static org.exparity.hamcrest.date.LocalDateTimeMatchers.within;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
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
        assertThat(token.getStatus(), equalTo(TokenProcessingStatus.CYPHERED));
    }


    @Test
    public void when_decipher_then_it_must_be_deciphered() {
        when_decipher_the_token();
        assertThat(token.getStatus(), equalTo(TokenProcessingStatus.DECIPHERED));
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

    @Test
    public void when_given_cyphered_token_then_the_bp_is_loaded() {
        when_decipher_the_token();
        assertThat(token.getBP(), equalTo(samlHelper.getAttributeValue(bp)));
    }

    @Test
    public void when_given_cyphered_token_then_the_issuer_is_loaded() {
        when_decipher_the_token();
        assertThat(token.getIssuer(), equalTo(response.getIssuer().getValue()));
    }

    @Test
    public void when_given_cyphered_token_then_the_state_is_loaded() {
        when_decipher_the_token();
        assertThat(token.getState(), equalTo(samlHelper.getAttributeValue(state)));
    }

    @Test
    public void when_given_cyphered_token_then_the_audience_is_loaded() {
        when_decipher_the_token();
        assertThat(token.getAudience(), equalTo(audience.getAudienceURI()));
    }

    @Test
    public void when_given_cyphered_token_then_the_target_url_is_loaded() {
        when_decipher_the_token();
        assertThat(token.getTargetURL(), equalTo(response.getDestination()));
    }

    @Test
    public void when_given_cyphered_token_then_the_delegator_is_loaded() {
        when_decipher_the_token();
        assertThat(token.getDelegator(), equalTo(samlHelper.getAttributeValue(delegator)));
    }

    @Test
    public void when_given_cyphered_token_then_the_delegate_is_loaded() {
        when_decipher_the_token();
        assertThat(token.getDelegate(), equalTo(samlHelper.getAttributeValue(delegate)));
    }

    @Test
    public void when_given_cyphered_token_then_the_verdict_is_loaded() {
        when_decipher_the_token();
        assertThat(token.getVerdict(), equalTo(TokenVerdict.FAIL));
    }

    @Test
    public void when_given_cyphered_token_then_the_expiry_time_is_loaded() {
        when_decipher_the_token();
        final Date date = assertion.getConditions().getNotOnOrAfter().toDateTime(DateTimeZone.getDefault()).toDate();
        final LocalDateTime expected = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        assertThat(token.getExpiryTime(), notNullValue());
        assertThat(token.getExpiryTime(), within(0, ChronoUnit.SECONDS, expected));
    }

    @Test
    public void when_given_cyphered_token_then_the_effective_time_is_loaded() {
        when_decipher_the_token();
        final Date date = assertion.getAuthnStatements().get(0).getAuthnInstant().toDateTime(DateTimeZone.getDefault()).toDate();
        final LocalDateTime expected = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        assertThat(token.getEffectiveTime(), notNullValue());
        assertThat(token.getEffectiveTime(), within(0, ChronoUnit.SECONDS, expected));
    }

    @Test
    public void when_given_cyphered_token_then_the_issuance_time_is_loaded() {
        when_decipher_the_token();
        final Date date = response.getIssueInstant().toDateTime(DateTimeZone.getDefault()).toDate();
        final LocalDateTime expected = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        assertThat(token.getIssuanceTime(), notNullValue());
        assertThat(token.getIssuanceTime(), within(0, ChronoUnit.SECONDS, expected));
    }

    @Test(expected = IDPSAMLErrorException.class)
    public void when_the_decryption_key_does_not_match_then_error() {
        final Pair<PrivateKey, X509Certificate> rsaCred = KeysForTests.generateRSACredential();
        BasicX509Credential credential = (BasicX509Credential) decipherCred;
        credential.setPrivateKey(rsaCred.getKey());
        token = new SAMLAccessToken(serialized, credential);
        token.decipher();
    }

    @Test(expected = IDPSAMLErrorException.class)
    public void when_the_signature_key_does_not_match_then_error() {
        final Pair<PrivateKey, X509Certificate> rsaCred = KeysForTests.generateRSACredential();
        BasicX509Credential credential = (BasicX509Credential) decipherCred;
        credential.setEntityCertificate(rsaCred.getValue());
        token = new SAMLAccessToken(serialized, credential);
        token.decipher();
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_the_cyphered_token_is_null_then_error() {
        new SAMLAccessToken(null, decipherCred);
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_the_credential_is_null_then_error() {
        new SAMLAccessToken(serialized, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_the_private_key_is_null_then_error() {
        BasicX509Credential credential = (BasicX509Credential) decipherCred;
        credential.setPrivateKey(null);
        new SAMLAccessToken(serialized, decipherCred);
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_the_certificate_is_null_then_error() {
        BasicX509Credential credential = (BasicX509Credential) decipherCred;
        credential.setEntityCertificate(null);
        new SAMLAccessToken(serialized, decipherCred);
    }

    @Test(expected = IDPSAMLErrorException.class)
    public void when_the_cyphered_token_is_not_well_formatted_then_error() {
        new SAMLAccessToken(randomString(), decipherCred);
    }

    @Test(expected = IDPSAMLErrorException.class)
    public void when_the_token_is_not_signed_then_error() {
        given_the_claims_are_encrypted_and_not_signed_and_serialized();
        token = new SAMLAccessToken(serialized, decipherCred);
        token.decipher();
    }

    @Test(expected = IDPSAMLErrorException.class)
    public void when_the_token_is_signed_but_not_encrypted_then_error() {
        given_the_claims_are_not_encrypted_but_signed_and_serialized();
        token = new SAMLAccessToken(serialized, decipherCred);
        token.decipher();
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_stamp_is_not_modifiable_before_decipher() {
        token = new SAMLAccessToken(serialized, decipherCred);
        token.setStamp(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_stamp_is_not_modifiable_after_decipher() {
        when_decipher_the_token();
        token.setStamp(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_subject_is_not_modifiable_before_decipher() {
        token = new SAMLAccessToken(serialized, decipherCred);
        token.setSubject(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_subject_is_not_modifiable_after_decipher() {
        when_decipher_the_token();
        token.setSubject(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_scopes_are_not_modifiable_before_decipher() {
        token = new SAMLAccessToken(serialized, decipherCred);
        token.setScopes(new HashSet<>(Arrays.asList(TokenScope.values())));
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_scopes_are_not_modifiable_after_decipher() {
        when_decipher_the_token();
        token.setScopes(new HashSet<>(Arrays.asList(TokenScope.values())));
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_bp_is_not_modifiable_before_decipher() {
        token = new SAMLAccessToken(serialized, decipherCred);
        token.setBP(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_bp_is_not_modifiable_after_decipher() {
        when_decipher_the_token();
        token.setBP(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_state_is_not_modifiable_before_decipher() {
        token = new SAMLAccessToken(serialized, decipherCred);
        token.setState(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_state_is_not_modifiable_after_decipher() {
        when_decipher_the_token();
        token.setState(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_issuer_is_not_modifiable_before_decipher() {
        token = new SAMLAccessToken(serialized, decipherCred);
        token.setIssuer(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_issuer_is_not_modifiable_after_decipher() {
        when_decipher_the_token();
        token.setIssuer(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_audience_is_not_modifiable_before_decipher() {
        token = new SAMLAccessToken(serialized, decipherCred);
        token.setAudience(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_audience_is_not_modifiable_after_decipher() {
        when_decipher_the_token();
        token.setAudience(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_target_url_is_not_modifiable_before_decipher() {
        token = new SAMLAccessToken(serialized, decipherCred);
        token.setTargetURL(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_target_url_is_not_modifiable_after_decipher() {
        when_decipher_the_token();
        token.setTargetURL(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_delegator_is_not_modifiable_before_decipher() {
        token = new SAMLAccessToken(serialized, decipherCred);
        token.setDelegator(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_delegator_is_not_modifiable_after_decipher() {
        when_decipher_the_token();
        token.setDelegator(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_delegate_is_not_modifiable_before_decipher() {
        token = new SAMLAccessToken(serialized, decipherCred);
        token.setDelegate(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_delegate_is_not_modifiable_after_decipher() {
        when_decipher_the_token();
        token.setDelegate(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_verdict_is_not_modifiable_before_decipher() {
        token = new SAMLAccessToken(serialized, decipherCred);
        token.setVerdict(TokenVerdict.SUCCESS);
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void the_verdict_is_not_modifiable_after_decipher() {
        when_decipher_the_token();
        token.setVerdict(TokenVerdict.SUCCESS);
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
        response.setDestination(randomString());
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

    private void given_the_claims_are_encrypted_and_not_signed_and_serialized() {
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

            serialized = encodeBytes(samlHelper.toString(response).getBytes());
        } catch (Exception e) {
            throw IDPSAMLErrorException.newInstance(e);
        }
    }

    private void given_the_claims_are_not_encrypted_but_signed_and_serialized() {
        try {
            response.getEncryptedAssertions().clear();
            response.getAssertions().add(assertion);
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
