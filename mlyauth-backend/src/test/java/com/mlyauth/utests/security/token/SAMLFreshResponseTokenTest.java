package com.mlyauth.utests.security.token;

import com.mlyauth.constants.*;
import com.mlyauth.security.sso.SAMLHelper;
import com.mlyauth.security.token.SAMLResponseToken;
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
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AuthnContext;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.core.SubjectConfirmation;
import org.opensaml.xml.ConfigurationException;
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

import static com.mlyauth.constants.TokenScope.*;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.opensaml.saml2.core.NameIDType.TRANSIENT;

@RunWith(DataProviderRunner.class)
public class SAMLFreshResponseTokenTest {

    private SAMLResponseToken token;
    private SAMLHelper samlHelper;
    private Credential cypherCredential;
    private Credential decipherCredential;

    @Before
    public void setup() throws ConfigurationException {
        DefaultBootstrap.bootstrap();
        samlHelper = new SAMLHelper();
        final Pair<PrivateKey, X509Certificate> pair1 = KeysForTests.generateRSACredential();
        final Pair<PrivateKey, X509Certificate> pair2 = KeysForTests.generateRSACredential();
        cypherCredential = samlHelper.toCredential(pair1.getKey(), pair2.getValue());
        decipherCredential = samlHelper.toCredential(pair2.getKey(), pair1.getValue());
        token = new SAMLResponseToken(cypherCredential);
        ReflectionTestUtils.setField(token, "samlHelper", samlHelper);
    }



    @Test(expected = IllegalArgumentException.class)
    public void when_credential_is_null_the_error() {
        new SAMLResponseToken(null);
    }


    @Test(expected = IllegalArgumentException.class)
    public void when_credential_private_key_is_null_then_error() {
        final BasicCredential credential = new BasicCredential();
        credential.setPublicKey(KeysForTests.generateRSACredential().getValue().getPublicKey());
        credential.setPrivateKey(null);
        new SAMLResponseToken(credential);
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_credential_public_key_is_null_then_error() {
        final BasicCredential credential = new BasicCredential();
        credential.setPrivateKey(KeysForTests.generateRSACredential().getKey());
        credential.setPublicKey(null);
        new SAMLResponseToken(credential);
    }

    @Test
    public void when_create_fresh_response_then_token_native_must_be_fresh() {
        assertThat(token.getNative(), notNullValue());
        assertThat(token.getNative().getStatus(), notNullValue());
        assertThat(token.getNative().getStatus().getStatusCode(), notNullValue());
        assertThat(token.getNative().getIssuer(), notNullValue());
        assertThat(token.getNative().getAssertions(), hasSize(1));
        assertThat(token.getNative().getAssertions().get(0).getIssuer(), notNullValue());
        assertThat(token.getNative().getAssertions().get(0).getSubject(), notNullValue());
        assertThat(token.getNative().getAssertions().get(0).getSubject().getSubjectConfirmations(), hasSize(1));
        assertThat(token.getNative().getAssertions().get(0).getSubject().getSubjectConfirmations()
                .get(0).getSubjectConfirmationData(), notNullValue());
        assertThat(token.getNative().getAssertions().get(0).getConditions(), notNullValue());
        assertThat(token.getNative().getAssertions().get(0).getConditions().getAudienceRestrictions(), hasSize(1));
        assertThat(token.getNative().getAssertions().get(0).getConditions().getAudienceRestrictions()
                .get(0).getAudiences(), hasSize(1));
        assertThat(token.getNative().getAssertions().get(0).getAuthnStatements(), hasSize(1));
        assertThat(token.getNative().getAssertions().get(0).getAuthnStatements().get(0).getAuthnContext(), notNullValue());
        assertThat(token.getNative().getAssertions().get(0).getAuthnStatements().get(0).getAuthnContext()
                .getAuthnContextClassRef(), notNullValue());
        assertThat(token.getNative().getAssertions().get(0).getAuthnStatements().get(0).getAuthnContext()
                .getAuthnContextClassRef().getAuthnContextClassRef(), equalTo(AuthnContext.PASSWORD_AUTHN_CTX));
    }

    @Test
    public void when_create_a_fresh_token_then_it_is_effective_now() {
        assertThat(token.getEffectiveTime(), notNullValue());
        assertThat(token.getEffectiveTime().isAfter(LocalDateTime.now().minusSeconds(1)), equalTo(true));
        assertThat(token.getNative().getAssertions().get(0).getAuthnStatements()
                .get(0).getAuthnInstant(), notNullValue());
        assertThat(token.getNative().getAssertions().get(0).getAuthnStatements()
                .get(0).getAuthnInstant().toDateTime(DateTimeZone.getDefault())
                .isAfter((new DateTime()).minusSeconds(1)), equalTo(true));
    }


    @Test
    public void when_create_a_fresh_token_then_it_expires_in_2_minutes() {
        assertThat(token.getExpiryTime(), notNullValue());
        assertThat(token.getExpiryTime().isBefore(LocalDateTime.now().plusMinutes(3)), equalTo(true));
        assertThat(token.getNative().getAssertions()
                .get(0).getSubject().getSubjectConfirmations()
                .get(0).getSubjectConfirmationData().getNotOnOrAfter(), notNullValue());
        assertThat(token.getNative().getAssertions()
                .get(0).getSubject().getSubjectConfirmations()
                .get(0).getSubjectConfirmationData().getNotOnOrAfter().toDateTime(DateTimeZone.getDefault())
                .isBefore(DateTime.now().plusMinutes(3)), equalTo(true));
        assertThat(token.getNative().getAssertions()
                .get(0).getConditions().getNotOnOrAfter(), notNullValue());
        assertThat(token.getNative().getAssertions()
                .get(0).getConditions().getNotOnOrAfter().toDateTime(DateTimeZone.getDefault())
                .isBefore(DateTime.now().plusMinutes(3)), equalTo(true));
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
    public void when_create_a_fresh_token_and_set_scopes_then_they_must_be_set(String... scopes) {
        final Set<TokenScope> scopesSet = Arrays.stream(scopes).map(TokenScope::valueOf).collect(toSet());
        token.setScopes(scopesSet);
        assertThat(token.getScopes(), equalTo(scopesSet));
        assertThat(token.getStatus(), equalTo(TokenStatus.FORGED));
    }

    @Test
    public void when_create_a_fresh_token_and_set_Id_then_must_be_set() {
        String id = randomString();
        token.setId(id);
        assertThat(token.getId(), equalTo(id));
        assertThat(token.getNative().getID(), equalTo(id));
        assertThat(token.getNative().getAssertions().get(0).getID(), equalTo(id));
        assertThat(token.getStatus(), equalTo(TokenStatus.FORGED));
    }

    @Test
    public void when_create_a_fresh_token_it_issued_now() {
        assertThat(token.getIssuanceTime(), notNullValue());
        assertThat(token.getIssuanceTime().isAfter(LocalDateTime.now().minusSeconds(1)), equalTo(true));
        assertThat(token.getNative().getIssueInstant(), notNullValue());
        assertThat(token.getNative().getIssueInstant().toDateTime(DateTimeZone.getDefault())
                .isAfter((new DateTime()).minusSeconds(1)), equalTo(true));
        assertThat(token.getNative().getAssertions().get(0).getIssueInstant(), notNullValue());
        assertThat(token.getNative().getAssertions().get(0).getIssueInstant()
                .toDateTime(DateTimeZone.getDefault()).isAfter(DateTime.now().minusSeconds(1)), equalTo(true));
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
        assertThat(token.getNative().getAssertions(), hasSize(1));
        assertThat(token.getNative().getAssertions().get(0).getSubject(), notNullValue());
        assertThat(token.getNative().getAssertions().get(0).getSubject().getNameID().getValue(), equalTo(subject));
        assertThat(token.getNative().getAssertions().get(0).getSubject().getNameID().getFormat(), equalTo(TRANSIENT));
        assertThat(token.getNative().getAssertions().get(0).getSubject().getSubjectConfirmations(), hasSize(1));
        assertThat(token.getNative().getAssertions().get(0).getSubject().getSubjectConfirmations()
                .get(0).getMethod(), equalTo(SubjectConfirmation.METHOD_BEARER));
        assertThat(token.getStatus(), equalTo(TokenStatus.FORGED));
    }

    @Test
    public void when_create_a_fresh_token_and_set_BP_then_it_must_be_set() {
        final String bp = randomString();
        token.setBP(bp);
        assertThat(token.getBP(), equalTo(bp));
        assertThat(token.getStatus(), equalTo(TokenStatus.FORGED));
    }

    @Test
    public void when_create_a_fresh_token_and_set_State_then_it_must_be_set() {
        final String state = randomString();
        token.setState(state);
        assertThat(token.getState(), equalTo(state));
        assertThat(token.getStatus(), equalTo(TokenStatus.FORGED));
    }

    @Test
    public void when_create_a_fresh_token_and_set_issuer_then_it_must_be_set() {
        final String issuerURI = randomString();
        token.setIssuer(issuerURI);
        assertThat(token.getIssuer(), equalTo(issuerURI));
        assertThat(token.getNative().getIssuer(), notNullValue());
        assertThat(token.getNative().getIssuer().getValue(), equalTo(issuerURI));
        assertThat(token.getNative().getAssertions().get(0).getIssuer(), notNullValue());
        assertThat(token.getNative().getAssertions().get(0).getIssuer().getValue(), equalTo(issuerURI));
        assertThat(token.getStatus(), equalTo(TokenStatus.FORGED));
    }

    @Test
    public void when_create_fresh_response_then_token_claims_must_be_fresh() {
        assertThat(token.getId(), nullValue());
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
        assertThat(token.getStatus(), equalTo(TokenStatus.CREATED));
    }

    @Test
    public void when_create_a_fresh_token_and_set_delegator_then_it_must_be_set() {
        final String delegator = randomString();
        token.setDelegator(delegator);
        assertThat(token.getDelegator(), equalTo(delegator));
        assertThat(token.getStatus(), equalTo(TokenStatus.FORGED));
    }

    @Test
    public void when_create_a_fresh_token_and_set_delegate_then_it_must_be_set() {
        final String delegate = randomString();
        token.setDelegate(delegate);
        assertThat(token.getDelegate(), equalTo(delegate));
        assertThat(token.getStatus(), equalTo(TokenStatus.FORGED));
    }

    @Test
    public void when_create_fresh_token_and_set_success_verdict_then_it_must_be_set() {
        token.setVerdict(TokenVerdict.SUCCESS);
        assertThat(token.getVerdict(), equalTo(TokenVerdict.SUCCESS));
        assertThat(token.getNative().getStatus().getStatusCode().getValue(), equalTo(StatusCode.SUCCESS_URI));
        assertThat(token.getStatus(), equalTo(TokenStatus.FORGED));
    }

    @Test
    public void when_create_fresh_token_and_set_fail_verdict_then_it_must_be_set() {
        token.setVerdict(TokenVerdict.FAIL);
        assertThat(token.getVerdict(), equalTo(TokenVerdict.FAIL));
        assertThat(token.getNative().getStatus().getStatusCode().getValue(), equalTo(StatusCode.AUTHN_FAILED_URI));
        assertThat(token.getStatus(), equalTo(TokenStatus.FORGED));
    }

    @Test
    public void when_create_a_fresh_token_and_set_audience_then_it_must_be_set() {
        final String audience = randomString();
        token.setAudience(audience);
        assertThat(token.getAudience(), equalTo(audience));
        assertThat(token.getNative().getAssertions().get(0).getConditions(), notNullValue());
        assertThat(token.getNative().getAssertions().get(0).getConditions().getAudienceRestrictions()
                .get(0).getAudiences().get(0).getAudienceURI(), equalTo(audience));
        assertThat(token.getNative().getAssertions().get(0).getSubject().getSubjectConfirmations()
                .get(0).getSubjectConfirmationData().getInResponseTo(), equalTo(audience));
        assertThat(token.getStatus(), equalTo(TokenStatus.FORGED));
    }

    @Test
    public void when_create_a_fresh_token_and_set_target_URL_then_it_must_be_set() {
        final String url = randomString();
        token.setTargetURL(url);
        assertThat(token.getTargetURL(), equalTo(url));
        assertThat(token.getNative().getDestination(), equalTo(url));
        assertThat(token.getNative().getAssertions().get(0).getSubject().getSubjectConfirmations().get(0).getSubjectConfirmationData().getRecipient(), equalTo(url));
    }

    @Test
    public void when_create_a_fresh_token_and_cypher_it_then_it_must_be_cyphered() {
        given_forged_token();
        token.cypher();
        assertThat(token.getNative().getAssertions(), empty());
        assertThat(token.getNative().getEncryptedAssertions(), hasSize(1));
        Assertion assertion = samlHelper.decryptAssertion(token.getNative().getEncryptedAssertions().get(0), decipherCredential);
        assertThat(assertion, notNullValue());
    }

    @Test
    public void when_create_a_fresh_token_and_cypher_it_then_it_must_be_signed() {
        given_forged_token();
        token.cypher();
        samlHelper.validateSignature(token.getNative(), decipherCredential);
    }

    @Test
    public void when_create_a_fresh_token_and_cypher_it_then_it_must_be_set_as_cyphered() {
        given_forged_token();
        token.cypher();
        assertThat(token.getStatus(), equalTo(TokenStatus.CYPHERED));
    }

    private void given_forged_token() {
        token.setId(randomString());
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

    private static String randomString() {
        final int length = (new Random()).nextInt(30);
        return RandomStringUtils.random(length > 0 ? length : 20, true, true);
    }
}
