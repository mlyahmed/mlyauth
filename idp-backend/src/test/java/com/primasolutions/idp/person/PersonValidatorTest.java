package com.primasolutions.idp.person;

import com.primasolutions.idp.constants.RoleCode;
import com.primasolutions.idp.exception.IDPException;
import com.primasolutions.idp.person.mocks.MockPersonLookuper;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class PersonValidatorTest {


    private PersonValidator validator;

    @Before
    public void setup() {
        validator = new PersonValidator();
        setField(validator, "personLookuper", MockPersonLookuper.getInstance());
    }

    @Test
    public void when_a_new_person_is_valid_then_no_error() {
        final PersonBean person = PersonBean.newInstance()
                .setFirstname("Moulay Ahmed")
                .setLastname("EL IDRISSI")
                .setRole(RoleCode.CLIENT)
                .setEmail("ahmed@gmail.com");

        validator.validateNew(person);
    }

    @Test
    public void when_validate_new_person_and_null_then_error() {
        IDPException ex = null;

        try {
            validator.validateNew(null);
        } catch (IDPException e) {
            ex = e;
        }

        assertThat(ex, Matchers.notNullValue());
        assertThat(ex.getErrors(), hasSize(1));
        assertThat(ex.getErrors().stream().findFirst().get().getCode(), equalTo("PERSON_IS_NULL"));
    }


    @Test
    public void when_email_is_null_then_error() {
        final PersonBean person = PersonBean.newInstance()
                .setFirstname("Ahmed")
                .setLastname("EL IDRISSI")
                .setPassword("password".toCharArray());

        IDPException ex = null;

        try {
            validator.validateNew(person);
        } catch (IDPException e) {
            ex = e;
        }

        assertThat(ex, Matchers.notNullValue());
        assertThat(ex.getErrors(), hasSize(1));
        assertThat(ex.getErrors().stream().findFirst().get().getCode(), equalTo("PERSON_EMAIL_IS_NULL"));

    }

}
