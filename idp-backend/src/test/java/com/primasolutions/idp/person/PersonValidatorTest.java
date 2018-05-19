package com.primasolutions.idp.person;

import com.primasolutions.idp.constants.RoleCode;
import com.primasolutions.idp.exception.IDPException;
import com.primasolutions.idp.person.mocks.MockPersonLookuper;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import static com.primasolutions.idp.tools.RandomForTests.randomName;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class PersonValidatorTest {


    private PersonValidator validator;
    private PersonBean person;

    @Before
    public void setup() {
        validator = new PersonValidator();
        setField(validator, "personLookuper", MockPersonLookuper.getInstance());
    }

    @Test
    public void when_a_new_person_is_valid_then_no_error() {
        given_valid_person_to_create();
        validator.validateNew(person);
    }

    @Test
    public void when_validate_new_person_and_null_then_error() {
        final IDPException ex = assertThrows(IDPException.class, () -> validator.validateNew(null));
        assertThat(ex, Matchers.notNullValue());
        assertThat(ex.getErrors(), hasSize(1));
        assertThat(ex.getErrors().stream().findFirst().get().getCode(), equalTo("PERSON_IS_NULL"));
    }

    @Test
    public void when_email_is_null_then_error() {
        given_valid_person_to_create();
        given_the_person_email_is_null();
        final IDPException ex = assertThrows(IDPException.class, () -> validator.validateNew(person));
        assertThat(ex, Matchers.notNullValue());
        assertThat(ex.getErrors(), hasSize(1));
        assertThat(ex.getErrors().stream().findFirst().get().getCode(), equalTo("PERSON_EMAIL_IS_NULL"));

    }

    private void given_valid_person_to_create() {
        person = PersonBean.newInstance()
                .setFirstname(randomName().getFirstName())
                .setLastname(randomName().getLastName())
                .setRole(RoleCode.CLIENT)
                .setEmail("ahmed@gmail.com");
    }

    private void given_the_person_email_is_null() {
        person.setEmail(null);
    }

}
