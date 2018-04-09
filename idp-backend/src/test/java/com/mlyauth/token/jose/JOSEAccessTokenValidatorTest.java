package com.mlyauth.token.jose;

import com.mlyauth.constants.TokenScope;
import com.mlyauth.constants.TokenVerdict;
import com.mlyauth.exception.InvalidTokenException;
import com.mlyauth.tools.KeysForTests;
import javafx.util.Pair;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;

import static com.mlyauth.tools.RandomForTests.randomString;

public class JOSEAccessTokenValidatorTest {

    private String localEntityId;
    private JOSEAccessTokenValidator validator;

    @Before
    public void setup() {
        localEntityId = randomString();
        validator = new JOSEAccessTokenValidator();
        ReflectionTestUtils.setField(validator, "localEntityId", localEntityId);
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_the_token_is_null_then_error() {
        validator = new JOSEAccessTokenValidator();
        validator.validate(null);
    }

    @Test
    public void when_the_token_is_valid_then_ok() {
        JOSEAccessToken access = given_access_token();
        access.cypher();
        boolean valid = validator.validate(access);
        Assert.assertThat(valid, Matchers.equalTo(true));
    }

    @Test(expected = InvalidTokenException.class)
    public void when_the_id_is_null_then_error() {
        JOSEAccessToken access = given_access_token();
        access.setStamp(null);
        validator.validate(access);
    }

    @Test(expected = InvalidTokenException.class)
    public void when_the_subject_is_null_then_error() {
        JOSEAccessToken access = given_access_token();
        access.setSubject(null);
        validator.validate(access);
    }

    @Test(expected = InvalidTokenException.class)
    public void when_the_bp_is_null_then_error() {
        JOSEAccessToken access = given_access_token();
        access.setBP(null);
        validator.validate(access);
    }

    @Test(expected = InvalidTokenException.class)
    public void when_the_issuer_is_null_then_error() {
        JOSEAccessToken access = given_access_token();
        access.setIssuer(null);
        validator.validate(access);
    }

    @Test(expected = InvalidTokenException.class)
    public void when_the_audience_is_not_valid_then_error() {
        JOSEAccessToken access = given_access_token();
        access.setAudience(randomString());
        validator.validate(access);
    }

    @Test(expected = InvalidTokenException.class)
    public void when_the_target_url_is_not_valid_then_error() {
        JOSEAccessToken access = given_access_token();
        access.setTargetURL(null);
        validator.validate(access);
    }

    @Test(expected = InvalidTokenException.class)
    public void when_the_verdict_is_not_valid_then_error() {
        JOSEAccessToken access = given_access_token();
        access.setVerdict(null);
        validator.validate(access);
    }

    @Test(expected = InvalidTokenException.class)
    public void when_the_verdict_is_a_fail_then_error() {
        JOSEAccessToken access = given_access_token();
        access.setVerdict(TokenVerdict.FAIL);
        validator.validate(access);
    }

    @Test(expected = InvalidTokenException.class)
    public void when_the_token_is_expired_then_error() {
        MockJOSEAccessToken access = given_access_token();
        access.setExpiryTime(LocalDateTime.now().minusSeconds(1));
        validator.validate(access);
    }

    @Test(expected = InvalidTokenException.class)
    public void when_the_token_issuance_is_issuance_time_is_later_then_error() {
        MockJOSEAccessToken access = given_access_token();
        access.cypher();
        access.setIssuanceTime(LocalDateTime.now().plusSeconds(1));
        validator.validate(access);
    }

    @Test(expected = InvalidTokenException.class)
    public void when_the_token_issuance_is_effective_time_is_later_then_error() {
        MockJOSEAccessToken access = given_access_token();
        access.cypher();
        access.setEffectiveTime(LocalDateTime.now().plusSeconds(1));
        validator.validate(access);
    }

    private MockJOSEAccessToken given_access_token() {
        final Pair<PrivateKey, X509Certificate> credential = KeysForTests.generateRSACredential();
        MockJOSEAccessToken access = new MockJOSEAccessToken(credential.getKey(), credential.getValue().getPublicKey());
        access.setStamp(randomString());
        access.setSubject(randomString());
        access.setScopes(new HashSet<>(Arrays.asList(TokenScope.values())));
        access.setBP(randomString());
        access.setState(randomString());
        access.setIssuer(randomString());
        access.setAudience(localEntityId);
        access.setTargetURL(randomString());
        access.setDelegator(randomString());
        access.setDelegate(randomString());
        access.setVerdict(TokenVerdict.SUCCESS);
        return access;
    }
}
