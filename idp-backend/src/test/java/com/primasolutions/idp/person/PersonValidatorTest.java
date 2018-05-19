package com.primasolutions.idp.person;

import com.primasolutions.idp.constants.RoleCode;
import com.primasolutions.idp.exception.IDPException;
import com.primasolutions.idp.person.mocks.MockPersonLookuper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.primasolutions.idp.tools.RandomForTests.randomEmail;
import static com.primasolutions.idp.tools.RandomForTests.randomName;
import static com.primasolutions.idp.tools.RandomForTests.randomString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.util.ReflectionTestUtils.setField;

class PersonValidatorTest {


    private PersonValidator validator;
    private PersonBean person;

    @BeforeEach
    void setup() {
        validator = new PersonValidator();
        setField(validator, "personLookuper", MockPersonLookuper.getInstance());
    }

    @Test
    void when_a_new_person_is_valid_then_no_error() {
        given_valid_person_to_create();
        validator.validateNew(person);
    }

    @Test
    void when_validate_new_person_and_null_then_error() {
        final IDPException ex = assertThrows(IDPException.class, () -> validator.validateNew(null));
        assertThat(ex, notNullValue());
        assertThat(ex.getErrors(), hasSize(1));
        assertThat(ex.getErrors().stream().findFirst().get().getCode(), equalTo("PERSON_IS_NULL"));
    }

    private static Stream<String> emptyEmails() {
        return Stream.of(null, "");
    }

    @ParameterizedTest
    @MethodSource("emptyEmails")
    void when_email_address_is_empty_then_error(final String email) {
        given_valid_person_to_create();
        and_email_address_is(email);
        final IDPException ex = assertThrows(IDPException.class, () -> validator.validateNew(person));
        assertThat(ex, notNullValue());
        assertThat(ex.getErrors(), hasSize(1));
        assertThat(ex.getErrors().stream().findFirst().get().getCode(), equalTo("PERSON_EMAIL_IS_EMPTY"));

    }

    @Test
    void when_email_address_is_not_valid_then_error() {
        given_valid_person_to_create();
        and_email_address_is(randomString());
        final IDPException ex = assertThrows(IDPException.class, () -> validator.validateNew(person));
        assertThat(ex, notNullValue());
        assertThat(ex.getErrors(), hasSize(1));
        assertThat(ex.getErrors().stream().findFirst().get().getCode(), equalTo("PERSON_EMAIL_IS_INVALID"));
    }

    private void and_email_address_is(final String email) {
        person.setEmail(email);
    }

    private void given_valid_person_to_create() {
        person = PersonBean.newInstance()
                .setFirstname(randomName().getFirstName())
                .setLastname(randomName().getLastName())
                .setRole(RoleCode.CLIENT)
                .setEmail(randomEmail());
    }

}
