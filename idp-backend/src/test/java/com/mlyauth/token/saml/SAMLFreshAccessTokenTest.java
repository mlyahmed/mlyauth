package com.mlyauth.token.saml;

import com.mlyauth.constants.*;
import com.mlyauth.exception.TokenNotCipheredException;
import com.mlyauth.exception.TokenUnmodifiableException;
import com.mlyauth.tools.KeysForTests;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import javafx.util.Pair;
import org.apache.commons.lang.RandomStringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.*;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.security.credential.BasicCredential;
import org.opensaml.xml.security.credential.Credential;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import static com.mlyauth.constants.TokenScope.*;
import static com.mlyauth.constants.TokenStatus.CYPHERED;
import static com.mlyauth.token.IDPClaims.*;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.opensaml.saml2.core.NameIDType.TRANSIENT;
import static org.opensaml.saml2.core.SubjectConfirmation.METHOD_BEARER;

@RunWith(DataProviderRunner.class)
public class SAMLFreshAccessTokenTest {

    private SAMLAccessToken token;
    private SAMLHelper samlHelper;
    private Credential cypherCred;
    private Credential decipherCred;

    @Before
    public void setup() throws ConfigurationException {
        DefaultBootstrap.bootstrap();
        samlHelper = new SAMLHelper();
        final Pair<PrivateKey, X509Certificate> pair1 = KeysForTests.generateRSACredential();
        final Pair<PrivateKey, X509Certificate> pair2 = KeysForTests.generateRSACredential();
        cypherCred = samlHelper.toCredential(pair1.getKey(), pair2.getValue());
        decipherCred = samlHelper.toCredential(pair2.getKey(), pair1.getValue());
        token = new SAMLAccessToken(cypherCred);
        ReflectionTestUtils.setField(token, "samlHelper", samlHelper);
    }


    @Test(expected = IllegalArgumentException.class)
    public void when_credential_is_null_then_error() {
        new SAMLAccessToken(null);
    }


    @Test(expected = IllegalArgumentException.class)
    public void when_credential_private_key_is_null_then_error() {
        final BasicCredential credential = new BasicCredential();
        credential.setPublicKey(KeysForTests.generateRSACredential().getValue().getPublicKey());
        credential.setPrivateKey(null);
        new SAMLAccessToken(credential);
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_credential_public_key_is_null_then_error() {
        final BasicCredential credential = new BasicCredential();
        credential.setPrivateKey(KeysForTests.generateRSACredential().getKey());
        credential.setPublicKey(null);
        new SAMLAccessToken(credential);
    }

    @Test
    public void when_create_fresh_response_then_token_claims_must_be_fresh() {
        assertThat(token.getStamp(), nullValue());
        assertThat(token.getSubject(), nullValue());
        assertThat(token.getScopes(), empty());
        assertThat(token.getBP(), nullValue());
        assertThat(token.getState(), nullValue());
        assertThat(token.getIssuer(), nullValue());
        assertThat(token.getAudience(), nullValue());
        assertThat(token.getDelegator(), nullValue());
        assertThat(token.getDelegate(), nullValue());
        assertThat(token.getVerdict(), nullValue());
        assertThat(token.getNorm(), equalTo(TokenNorm.SAML));
        assertThat(token.getType(), equalTo(TokenType.ACCESS));
        assertThat(token.getStatus(), equalTo(TokenStatus.FRESH));
    }

    @Test
    public void when_create_a_fresh_token_and_set_stamp_then_must_be_set() {
        String id = randomString();
        token.setStamp(id);
        assertThat(token.getStamp(), equalTo(id));
        assertThat(token.getStatus(), equalTo(TokenStatus.FORGED));
    }

    @Test
    public void when_serialize_cyphered_token_then_the_stamp_must_be_committed() {
        final String id = randomString();
        token.setStamp(id);
        when_cypher_the_token();
        Response response = (Response) samlHelper.decode(when_serialize_the_token());
        Assertion assertion = samlHelper.decryptAssertion(response.getEncryptedAssertions().get(0), decipherCred);
        assertThat(response.getID(), equalTo(id));
        assertThat(assertion.getID(), equalTo(id));
    }

    @DataProvider
    public static String[] subjects() {
        // @formatter:off
        return new String[]{
                "BA0000000000855",
                "BA0000000000856",
                "125485"
        };
        // @formatter:on
    }

    @Test
    @UseDataProvider("subjects")
    public void when_create_a_fresh_token_and_set_subject_then_it_must_be_set(String subject) {
        token.setSubject(subject);
        assertThat(token.getSubject(), equalTo(subject));
        assertThat(token.getStatus(), equalTo(TokenStatus.FORGED));
    }

    @Test
    @UseDataProvider("subjects")
    public void when_serialize_cyphered_token_then_the_subject_must_be_committed(String subject) {
        token.setSubject(subject);
        when_cypher_the_token();
        Response response = (Response) samlHelper.decode(when_serialize_the_token());
        Assertion assertion = samlHelper.decryptAssertion(response.getEncryptedAssertions().get(0), decipherCred);
        assertThat(assertion.getSubject(), notNullValue());
        assertThat(assertion.getSubject().getNameID().getValue(), equalTo(subject));
        assertThat(assertion.getSubject().getNameID().getFormat(), equalTo(TRANSIENT));
        assertThat(assertion.getSubject().getSubjectConfirmations(), hasSize(1));
        assertThat(assertion.getSubject().getSubjectConfirmations().get(0).getMethod(), equalTo(METHOD_BEARER));
    }

    @DataProvider
    public static Object[][] scopes() {
        // @formatter:off
        return new Object[][]{
                {PERSON.name(), POLICY.name(), CLAIM.name()},
                {PROPOSAL.name(), POLICY.name(), PERSON.name()},
                {POLICY.name(), CLAIM.name(), PERSON.name(), PROPOSAL.name()},
                {POLICY.name()},
                {CLAIM.name()},
        };
        // @formatter:on
    }

    @Test
    @UseDataProvider("scopes")
    public void when_create_a_fresh_token_and_set_scopes_then_they_must_be_set(String... scopesArrays) {
        final Set<TokenScope> scopes = Arrays.stream(scopesArrays).map(TokenScope::valueOf).collect(toSet());
        token.setScopes(scopes);
        assertThat(token.getScopes(), equalTo(scopes));
        assertThat(token.getStatus(), equalTo(TokenStatus.FORGED));
    }

    @Test
    @UseDataProvider("scopes")
    public void when_serialize_cyphered_token_then_the_scopes_must_be_committed(String... scopesArray) {
        final Set<TokenScope> scopes = Arrays.stream(scopesArray).map(TokenScope::valueOf).collect(toSet());
        token.setScopes(scopes);
        when_cypher_the_token();
        Response response = (Response) samlHelper.decode(when_serialize_the_token());
        Assertion assertion = samlHelper.decryptAssertion(response.getEncryptedAssertions().get(0), decipherCred);
        assertThat(getAttributeValue(assertion, SCOPES.getValue()), equalTo(compact(scopes)));
    }

    private String compact(Set<TokenScope> scopesSet) {
        return scopesSet.stream().map(TokenScope::name).collect(Collectors.joining("|"));
    }

    @Test
    public void when_create_a_fresh_token_and_set_BP_then_it_must_be_set() {
        final String bp = randomString();
        token.setBP(bp);
        assertThat(token.getBP(), equalTo(bp));
        assertThat(token.getStatus(), equalTo(TokenStatus.FORGED));
    }

    @Test
    public void when_serialize_cyphered_token_then_the_BP_must_be_committed() {
        final String bp = randomString();
        token.setBP(bp);
        when_cypher_the_token();
        Response response = (Response) samlHelper.decode(when_serialize_the_token());
        Assertion assertion = samlHelper.decryptAssertion(response.getEncryptedAssertions().get(0), decipherCred);
        assertThat(getAttributeValue(assertion, BP.getValue()), equalTo(bp));
    }

    @Test
    public void when_create_a_fresh_token_and_set_state_then_it_must_be_set() {
        final String state = randomString();
        token.setState(state);
        assertThat(token.getState(), equalTo(state));
        assertThat(token.getStatus(), equalTo(TokenStatus.FORGED));
    }

    @Test
    public void when_serialize_cyphered_token_then_the_state_must_be_committed() {
        final String state = randomString();
        token.setState(state);
        when_cypher_the_token();
        Response response = (Response) samlHelper.decode(when_serialize_the_token());
        Assertion assertion = samlHelper.decryptAssertion(response.getEncryptedAssertions().get(0), decipherCred);
        assertThat(getAttributeValue(assertion, STATE.getValue()), equalTo(state));
    }

    @Test
    public void when_create_a_fresh_token_and_set_issuer_then_it_must_be_set() {
        final String issuerURI = randomString();
        token.setIssuer(issuerURI);
        assertThat(token.getIssuer(), equalTo(issuerURI));
        assertThat(token.getStatus(), equalTo(TokenStatus.FORGED));
    }

    @Test
    public void when_serialize_cyphered_token_then_the_issuer_must_be_committed() {
        final String issuerURI = randomString();
        token.setIssuer(issuerURI);
        when_cypher_the_token();
        Response response = (Response) samlHelper.decode(when_serialize_the_token());
        Assertion assertion = samlHelper.decryptAssertion(response.getEncryptedAssertions().get(0), decipherCred);
        assertThat(response.getIssuer(), notNullValue());
        assertThat(response.getIssuer().getValue(), equalTo(issuerURI));
        assertThat(assertion.getIssuer(), notNullValue());
        assertThat(assertion.getIssuer().getValue(), equalTo(issuerURI));
    }

    @Test
    public void when_create_a_fresh_token_and_set_audience_then_it_must_be_set() {
        final String audience = randomString();
        token.setAudience(audience);
        assertThat(token.getAudience(), equalTo(audience));
        assertThat(token.getStatus(), equalTo(TokenStatus.FORGED));
    }

    @Test
    public void when_serialize_cyphered_token_then_the_audience_must_be_committed() {
        final String audience = randomString();
        token.setAudience(audience);
        when_cypher_the_token();
        Response response = (Response) samlHelper.decode(when_serialize_the_token());
        Assertion assertion = samlHelper.decryptAssertion(response.getEncryptedAssertions().get(0), decipherCred);
        assertThat(assertion.getConditions(), notNullValue());
        assertThat(assertion.getConditions().getAudienceRestrictions().get(0).getAudiences().get(0)
                .getAudienceURI(), equalTo(audience));
        assertThat(assertion.getSubject().getSubjectConfirmations().get(0).getSubjectConfirmationData()
                .getInResponseTo(), equalTo(audience));
    }

    @Test
    public void when_create_a_fresh_token_and_set_target_URL_then_it_must_be_set() {
        final String url = randomString();
        token.setTargetURL(url);
        assertThat(token.getTargetURL(), equalTo(url));
        assertThat(token.getStatus(), equalTo(TokenStatus.FORGED));
    }

    @Test
    public void when_serialize_cyphered_token_then_the_target_URL_must_be_committed() {
        final String url = randomString();
        token.setTargetURL(url);
        when_cypher_the_token();
        Response response = (Response) samlHelper.decode(when_serialize_the_token());
        Assertion assertion = samlHelper.decryptAssertion(response.getEncryptedAssertions().get(0), decipherCred);
        assertThat(response.getDestination(), equalTo(url));
        assertThat(assertion.getSubject().getSubjectConfirmations().get(0).getSubjectConfirmationData()
                .getRecipient(), equalTo(url));
    }

    @Test
    public void when_create_a_fresh_token_and_set_delegator_then_it_must_be_set() {
        final String delegator = randomString();
        token.setDelegator(delegator);
        assertThat(token.getDelegator(), equalTo(delegator));
        assertThat(token.getStatus(), equalTo(TokenStatus.FORGED));
    }

    @Test
    public void when_serialize_cyphered_token_then_the_delegator_must_be_committed() {
        final String delegator = randomString();
        token.setDelegator(delegator);
        when_cypher_the_token();
        Response response = (Response) samlHelper.decode(when_serialize_the_token());
        Assertion assertion = samlHelper.decryptAssertion(response.getEncryptedAssertions().get(0), decipherCred);
        assertThat(getAttributeValue(assertion, DELEGATOR.getValue()), equalTo(delegator));
    }

    @Test
    public void when_create_a_fresh_token_and_set_delegate_then_it_must_be_set() {
        final String delegateURI = randomString();
        token.setDelegate(delegateURI);
        assertThat(token.getDelegate(), equalTo(delegateURI));
        assertThat(token.getStatus(), equalTo(TokenStatus.FORGED));
    }

    @Test
    public void when_serialize_cyphered_token_then_the_delegate_must_be_committed() {
        final String delegateURI = randomString();
        token.setDelegate(delegateURI);
        when_cypher_the_token();
        Response response = (Response) samlHelper.decode(when_serialize_the_token());
        Assertion assertion = samlHelper.decryptAssertion(response.getEncryptedAssertions().get(0), decipherCred);
        assertThat(getAttributeValue(assertion, DELEGATE.getValue()), equalTo(delegateURI));
    }

    @Test
    public void when_create_fresh_token_and_set_success_verdict_then_it_must_be_set() {
        token.setVerdict(TokenVerdict.SUCCESS);
        assertThat(token.getVerdict(), equalTo(TokenVerdict.SUCCESS));
        assertThat(token.getStatus(), equalTo(TokenStatus.FORGED));
    }

    @Test
    public void when_serialize_cyphered_token_then_the_success_verdict_must_be_committed() {
        token.setVerdict(TokenVerdict.SUCCESS);
        when_cypher_the_token();
        Response response = (Response) samlHelper.decode(when_serialize_the_token());
        assertThat(response.getStatus().getStatusCode().getValue(), equalTo(StatusCode.SUCCESS_URI));
    }

    @Test
    public void when_create_fresh_token_and_set_fail_verdict_then_it_must_be_set() {
        token.setVerdict(TokenVerdict.FAIL);
        assertThat(token.getVerdict(), equalTo(TokenVerdict.FAIL));
        assertThat(token.getStatus(), equalTo(TokenStatus.FORGED));
    }

    @Test
    public void when_serialize_cyphered_token_then_the_fail_verdict_must_be_committed() {
        token.setVerdict(TokenVerdict.FAIL);
        when_cypher_the_token();
        Response response = (Response) samlHelper.decode(when_serialize_the_token());
        assertThat(response.getStatus().getStatusCode().getValue(), equalTo(StatusCode.AUTHN_FAILED_URI));
    }

    @Test
    public void when_create_a_fresh_token_then_it_expires_in_3_minutes() {
        assertThat(token.getExpiryTime(), notNullValue());
        assertThat(token.getExpiryTime().isBefore(LocalDateTime.now().plusMinutes(3)), equalTo(true));
    }

    @Test
    public void when_serialize_cyphered_token_then_the_expiry_time_must_be_committed() {
        when_cypher_the_token();
        Response response = (Response) samlHelper.decode(when_serialize_the_token());
        Assertion assertion = samlHelper.decryptAssertion(response.getEncryptedAssertions().get(0), decipherCred);
        assertThat(assertion.getSubject().getSubjectConfirmations().get(0).getSubjectConfirmationData()
                .getNotOnOrAfter(), notNullValue());
        assertThat(assertion.getSubject().getSubjectConfirmations().get(0).getSubjectConfirmationData()
                .getNotOnOrAfter().toDateTime(DateTimeZone.getDefault())
                .isBefore(DateTime.now().plusMinutes(3)), equalTo(true));
        assertThat(assertion.getConditions().getNotOnOrAfter(), notNullValue());
        assertThat(assertion.getConditions().getNotOnOrAfter().toDateTime(DateTimeZone.getDefault())
                .isBefore(DateTime.now().plusMinutes(3)), equalTo(true));
    }

    @Test
    public void when_create_a_fresh_token_then_it_is_effective_now() {
        assertThat(token.getEffectiveTime(), notNullValue());
        assertThat(token.getEffectiveTime().isAfter(LocalDateTime.now().minusSeconds(1)), equalTo(true));
    }

    @Test
    public void when_serialize_cyphered_token_then_the_effective_time_must_be_committed() {
        when_cypher_the_token();
        Response response = (Response) samlHelper.decode(when_serialize_the_token());
        Assertion assertion = samlHelper.decryptAssertion(response.getEncryptedAssertions().get(0), decipherCred);
        assertThat(assertion.getAuthnStatements().get(0).getAuthnInstant(), notNullValue());
        assertThat(assertion.getAuthnStatements().get(0).getAuthnInstant().toDateTime(DateTimeZone.getDefault())
                .isAfter((new DateTime()).minusSeconds(1)), equalTo(true));
    }

    @Test
    public void when_serialize_cyphered_token_then_the_auth_context_is_set() {
        given_forged_token();
        when_cypher_the_token();
        Response response = (Response) samlHelper.decode(when_serialize_the_token());
        Assertion assertion = samlHelper.decryptAssertion(response.getEncryptedAssertions().get(0), decipherCred);
        assertThat(assertion.getAuthnStatements().get(0).getAuthnContext()
                .getAuthnContextClassRef(), notNullValue());
        ;
        assertThat(assertion.getAuthnStatements().get(0).getAuthnContext().getAuthnContextClassRef()
                .getAuthnContextClassRef(), equalTo(AuthnContext.PASSWORD_AUTHN_CTX));
    }

    @Test
    public void when_create_a_fresh_token_it_issued_now() {
        assertThat(token.getIssuanceTime(), notNullValue());
        assertThat(token.getIssuanceTime().isAfter(LocalDateTime.now().minusSeconds(1)), equalTo(true));
    }

    @Test
    public void when_serialize_cyphered_token_then_the_issuance_time_must_be_committed() {
        when_cypher_the_token();
        Response response = (Response) samlHelper.decode(when_serialize_the_token());
        Assertion assertion = samlHelper.decryptAssertion(response.getEncryptedAssertions().get(0), decipherCred);
        assertThat(response.getIssueInstant(), notNullValue());
        assertThat(response.getIssueInstant().toDateTime(DateTimeZone.getDefault())
                .isAfter((new DateTime()).minusSeconds(1)), equalTo(true));
        assertThat(assertion.getIssueInstant(), notNullValue());
        assertThat(assertion.getIssueInstant().toDateTime(DateTimeZone.getDefault())
                .isAfter(DateTime.now().minusSeconds(1)), equalTo(true));
    }

    @DataProvider
    public static Object[][] claims() {
        // @formatter:off
        return new String[][]{
                {"prestationId", "BA000000215487"},
                {"action", "S"},
                {"codeRole", "CL"},
                {"referenceRole", "215487548754"},
        };
        // @formatter:on
    }

    @Test
    @UseDataProvider("claims")
    public void when_set_other_claim_then_it_must_be_set(String... claimPair) {
        token.setClaim(claimPair[0], claimPair[1]);
        assertThat(token.getClaim(claimPair[0]), equalTo(claimPair[1]));
    }

    @Test
    @UseDataProvider("claims")
    public void when_serialize_cyphered_token_then_the_other_claims_must_be_committed(String... claimPair) {
        token.setClaim(claimPair[0], claimPair[1]);
        when_cypher_the_token();
        Response response = (Response) samlHelper.decode(when_serialize_the_token());
        Assertion assertion = samlHelper.decryptAssertion(response.getEncryptedAssertions().get(0), decipherCred);
        assertThat(getAttributeValue(assertion, claimPair[0]), equalTo(claimPair[1]));
    }

    @Test
    public void when_create_a_fresh_token_and_cypher_it_then_it_must_be_set_as_cyphered() {
        given_forged_token();
        when_cypher_the_token();
        assertThat(token.getStatus(), equalTo(CYPHERED));
    }

    @Test
    public void when_serialize_a_cyphered_token_then_return_encoded_response() {
        given_forged_token();
        when_cypher_the_token();
        assertThat(when_serialize_the_token(), notNullValue());
        assertThat(samlHelper.decode(when_serialize_the_token()), instanceOf(Response.class));
    }

    @Test
    public void when_serialize_a_cyphered_token_then_return_signed_response() {
        given_forged_token();
        when_cypher_the_token();
        Response response = (Response) samlHelper.decode(when_serialize_the_token());
        samlHelper.validateSignature(response, decipherCred);
    }

    @Test
    public void when_serialize_a_cyphered_token_then_return_an_encrypted_assertion() {
        given_forged_token();
        when_cypher_the_token();
        final String serialized = when_serialize_the_token();
        Response response = (Response) samlHelper.decode(serialized);
        assertThat(response.getEncryptedAssertions(), hasSize(1));
        assertThat(samlHelper.decryptAssertion(response.getEncryptedAssertions().get(0), decipherCred), notNullValue());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void when_create_a_fresh_token_and_cypher_then_we_cannot_decipher_it() {
        given_forged_token();
        when_cypher_the_token();
        when_decipher_the_token();
    }

    @Test(expected = TokenNotCipheredException.class)
    public void when_serialize_a_not_ciphered_token_then_error() {
        given_forged_token();
        when_serialize_the_token();
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void when_set_stamp_and_already_ciphered_then_error() {
        given_forged_token();
        when_cypher_the_token();
        token.setStamp(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void when_set_subject_and_already_ciphered_then_error() {
        given_forged_token();
        when_cypher_the_token();
        token.setSubject(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void when_set_scopes_and_already_ciphered_then_error() {
        given_forged_token();
        when_cypher_the_token();
        token.setScopes(new HashSet<>(Arrays.asList(PERSON)));
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void when_set_BP_and_already_ciphered_then_error() {
        given_forged_token();
        when_cypher_the_token();
        token.setBP(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void when_set_state_and_already_ciphered_then_error() {
        given_forged_token();
        when_cypher_the_token();
        token.setState(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void when_set_issuer_and_already_ciphered_then_error() {
        given_forged_token();
        when_cypher_the_token();
        token.setIssuer(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void when_set_audience_and_already_ciphered_then_error() {
        given_forged_token();
        when_cypher_the_token();
        token.setAudience(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void when_set_target_URL_and_already_ciphered_then_error() {
        given_forged_token();
        when_cypher_the_token();
        token.setTargetURL(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void when_set_delegator_and_already_ciphered_then_error() {
        given_forged_token();
        when_cypher_the_token();
        token.setDelegator(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void when_set_delegate_and_already_ciphered_then_error() {
        given_forged_token();
        when_cypher_the_token();
        token.setDelegate(randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void when_set_verdict_and_already_ciphered_then_error() {
        given_forged_token();
        when_cypher_the_token();
        token.setVerdict(TokenVerdict.FAIL);
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void when_set_claim_and_already_ciphered_then_error() {
        given_forged_token();
        when_cypher_the_token();
        token.setClaim(randomString(), randomString());
    }

    private void given_forged_token() {
        token.setStamp(randomString());
        token.setVerdict(TokenVerdict.SUCCESS);
        token.setDelegate(randomString());
        token.setDelegator(randomString());
        token.setAudience(randomString());
        token.setIssuer(randomString());
        token.setState(randomString());
        token.setScopes(new HashSet<>(Arrays.asList(PERSON, POLICY)));
        token.setBP(randomString());
        token.setSubject(randomString());
        token.setTargetURL(randomString());
    }

    private void when_cypher_the_token() {
        token.cypher();
    }

    private String when_serialize_the_token() {
        return token.serialize();
    }

    private void when_decipher_the_token() {
        token.decipher();
    }

    private String getAttributeValue(Assertion assertion, String attributeName) {
        final Attribute actual = assertion.getAttributeStatements().get(0).getAttributes().stream().filter(attr -> attributeName.equals(attr.getName())).findFirst().get();
        return ((XSString) actual.getAttributeValues().get(0)).getValue();
    }

    private static String randomString() {
        final int length = (new Random()).nextInt(30);
        return RandomStringUtils.random(length > 0 ? length : 20, true, true);
    }
}
