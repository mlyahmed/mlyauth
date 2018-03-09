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

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashSet;

import static com.mlyauth.tools.RandomForTests.randomString;

public class JOSEAccessTokenValidatorTest {

    private JOSEAccessTokenValidator validator;

    @Before
    public void setup() {
        validator = new JOSEAccessTokenValidator();
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_the_token_is_null_then_error() {
        validator = new JOSEAccessTokenValidator();
        validator.validate(null);
    }

    @Test
    public void when_the_token_is_valid_then_ok() {
        JOSEAccessToken access = given_access_token();
        boolean valid = validator.validate(access);
        Assert.assertThat(valid, Matchers.equalTo(true));
    }

    @Test(expected = InvalidTokenException.class)
    public void when_the_id_is_null_then_error() {
        JOSEAccessToken access = given_access_token();
        access.setId(null);
        validator.validate(access);
    }

    @Test(expected = InvalidTokenException.class)
    public void when_the_subject_is_null_then_error() {
        JOSEAccessToken access = given_access_token();
        access.setSubject(null);
        validator.validate(access);
    }

    @Test(expected = InvalidTokenException.class)
    public void when_the_scopes_list_is_null_then_error() {
        JOSEAccessToken access = given_access_token();
        access.setScopes(null);
        validator.validate(access);
    }

    @Test(expected = InvalidTokenException.class)
    public void when_the_bp_is_null_then_error() {
        JOSEAccessToken access = given_access_token();
        access.setBP(null);
        validator.validate(access);
    }

    private JOSEAccessToken given_access_token() {
        final Pair<PrivateKey, X509Certificate> credential = KeysForTests.generateRSACredential();
        JOSEAccessToken access = new JOSEAccessToken(credential.getKey(), credential.getValue().getPublicKey());
        access.setId(randomString());
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
    }
}
