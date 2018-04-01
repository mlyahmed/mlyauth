package com.mlyauth.token;

import com.mlyauth.constants.TokenScope;
import com.mlyauth.constants.TokenVerdict;
import com.mlyauth.domain.Token;
import com.mlyauth.exception.IDPSAMLErrorException;
import com.mlyauth.token.saml.SAMLAccessToken;
import com.mlyauth.token.saml.SAMLHelper;
import javafx.util.Pair;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Before;
import org.junit.Test;
import org.opensaml.DefaultBootstrap;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.security.credential.Credential;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static com.mlyauth.token.Claims.*;
import static com.mlyauth.tools.KeysForTests.generateRSACredential;
import static com.mlyauth.tools.RandomForTests.randomString;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.exparity.hamcrest.date.LocalDateTimeMatchers.within;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class TokenMapperTest {

    private TokenMapper mapper;

    @Before
    public void setup() {
        mapper = new TokenMapper();
    }

    @Test
    public void when_map_a_null_then_return_null() {
        final Token token = mapper.toToken(null);
        assertThat(token, nullValue());
    }

    @Test
    public void when_map_a_saml_access_token_then_map_to_non_null_token() {
        final SAMLAccessToken access = given_an_access_saml_token();
        final Token token = mapper.toToken(access);
        assertThat(token, notNullValue());
    }

    @Test
    public void when_map_a_saml_access_token_then_the_stamp_must_be_encoded() {
        final SAMLAccessToken access = given_an_access_saml_token();
        final Token token = mapper.toToken(access);
        assertThat(DigestUtils.sha256Hex(access.getStamp()), equalTo(token.getStamp()));
    }

    @Test
    public void when_map_a_saml_access_token_then_the_times_must_be_mapped() {
        final SAMLAccessToken access = given_an_access_saml_token();
        final Token token = mapper.toToken(access);
        assertThat(token.getIssuanceTime(), notNullValue());
        assertThat(toDateTime(token.getIssuanceTime()), within(0, SECONDS, access.getIssuanceTime()));
        assertThat(toDateTime(token.getEffectiveTime()), within(0, SECONDS, access.getEffectiveTime()));
        assertThat(token.getExpiryTime(), notNullValue());
        assertThat(toDateTime(token.getExpiryTime()), within(0, SECONDS, access.getExpiryTime()));
    }

    @Test
    public void when_map_a_saml_access_token_then_type_and_norm_must_be_mapped() {
        final SAMLAccessToken access = given_an_access_saml_token();
        final Token token = mapper.toToken(access);
        assertThat(token.getType(), notNullValue());
        assertThat(token.getType(), equalTo(access.getType()));
        assertThat(token.getNorm(), notNullValue());
        assertThat(token.getNorm(), equalTo(access.getNorm()));

    }

    @Test
    public void when_map_a_saml_access_token_then_subject_must_be_mapped() {
        final SAMLAccessToken access = given_an_access_saml_token();
        final Token token = mapper.toToken(access);
        assertThat(token.getClaimsMap().get(SUBJECT.getValue()), notNullValue());
        assertThat(token.getClaimsMap().get(SUBJECT.getValue()).getValue(), equalTo(access.getSubject()));
        assertThat(token.getClaimsMap().get(SUBJECT.getValue()).getToken(), equalTo(token));
    }

    @Test
    public void when_map_a_saml_access_token_then_scopes_must_be_mapped() {
        final SAMLAccessToken access = given_an_access_saml_token();
        final Token token = mapper.toToken(access);
        assertThat(token.getClaimsMap().get(SCOPES.getValue()), notNullValue());
        assertThat(token.getClaimsMap().get(SCOPES.getValue()).getValue(), equalTo(compactScopes(access.getScopes())));
        assertThat(token.getClaimsMap().get(SCOPES.getValue()).getToken(), equalTo(token));
    }

    @Test
    public void when_map_a_saml_access_token_then_bp_must_be_mapped() {
        final SAMLAccessToken access = given_an_access_saml_token();
        final Token token = mapper.toToken(access);
        assertThat(token.getClaimsMap().get(BP.getValue()), notNullValue());
        assertThat(token.getClaimsMap().get(BP.getValue()).getValue(), equalTo(access.getBP()));
        assertThat(token.getClaimsMap().get(BP.getValue()).getToken(), equalTo(token));
    }

    @Test
    public void when_map_a_saml_access_token_then_state_must_be_mapped() {
        final SAMLAccessToken access = given_an_access_saml_token();
        final Token token = mapper.toToken(access);
        assertThat(token.getClaimsMap().get(STATE.getValue()), notNullValue());
        assertThat(token.getClaimsMap().get(STATE.getValue()).getValue(), equalTo(access.getState()));
        assertThat(token.getClaimsMap().get(STATE.getValue()).getToken(), equalTo(token));
    }

    @Test
    public void when_map_a_saml_access_token_then_issuer_must_be_mapped() {
        final SAMLAccessToken access = given_an_access_saml_token();
        final Token token = mapper.toToken(access);
        assertThat(token.getClaimsMap().get(ISSUER.getValue()), notNullValue());
        assertThat(token.getClaimsMap().get(ISSUER.getValue()).getValue(), equalTo(access.getIssuer()));
        assertThat(token.getClaimsMap().get(ISSUER.getValue()).getToken(), equalTo(token));
    }

    @Test
    public void when_map_a_saml_access_token_then_audience_must_be_mapped() {
        final SAMLAccessToken access = given_an_access_saml_token();
        final Token token = mapper.toToken(access);
        assertThat(token.getClaimsMap().get(AUDIENCE.getValue()), notNullValue());
        assertThat(token.getClaimsMap().get(AUDIENCE.getValue()).getValue(), equalTo(access.getAudience()));
        assertThat(token.getClaimsMap().get(AUDIENCE.getValue()).getToken(), equalTo(token));
    }

    @Test
    public void when_map_a_saml_access_token_then_target_url_must_be_mapped() {
        final SAMLAccessToken access = given_an_access_saml_token();
        final Token token = mapper.toToken(access);
        assertThat(token.getClaimsMap().get(TARGET_URL.getValue()), notNullValue());
        assertThat(token.getClaimsMap().get(TARGET_URL.getValue()).getValue(), equalTo(access.getTargetURL()));
        assertThat(token.getClaimsMap().get(TARGET_URL.getValue()).getToken(), equalTo(token));
    }

    @Test
    public void when_map_a_saml_access_token_then_delegator_must_be_mapped() {
        final SAMLAccessToken access = given_an_access_saml_token();
        final Token token = mapper.toToken(access);
        assertThat(token.getClaimsMap().get(DELEGATOR.getValue()), notNullValue());
        assertThat(token.getClaimsMap().get(DELEGATOR.getValue()).getValue(), equalTo(access.getDelegator()));
        assertThat(token.getClaimsMap().get(DELEGATOR.getValue()).getToken(), equalTo(token));
    }

    @Test
    public void when_map_a_saml_access_token_then_delegate_must_be_mapped() {
        final SAMLAccessToken access = given_an_access_saml_token();
        final Token token = mapper.toToken(access);
        assertThat(token.getClaimsMap().get(DELEGATE.getValue()), notNullValue());
        assertThat(token.getClaimsMap().get(DELEGATE.getValue()).getValue(), equalTo(access.getDelegate()));
        assertThat(token.getClaimsMap().get(DELEGATE.getValue()).getToken(), equalTo(token));
    }

    @Test
    public void when_map_a_saml_access_token_then_verdict_must_be_mapped() {
        final SAMLAccessToken access = given_an_access_saml_token();
        final Token token = mapper.toToken(access);
        assertThat(token.getClaimsMap().get(VERDICT.getValue()), notNullValue());
        assertThat(token.getClaimsMap().get(VERDICT.getValue()).getValue(), equalTo(access.getVerdict().name()));
        assertThat(token.getClaimsMap().get(VERDICT.getValue()).getToken(), equalTo(token));
    }

    @Test
    public void when_map_a_saml_access_token_then_client_Id_must_be_mapped() {
        final SAMLAccessToken access = given_an_access_saml_token();
        access.setClaim(CLIENT_ID.getValue(), randomString());
        final Token token = mapper.toToken(access);
        assertThat(token.getClaimsMap().get(CLIENT_ID.getValue()), notNullValue());
        assertThat(token.getClaimsMap().get(CLIENT_ID.getValue()).getValue(), equalTo(access.getClaim(CLIENT_ID.getValue())));
        assertThat(token.getClaimsMap().get(CLIENT_ID.getValue()).getToken(), equalTo(token));
    }

    @Test
    public void when_map_a_saml_access_token_then_client_profile_must_be_mapped() {
        final SAMLAccessToken access = given_an_access_saml_token();
        access.setClaim(CLIENT_PROFILE.getValue(), randomString());
        final Token token = mapper.toToken(access);
        assertThat(token.getClaimsMap().get(CLIENT_PROFILE.getValue()), notNullValue());
        assertThat(token.getClaimsMap().get(CLIENT_PROFILE.getValue()).getValue(), equalTo(access.getClaim(CLIENT_PROFILE.getValue())));
        assertThat(token.getClaimsMap().get(CLIENT_PROFILE.getValue()).getToken(), equalTo(token));
    }

    @Test
    public void when_map_a_saml_access_token_then_entity_id_must_be_mapped() {
        final SAMLAccessToken access = given_an_access_saml_token();
        access.setClaim(ENTITY_ID.getValue(), randomString());
        final Token token = mapper.toToken(access);
        assertThat(token.getClaimsMap().get(ENTITY_ID.getValue()), notNullValue());
        assertThat(token.getClaimsMap().get(ENTITY_ID.getValue()).getValue(), equalTo(access.getClaim(ENTITY_ID.getValue())));
        assertThat(token.getClaimsMap().get(ENTITY_ID.getValue()).getToken(), equalTo(token));
    }

    @Test
    public void when_map_a_saml_access_token_then_action_must_be_mapped() {
        final SAMLAccessToken access = given_an_access_saml_token();
        access.setClaim(ACTION.getValue(), randomString());
        final Token token = mapper.toToken(access);
        assertThat(token.getClaimsMap().get(ACTION.getValue()), notNullValue());
        assertThat(token.getClaimsMap().get(ACTION.getValue()).getValue(), equalTo(access.getClaim(ACTION.getValue())));
        assertThat(token.getClaimsMap().get(ACTION.getValue()).getToken(), equalTo(token));
    }

    @Test
    public void when_map_a_saml_access_token_then_application_must_be_mapped() {
        final SAMLAccessToken access = given_an_access_saml_token();
        access.setClaim(APPLICATION.getValue(), randomString());
        final Token token = mapper.toToken(access);
        assertThat(token.getClaimsMap().get(APPLICATION.getValue()), notNullValue());
        assertThat(token.getClaimsMap().get(APPLICATION.getValue()).getValue(), equalTo(access.getClaim(APPLICATION.getValue())));
        assertThat(token.getClaimsMap().get(APPLICATION.getValue()).getToken(), equalTo(token));
    }

    @Test
    public void when_map_a_saml_access_without_claims_then_claims_set_must_ne_empty() throws ConfigurationException {
        DefaultBootstrap.bootstrap();
        SAMLHelper samlHelper = new SAMLHelper();
        final Pair<PrivateKey, X509Certificate> pair = generateRSACredential();
        final Credential credential = samlHelper.toCredential(pair.getKey(), pair.getValue());
        SAMLAccessToken access = new SAMLAccessToken(credential);
        access.setStamp(randomString());
        final Token token = mapper.toToken(access);
        assertThat(token.getClaims(), is(empty()));
    }

    private LocalDateTime toDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    protected String compactScopes(Set<TokenScope> scopes) {
        return scopes != null ? scopes.stream().map(TokenScope::name).collect(Collectors.joining("|")) : null;
    }


    private SAMLAccessToken given_an_access_saml_token() {
        try {
            DefaultBootstrap.bootstrap();
            SAMLHelper samlHelper = new SAMLHelper();
            final Pair<PrivateKey, X509Certificate> pair = generateRSACredential();
            final Credential credential = samlHelper.toCredential(pair.getKey(), pair.getValue());
            SAMLAccessToken access = new SAMLAccessToken(credential);
            access.setStamp(randomString());
            access.setSubject(randomString());
            access.setScopes(new HashSet<>(Arrays.asList(TokenScope.values())));
            access.setBP(randomString());
            access.setState(randomString());
            access.setIssuer(randomString());
            access.setAudience(randomString());
            access.setTargetURL(randomString());
            access.setDelegator(randomString());
            access.setDelegate(randomString());
            access.setVerdict(TokenVerdict.SUCCESS);
            return access;
        } catch (Exception e) {
            throw IDPSAMLErrorException.newInstance(e);
        }
    }

}