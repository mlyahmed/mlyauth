package com.mlyauth.person;

import com.mlyauth.beans.PersonBean;
import com.mlyauth.exception.IDPException;
import com.mlyauth.lookup.PersonLookuper;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class PersonValidatorTest {


    private PersonLookuper personLookuper;

    private PersonValidator validator;

    @Before
    public void setup() {
        validator = new PersonValidator();
        personLookuper = Mockito.mock(PersonLookuper.class);
        ReflectionTestUtils.setField(validator, "personLookuper", personLookuper);
    }

    @Test
    public void when_a_new_person_is_valid_then_no_error() {
        final PersonBean person = PersonBean.newInstance()
                .setFirstname("Ahmed")
                .setLastname("EL IDRISSI")
                .setPassword("password".toCharArray())
                .setEmail("ahmed@gmail.com");

        validator.validateNewPerson(person);
    }

    @Test
    public void when_validate_new_person_and_null_then_error() {
        IDPException ex = null;

        try {
            validator.validateNewPerson(null);
        } catch (IDPException e) {
            ex = e;
        }

        assertThat(ex, Matchers.notNullValue());
        assertThat(ex.getErrors(), hasSize(1));
        assertThat(ex.getErrors().stream().findFirst().get().getCode(), equalTo("PERSON_IS_NULL"));
    }


    @Test
    public void when_email_is_not_valid_then_error() {
        final PersonBean person = PersonBean.newInstance()
                .setFirstname("Ahmed")
                .setLastname("EL IDRISSI")
                .setPassword("password".toCharArray())
                .setEmail(null);

        IDPException ex = null;

        try {
            validator.validateNewPerson(person);
        } catch (IDPException e) {
            ex = e;
        }

        assertThat(ex, Matchers.notNullValue());
        assertThat(ex.getErrors(), hasSize(1));
        assertThat(ex.getErrors().stream().findFirst().get().getCode(), equalTo("EMAIL_INVALID"));

    }

}
