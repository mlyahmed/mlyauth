package com.mlyauth.security.functions.validators;

import com.mlyauth.beans.PersonBean;
import com.mlyauth.constants.ProfileCode;
import com.mlyauth.security.functions.IDPPermission;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(DataProviderRunner.class)
public class PersonPermissionValidatorTest {

    private PersonPermissionValidator validator;
    private Authentication authentication;

    @Before
    public void setup() {
        validator = new PersonPermissionValidator();
        authentication = new TestingAuthenticationToken(null, null, new String[]{});
    }

    @DataProvider
    public static Object[] allActions() {
        // @formatter:off
        return new String[]{
                IDPPermission.CREATE.name(),
                IDPPermission.CREATE.name()
        };
        // @formatter:on
    }

    @Test
    @UseDataProvider("allActions")
    public void when_person_is_null_then_permit(String action) {
        assertThat(validator.hasPermission(authentication, null, IDPPermission.valueOf(action)), equalTo(true));
    }

    @Test
    @UseDataProvider("allActions")
    public void when_authentication_is_null_and_person_null_then_not_permitted(String action) {
        assertThat(validator.hasPermission(null, null, IDPPermission.valueOf(action)), equalTo(false));
    }

    @Test
    @UseDataProvider("allActions")
    public void when_authentication_is_null_then_not_permitted(String action) {
        assertThat(validator.hasPermission(null, null, IDPPermission.valueOf(action)), equalTo(false));
    }


    @Test
    @UseDataProvider("allActions")
    public void when_the_person_is_a_master_then_permit(String action) {
        authentication = new TestingAuthenticationToken(null, null, ProfileCode.MASTER.name());
        assertThat(validator.hasPermission(authentication, null, IDPPermission.valueOf(action)), equalTo(true));
    }

    @Test
    public void when_the_person_is_not_a_master_and_wants_to_create_a_person_then_not_permit() {
        authentication = new TestingAuthenticationToken(null, null, new String[]{});
        PersonBean person = PersonBean.newInstance();
        assertThat(validator.hasPermission(authentication, person, IDPPermission.CREATE), equalTo(false));
    }


}
