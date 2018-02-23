package com.mlyauth.utests.security.functions.validators;

import com.mlyauth.security.functions.IDPPermission;
import com.mlyauth.security.functions.validators.PersonPermissionValidator;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

public class PersonPermissionValidatorTest {

    private PersonPermissionValidator validator;
    private Authentication authentication;

    @Before
    public void setup() {
        validator = new PersonPermissionValidator();
        authentication = new TestingAuthenticationToken(null, null, new String[]{});
    }


    @Test
    public void when_person_is_null_then_OK() {
        final boolean hasPermission = validator.hasPermission(authentication, null, IDPPermission.CREATE);
        Assert.assertThat(hasPermission, Matchers.equalTo(true));
    }

    @Test
    public void when_authentication_is_null__and_person_null_then_not_permitted() {
        final boolean hasPermission = validator.hasPermission(null, null, IDPPermission.CREATE);
        Assert.assertThat(hasPermission, Matchers.equalTo(false));
    }

    @Test
    public void when_authentication_is_null__then_not_permitted() {
        final boolean hasPermission = validator.hasPermission(null, null, IDPPermission.CREATE);
        Assert.assertThat(hasPermission, Matchers.equalTo(false));
    }
}
