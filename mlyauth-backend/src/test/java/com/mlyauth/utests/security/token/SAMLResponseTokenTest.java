package com.mlyauth.utests.security.token;

import com.mlyauth.constants.TokenNorm;
import com.mlyauth.constants.TokenScope;
import com.mlyauth.constants.TokenStatus;
import com.mlyauth.constants.TokenType;
import com.mlyauth.security.sso.SAMLHelper;
import com.mlyauth.security.token.SAMLResponseToken;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.Response;
import org.opensaml.xml.ConfigurationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Set;

import static com.mlyauth.constants.TokenScope.*;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@RunWith(DataProviderRunner.class)
public class SAMLResponseTokenTest {

    private SAMLResponseToken token;
    private SAMLHelper samlHelper;

    @Before
    public void setup() throws ConfigurationException {
        DefaultBootstrap.bootstrap();
        samlHelper = new SAMLHelper();
        token = new SAMLResponseToken();
        ReflectionTestUtils.setField(token, "samlHelper", samlHelper);
    }

    @Test
    public void when_create_fresh_response_then_token_must_be_fresh() {
        assertThat(token.getNative(), notNullValue());
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
        assertThat(token.getExpiryTime(), nullValue());
        assertThat(token.getEffectiveTime(), nullValue());
        assertThat(token.getIssuanceTime(), nullValue());
        assertThat(token.getNorm(), equalTo(TokenNorm.SAML));
        assertThat(token.getType(), equalTo(TokenType.ACCESS));
        assertThat(token.getStatus(), equalTo(TokenStatus.CREATED));
    }

    @Test
    public void when_get_native_then_return_a_copy() {
        final Response response = token.getNative();
        response.setID(samlHelper.generateRandomId());
        assertThat(token.getId(), nullValue());
        assertThat(token.getNative().getID(), nullValue());
    }

    @Test
    public void when_create_a_fresh_token_and_set_Id_then_must_be_set() {
        final String id = samlHelper.generateRandomId();
        token.setId(id);
        assertThat(token.getId(), equalTo(id));
        assertThat(token.getNative().getID(), equalTo(id));
        assertThat(token.getStatus(), equalTo(TokenStatus.FORGED));
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
        assertThat(token.getNative().getAssertions(), empty());
        assertThat(token.getStatus(), equalTo(TokenStatus.FORGED));
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

    //public void when_create_a_fresh_token_and_set_BP_then_it_must_be_set
}
