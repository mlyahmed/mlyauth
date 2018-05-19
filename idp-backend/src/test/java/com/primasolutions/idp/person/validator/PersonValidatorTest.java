package com.primasolutions.idp.person.validator;

import com.primasolutions.idp.constants.RoleCode;
import com.primasolutions.idp.exception.IDPException;
import com.primasolutions.idp.person.Person;
import com.primasolutions.idp.person.PersonBean;
import com.primasolutions.idp.person.PersonByEmail;
import com.primasolutions.idp.person.mocks.MockPersonByEmailDAO;
import com.primasolutions.idp.person.mocks.MockPersonDAO;
import com.primasolutions.idp.person.mocks.MockPersonLookuper;
import org.apache.commons.lang.RandomStringUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.primasolutions.idp.tools.RandomForTests.randomBirthdate;
import static com.primasolutions.idp.tools.RandomForTests.randomEmail;
import static com.primasolutions.idp.tools.RandomForTests.randomName;
import static com.primasolutions.idp.tools.RandomForTests.randomString;
import static org.apache.commons.lang.StringUtils.leftPad;
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
        given_valid_person_to_create();
    }

    @Test
    void when_a_new_person_is_valid_then_no_error() {
        validator.validateNew(person);
    }

    @Test
    void when_validate_new_person_and_null_then_error() {
        final IDPException ex = assertThrows(IDPException.class, () -> validator.validateNew(null));
        assertThat(ex.getErrors(), hasSize(1));
        assertThat(ex.getErrors().stream().findFirst().get().getCode(), equalTo("PERSON_NULL"));
    }

    private static Stream<String> emptyStrings() {
        return Stream.of(null, "");
    }

    @ParameterizedTest
    @MethodSource("emptyStrings")
    void when_email_address_is_empty_then_error(final String email) {
        and_email_address_is(email);
        final IDPException ex = assertThrows(IDPException.class, () -> validator.validateNew(person));
        assertThat(ex.getErrors(), hasSize(1));
        assertThat(ex.getErrors().stream().findFirst().get().getCode(), equalTo("EMAIL_EMPTY"));

    }

    @Test
    void when_email_address_is_not_valid_then_error() {
        and_email_address_is(randomString());
        final IDPException ex = assertThrows(IDPException.class, () -> validator.validateNew(person));
        assertThat(ex.getErrors(), hasSize(1));
        assertThat(ex.getErrors().stream().findFirst().get().getCode(), equalTo("EMAIL_INVALID"));
    }

    private static Stream<String> tooLongEmails() {
        return Stream.of(
                //CHECKSTYLE:OFF
                leftPad("@yahoo.fr", 65, randomString().charAt(0)),
                leftPad("@hotmail.com", 67, randomString().charAt(0)),
                leftPad("@gmail.com", 100, randomString().charAt(0))
                //CHECKSTYLE:ON
        );
    }

    @ParameterizedTest
    @MethodSource("tooLongEmails")
    void when_email_address_is_too_long_then_error(final String tooLongEmail) {
        and_email_address_is(tooLongEmail);
        final IDPException ex = assertThrows(IDPException.class, () -> validator.validateNew(person));
        assertThat(ex.getErrors(), hasSize(1));
        assertThat(ex.getErrors().stream().findFirst().get().getCode(), equalTo("EMAIL_TOO_LONG"));
    }

    @Test
    void when_email_address_already_exists_then_error() {
        final String emailAddress = randomEmail();
        given_an_existing_person_with_email(emailAddress);
        and_email_address_is(emailAddress);
        final IDPException ex = assertThrows(IDPException.class, () -> validator.validateNew(person));
        assertThat(ex.getErrors(), hasSize(1));
        assertThat(ex.getErrors().stream().findFirst().get().getCode(), equalTo("EMAIL_ALREADY_EXISTS"));
    }

    @ParameterizedTest
    @MethodSource("emptyStrings")
    void when_external_id_is_empty_then_error(final String externalId) {
        and_external_id_is(externalId);
        final IDPException ex = assertThrows(IDPException.class, () -> validator.validateNew(person));
        assertThat(ex.getErrors(), hasSize(1));
        assertThat(ex.getErrors().stream().findFirst().get().getCode(), equalTo("EXTERNAL_ID_EMPTY"));

    }

    private static Stream<String> tooLongExternalIds() {
        return Stream.of(
                //CHECKSTYLE:OFF
                RandomStringUtils.random(101, true, true),
                RandomStringUtils.random(102, true, true),
                RandomStringUtils.random(230, true, true)
                //CHECKSTYLE:ON
        );
    }

    @ParameterizedTest
    @MethodSource("tooLongExternalIds")
    void when_external_id_is_too_long_then_error(final String tooLongExternalId) {
        and_external_id_is(tooLongExternalId);
        final IDPException ex = assertThrows(IDPException.class, () -> validator.validateNew(person));
        assertThat(ex.getErrors(), hasSize(1));
        assertThat(ex.getErrors().stream().findFirst().get().getCode(), equalTo("EXTERNAL_ID_TOO_LONG"));
    }

    @Test
    void when_external_id_already_exists_then_error() {
        final String externalId = randomString();
        given_an_existing_person_with_external_id(externalId);
        and_external_id_is(externalId);
        final IDPException ex = assertThrows(IDPException.class, () -> validator.validateNew(person));
        assertThat(ex.getErrors(), hasSize(1));
        assertThat(ex.getErrors().stream().findFirst().get().getCode(), equalTo("EXTERNAL_ID_ALREADY_EXISTS"));
    }

    @ParameterizedTest
    @MethodSource("emptyStrings")
    void when_first_name_is_empty_then_error(final String empty) {
        and_first_name_is(empty);
        final IDPException ex = assertThrows(IDPException.class, () -> validator.validateNew(person));
        assertThat(ex.getErrors(), hasSize(1));
        assertThat(ex.getErrors().stream().findFirst().get().getCode(), equalTo("FIRST_NAME_EMPTY"));
    }

    private static Stream<String> tooLongFirstNames() {
        return Stream.of(
                //CHECKSTYLE:OFF
                RandomStringUtils.random(101, true, true),
                RandomStringUtils.random(301, true, true),
                RandomStringUtils.random(201, true, true)
                //CHECKSTYLE:ON
        );
    }

    @ParameterizedTest
    @MethodSource("tooLongFirstNames")
    void when_first_name_is_too_long_then_error(final String tooLongFirstName) {
        and_first_name_is(tooLongFirstName);
        final IDPException ex = assertThrows(IDPException.class, () -> validator.validateNew(person));
        assertThat(ex.getErrors(), hasSize(1));
        assertThat(ex.getErrors().stream().findFirst().get().getCode(), equalTo("FIRST_NAME_TOO_LONG"));
    }

    @ParameterizedTest
    @MethodSource("emptyStrings")
    void when_last_name_is_empty_then_error(final String empty) {
        person.setLastname(empty);
        final IDPException ex = assertThrows(IDPException.class, () -> validator.validateNew(person));
        assertThat(ex.getErrors(), hasSize(1));
        assertThat(ex.getErrors().stream().findFirst().get().getCode(), equalTo("LAST_NAME_EMPTY"));
    }

    private static Stream<String> tooLongLastNames() {
        return Stream.of(
                //CHECKSTYLE:OFF
                RandomStringUtils.random(101, true, true),
                RandomStringUtils.random(105, true, true),
                RandomStringUtils.random(501, true, true)
                //CHECKSTYLE:ON
        );
    }

    @ParameterizedTest
    @MethodSource("tooLongLastNames")
    void when_last_name_is_too_long_then_error(final String tooLongLastName) {
        person.setLastname(tooLongLastName);
        final IDPException ex = assertThrows(IDPException.class, () -> validator.validateNew(person));
        assertThat(ex.getErrors(), hasSize(1));
        assertThat(ex.getErrors().stream().findFirst().get().getCode(), equalTo("LAST_NAME_TOO_LONG"));
    }

    @ParameterizedTest
    @MethodSource("emptyStrings")
    void the_birth_date_is_optional(final String empty) {
        person.setBirthdate(empty);
        validator.validateNew(person);
    }

    private static Stream<String> badFormattedBirthDates() {
        return Stream.of(
                randomString(),
                "19841112",
                "891009"
        );
    }

    @ParameterizedTest
    @MethodSource("badFormattedBirthDates")
    void when_birth_date_format_is_bad_then_error(final String badFormattedBirthDate) {
        person.setBirthdate(badFormattedBirthDate);
        final IDPException ex = assertThrows(IDPException.class, () -> validator.validateNew(person));
        assertThat(ex, notNullValue());
        assertThat(ex.getErrors(), hasSize(1));
        assertThat(ex.getErrorCodes(), Matchers.contains("BIRTH_DATE_BAD_FORMAT"));
    }


    @ParameterizedTest
    @MethodSource("emptyStrings")
    void when_role_code_is_empty_then_error(final String empty) {
        person.setRole(empty);
        final IDPException ex = assertThrows(IDPException.class, () -> validator.validateNew(person));
        assertThat(ex.getErrors(), hasSize(1));
        assertThat(ex.getErrors().stream().findFirst().get().getCode(), equalTo("ROLE_EMPTY"));
    }

    @Test
    void when_role_code_is_bad_then_error() {
        person.setRole(randomString());
        final IDPException ex = assertThrows(IDPException.class, () -> validator.validateNew(person));
        assertThat(ex.getErrors(), hasSize(1));
        assertThat(ex.getErrors().stream().findFirst().get().getCode(), equalTo("ROLE_INVALID"));
    }


    private void and_email_address_is(final String email) {
        person.setEmail(email);
    }

    private void given_an_existing_person_with_email(final String emailAddress) {
        final Person p = Person.newInstance().setExternalId(randomString()).setEmail(emailAddress);
        final PersonByEmail pbe = PersonByEmail.newInstance().setPersonId(p.getExternalId()).setEmail(p.getEmail());
        MockPersonDAO.getInstance().save(p);
        MockPersonByEmailDAO.getInstance().save(pbe);
    }

    private void and_external_id_is(final String tooLongExternalId) {
        person.setExternalId(tooLongExternalId);
    }

    private void given_an_existing_person_with_external_id(final String externalId) {
        final Person p = Person.newInstance().setExternalId(externalId).setEmail(randomEmail());
        final PersonByEmail pbe = PersonByEmail.newInstance().setPersonId(p.getExternalId()).setEmail(p.getEmail());
        MockPersonDAO.getInstance().save(p);
        MockPersonByEmailDAO.getInstance().save(pbe);
    }

    private void and_first_name_is(final String firstName) {
        person.setFirstname(firstName);
    }

    private void given_valid_person_to_create() {
        person = PersonBean.newInstance()
                .setExternalId(randomString())
                .setFirstname(randomName().getFirstName())
                .setLastname(randomName().getLastName())
                .setBirthdate(randomBirthdate())
                .setRole(RoleCode.CLIENT.getValue())
                .setEmail(randomEmail());
    }

}
