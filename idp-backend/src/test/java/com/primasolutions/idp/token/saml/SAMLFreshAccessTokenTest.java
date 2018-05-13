package com.primasolutions.idp.token.saml;

import com.primasolutions.idp.constants.TokenNorm;
import com.primasolutions.idp.constants.TokenProcessingStatus;
import com.primasolutions.idp.constants.TokenScope;
import com.primasolutions.idp.constants.TokenType;
import com.primasolutions.idp.constants.TokenVerdict;
import com.primasolutions.idp.exception.TokenNotCipheredException;
import com.primasolutions.idp.exception.TokenUnmodifiableException;
import com.primasolutions.idp.tools.KeysForTests;
import com.primasolutions.idp.tools.RandomForTests;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import javafx.util.Pair;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AuthnContext;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.StatusCode;
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
import java.util.Set;
import java.util.stream.Collectors;

import static com.primasolutions.idp.constants.TokenProcessingStatus.CYPHERED;
import static com.primasolutions.idp.constants.TokenScope.CLAIM;
import static com.primasolutions.idp.constants.TokenScope.PERSON;
import static com.primasolutions.idp.constants.TokenScope.POLICY;
import static com.primasolutions.idp.constants.TokenScope.PROPOSAL;
import static com.primasolutions.idp.token.Claims.BP;
import static com.primasolutions.idp.token.Claims.DELEGATE;
import static com.primasolutions.idp.token.Claims.DELEGATOR;
import static com.primasolutions.idp.token.Claims.SCOPES;
import static com.primasolutions.idp.token.Claims.STATE;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.opensaml.saml2.core.NameIDType.TRANSIENT;
import static org.opensaml.saml2.core.SubjectConfirmation.METHOD_BEARER;

@RunWith(DataProviderRunner.class)
public class SAMLFreshAccessTokenTest {

    public static final int THREE_MINUTES = 3;
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
        assertThat(token.getStatus(), equalTo(TokenProcessingStatus.FRESH));
    }

    @Test
    public void when_create_a_fresh_token_and_set_stamp_then_must_be_set() {
        String id = RandomForTests.randomString();
        token.setStamp(id);
        assertThat(token.getStamp(), equalTo(id));
        assertThat(token.getStatus(), equalTo(TokenProcessingStatus.FORGED));
    }

    @Test
    public void when_serialize_cyphered_token_then_the_stamp_must_be_committed() {
        final String id = RandomForTests.randomString();
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
    public void when_create_a_fresh_token_and_set_subject_then_it_must_be_set(final String subject) {
        token.setSubject(subject);
        assertThat(token.getSubject(), equalTo(subject));
        assertThat(token.getStatus(), equalTo(TokenProcessingStatus.FORGED));
    }

    @Test
    @UseDataProvider("subjects")
    public void when_serialize_cyphered_token_then_the_subject_must_be_committed(final String subject) {
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
    public void when_create_a_fresh_token_and_set_scopes_then_they_must_be_set(final String... scopesArrays) {
        final Set<TokenScope> scopes = Arrays.stream(scopesArrays).map(TokenScope::valueOf).collect(toSet());
        token.setScopes(scopes);
        assertThat(token.getScopes(), equalTo(scopes));
        assertThat(token.getStatus(), equalTo(TokenProcessingStatus.FORGED));
    }

    @Test
    @UseDataProvider("scopes")
    public void when_serialize_cyphered_token_then_the_scopes_must_be_committed(final String... scopesArray) {
        final Set<TokenScope> scopes = Arrays.stream(scopesArray).map(TokenScope::valueOf).collect(toSet());
        token.setScopes(scopes);
        when_cypher_the_token();
        Response response = (Response) samlHelper.decode(when_serialize_the_token());
        Assertion assertion = samlHelper.decryptAssertion(response.getEncryptedAssertions().get(0), decipherCred);
        assertThat(getAttributeValue(assertion, SCOPES.getValue()), equalTo(compact(scopes)));
    }

    private String compact(final Set<TokenScope> scopesSet) {
        return scopesSet.stream().map(TokenScope::name).collect(Collectors.joining("|"));
    }

    @Test
    public void when_create_a_fresh_token_and_set_BP_then_it_must_be_set() {
        final String bp = RandomForTests.randomString();
        token.setBP(bp);
        assertThat(token.getBP(), equalTo(bp));
        assertThat(token.getStatus(), equalTo(TokenProcessingStatus.FORGED));
    }

    @Test
    public void when_serialize_cyphered_token_then_the_BP_must_be_committed() {
        final String bp = RandomForTests.randomString();
        token.setBP(bp);
        when_cypher_the_token();
        Response response = (Response) samlHelper.decode(when_serialize_the_token());
        Assertion assertion = samlHelper.decryptAssertion(response.getEncryptedAssertions().get(0), decipherCred);
        assertThat(getAttributeValue(assertion, BP.getValue()), equalTo(bp));
    }

    @Test
    public void when_create_a_fresh_token_and_set_state_then_it_must_be_set() {
        final String state = RandomForTests.randomString();
        token.setState(state);
        assertThat(token.getState(), equalTo(state));
        assertThat(token.getStatus(), equalTo(TokenProcessingStatus.FORGED));
    }

    @Test
    public void when_serialize_cyphered_token_then_the_state_must_be_committed() {
        final String state = RandomForTests.randomString();
        token.setState(state);
        when_cypher_the_token();
        Response response = (Response) samlHelper.decode(when_serialize_the_token());
        Assertion assertion = samlHelper.decryptAssertion(response.getEncryptedAssertions().get(0), decipherCred);
        assertThat(getAttributeValue(assertion, STATE.getValue()), equalTo(state));
    }

    @Test
    public void when_create_a_fresh_token_and_set_issuer_then_it_must_be_set() {
        final String issuerURI = RandomForTests.randomString();
        token.setIssuer(issuerURI);
        assertThat(token.getIssuer(), equalTo(issuerURI));
        assertThat(token.getStatus(), equalTo(TokenProcessingStatus.FORGED));
    }

    @Test
    public void when_serialize_cyphered_token_then_the_issuer_must_be_committed() {
        final String issuerURI = RandomForTests.randomString();
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
        final String audience = RandomForTests.randomString();
        token.setAudience(audience);
        assertThat(token.getAudience(), equalTo(audience));
        assertThat(token.getStatus(), equalTo(TokenProcessingStatus.FORGED));
    }

    @Test
    public void when_serialize_cyphered_token_then_the_audience_must_be_committed() {
        final String audience = RandomForTests.randomString();
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
        final String url = RandomForTests.randomString();
        token.setTargetURL(url);
        assertThat(token.getTargetURL(), equalTo(url));
        assertThat(token.getStatus(), equalTo(TokenProcessingStatus.FORGED));
    }

    @Test
    public void when_serialize_cyphered_token_then_the_target_URL_must_be_committed() {
        final String url = RandomForTests.randomString();
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
        final String delegator = RandomForTests.randomString();
        token.setDelegator(delegator);
        assertThat(token.getDelegator(), equalTo(delegator));
        assertThat(token.getStatus(), equalTo(TokenProcessingStatus.FORGED));
    }

    @Test
    public void when_serialize_cyphered_token_then_the_delegator_must_be_committed() {
        final String delegator = RandomForTests.randomString();
        token.setDelegator(delegator);
        when_cypher_the_token();
        Response response = (Response) samlHelper.decode(when_serialize_the_token());
        Assertion assertion = samlHelper.decryptAssertion(response.getEncryptedAssertions().get(0), decipherCred);
        assertThat(getAttributeValue(assertion, DELEGATOR.getValue()), equalTo(delegator));
    }

    @Test
    public void when_create_a_fresh_token_and_set_delegate_then_it_must_be_set() {
        final String delegateURI = RandomForTests.randomString();
        token.setDelegate(delegateURI);
        assertThat(token.getDelegate(), equalTo(delegateURI));
        assertThat(token.getStatus(), equalTo(TokenProcessingStatus.FORGED));
    }

    @Test
    public void when_serialize_cyphered_token_then_the_delegate_must_be_committed() {
        final String delegateURI = RandomForTests.randomString();
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
        assertThat(token.getStatus(), equalTo(TokenProcessingStatus.FORGED));
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
        assertThat(token.getStatus(), equalTo(TokenProcessingStatus.FORGED));
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
        assertThat(token.getExpiryTime().isBefore(LocalDateTime.now().plusMinutes(THREE_MINUTES)), equalTo(true));
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
                .isBefore(DateTime.now().plusMinutes(THREE_MINUTES)), equalTo(true));
        assertThat(assertion.getConditions().getNotOnOrAfter(), notNullValue());
        assertThat(assertion.getConditions().getNotOnOrAfter().toDateTime(DateTimeZone.getDefault())
                .isBefore(DateTime.now().plusMinutes(THREE_MINUTES)), equalTo(true));
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
    public void when_set_other_claim_then_it_must_be_set(final String... claimPair) {
        token.setClaim(claimPair[0], claimPair[1]);
        assertThat(token.getClaim(claimPair[0]), equalTo(claimPair[1]));
    }

    @Test
    @UseDataProvider("claims")
    public void when_serialize_cyphered_token_then_the_other_claims_must_be_committed(final String... claimPair) {
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

    @Test
    public void when_serialize_a_cyphered_token_many_times_then_return_the_same_value() {
        given_forged_token();
        when_cypher_the_token();
        final String first = when_serialize_the_token();
        final String second = when_serialize_the_token();
        assertThat(first, equalTo(second));
    }

    @Test
    public void when_serialize_each_time_after_cypher_then_return_different_value() {
        given_forged_token();
        when_cypher_the_token();
        final String first = when_serialize_the_token();
        when_cypher_the_token();
        final String second = when_serialize_the_token();
        assertThat(first, not(equalTo(second)));
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
        token.setStamp(RandomForTests.randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void when_set_subject_and_already_ciphered_then_error() {
        given_forged_token();
        when_cypher_the_token();
        token.setSubject(RandomForTests.randomString());
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
        token.setBP(RandomForTests.randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void when_set_state_and_already_ciphered_then_error() {
        given_forged_token();
        when_cypher_the_token();
        token.setState(RandomForTests.randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void when_set_issuer_and_already_ciphered_then_error() {
        given_forged_token();
        when_cypher_the_token();
        token.setIssuer(RandomForTests.randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void when_set_audience_and_already_ciphered_then_error() {
        given_forged_token();
        when_cypher_the_token();
        token.setAudience(RandomForTests.randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void when_set_target_URL_and_already_ciphered_then_error() {
        given_forged_token();
        when_cypher_the_token();
        token.setTargetURL(RandomForTests.randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void when_set_delegator_and_already_ciphered_then_error() {
        given_forged_token();
        when_cypher_the_token();
        token.setDelegator(RandomForTests.randomString());
    }

    @Test(expected = TokenUnmodifiableException.class)
    public void when_set_delegate_and_already_ciphered_then_error() {
        given_forged_token();
        when_cypher_the_token();
        token.setDelegate(RandomForTests.randomString());
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
        token.setClaim(RandomForTests.randomString(), RandomForTests.randomString());
    }

    private void given_forged_token() {
        token.setStamp(RandomForTests.randomString());
        token.setVerdict(TokenVerdict.SUCCESS);
        token.setDelegate(RandomForTests.randomString());
        token.setDelegator(RandomForTests.randomString());
        token.setAudience(RandomForTests.randomString());
        token.setIssuer(RandomForTests.randomString());
        token.setState(RandomForTests.randomString());
        token.setScopes(new HashSet<>(Arrays.asList(PERSON, POLICY)));
        token.setBP(RandomForTests.randomString());
        token.setSubject(RandomForTests.randomString());
        token.setTargetURL(RandomForTests.randomString());
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

    private String getAttributeValue(final Assertion assertion, final String attributeName) {
        final Attribute actual = assertion.getAttributeStatements().get(0)
                .getAttributes().stream().filter(attr -> attributeName.equals(attr.getName())).findFirst().get();
        return ((XSString) actual.getAttributeValues().get(0)).getValue();
    }

}
