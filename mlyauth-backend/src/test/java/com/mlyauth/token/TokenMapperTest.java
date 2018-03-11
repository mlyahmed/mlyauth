package com.mlyauth.token;

import com.mlyauth.SecurityConfig;
import com.mlyauth.constants.TokenScope;
import com.mlyauth.constants.TokenVerdict;
import com.mlyauth.domain.Token;
import com.mlyauth.exception.IDPSAMLErrorException;
import com.mlyauth.token.saml.SAMLAccessToken;
import com.mlyauth.token.saml.SAMLHelper;
import javafx.util.Pair;
import org.junit.Before;
import org.junit.Test;
import org.opensaml.DefaultBootstrap;
import org.opensaml.xml.security.credential.Credential;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static com.mlyauth.token.IDPClaims.SCOPES;
import static com.mlyauth.token.IDPClaims.SUBJECT;
import static com.mlyauth.tools.KeysForTests.generateRSACredential;
import static com.mlyauth.tools.RandomForTests.randomString;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.exparity.hamcrest.date.LocalDateTimeMatchers.within;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class TokenMapperTest {

    private SecurityConfig securityConfig;
    private PasswordEncoder encoder;
    private TokenMapper mapper;

    @Before
    public void setup() {
        securityConfig = new SecurityConfig();
        encoder = securityConfig.passwordEncoder();
        mapper = new TokenMapper();
        ReflectionTestUtils.setField(mapper, "encoder", encoder);
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
        assertThat(encoder.matches(access.getStamp(), token.getStamp()), equalTo(true));
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
    public void when_map_a_saml_access_token_then_claims_must_be_mapped() {
        final SAMLAccessToken access = given_an_access_saml_token();
        final Token token = mapper.toToken(access);
        assertThat(token.getClaimsMap().get(SUBJECT.getValue()), notNullValue());
        assertThat(token.getClaimsMap().get(SUBJECT.getValue()).getValue(), equalTo(access.getSubject()));
        assertThat(token.getClaimsMap().get(SUBJECT.getValue()).getToken(), equalTo(token));
        assertThat(token.getClaimsMap().get(SCOPES.getValue()), notNullValue());
        assertThat(token.getClaimsMap().get(SCOPES.getValue()).getValue(), equalTo(compactScopes(access.getScopes())));
        assertThat(token.getClaimsMap().get(SCOPES.getValue()).getToken(), equalTo(token));
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