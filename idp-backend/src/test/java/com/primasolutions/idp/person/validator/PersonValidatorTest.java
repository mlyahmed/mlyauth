package com.primasolutions.idp.person.validator;

import com.primasolutions.idp.constants.RoleCode;
import com.primasolutions.idp.exception.IDPException;
import com.primasolutions.idp.person.Person;
import com.primasolutions.idp.person.PersonBean;
import com.primasolutions.idp.person.PersonByEmail;
import com.primasolutions.idp.person.mocks.MockPersonByEmailDAO;
import com.primasolutions.idp.person.mocks.MockPersonDAO;
import com.primasolutions.idp.person.mocks.MockPersonLookuper;
import com.primasolutions.idp.tools.MockReseter;
import org.apache.commons.lang.RandomStringUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
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
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
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
        given_the_person_is_valid();
    }

    @AfterEach
    void tearsDown() {
        MockReseter.resetAllMocks();
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

    @ParameterizedTest
    @MethodSource("emptyStrings")
    void when_validate_new_and_email_address_is_empty_then_error(final String email) {
        and_the_person_email_address_is(email);
        final IDPException ex = assertThrows(IDPException.class, this::when_validate_the_new);
        assertThat(ex.getErrors(), hasSize(1));
        assertThat(ex.getErrorCodes(), contains("EMAIL_EMPTY"));

    }

    @Test
    void when_validate_new_and_email_address_is_not_valid_then_error() {
        and_the_person_email_address_is(randomString());
        final IDPException ex = assertThrows(IDPException.class, this::when_validate_the_new);
        assertThat(ex.getErrors(), hasSize(1));
        assertThat(ex.getErrorCodes(), contains("EMAIL_INVALID"));
    }

    @ParameterizedTest
    @MethodSource("tooLongEmails")
    void when_validate_new_and_email_address_is_too_long_then_error(final String tooLong) {
        and_the_person_email_address_is(tooLong);
        final IDPException ex = assertThrows(IDPException.class, this::when_validate_the_new);
        assertThat(ex.getErrors(), hasSize(1));
        assertThat(ex.getErrorCodes(), contains("EMAIL_TOO_LONG"));
    }

    @Test
    void when_validate_new_and_email_address_already_exists_then_error() {
        given_the_person_with_already_existing_email();
        final IDPException ex = assertThrows(IDPException.class, this::when_validate_the_new);
        assertThat(ex.getErrors(), hasSize(1));
        assertThat(ex.getErrorCodes(), contains("EMAIL_ALREADY_EXISTS"));
    }

    @ParameterizedTest
    @MethodSource("emptyStrings")
    void when_validate_new_and_external_id_is_empty_then_error(final String externalId) {
        and_the_person_external_id_is(externalId);
        final IDPException ex = assertThrows(IDPException.class, this::when_validate_the_new);
        assertThat(ex.getErrors(), hasSize(1));
        assertThat(ex.getErrorCodes(), contains("EXTERNAL_ID_EMPTY"));
    }

    @ParameterizedTest
    @MethodSource("tooLongExternalIds")
    void when_validate_new_and_external_id_is_too_long_then_error(final String tooLong) {
        and_the_person_external_id_is(tooLong);
        final IDPException ex = assertThrows(IDPException.class, this::when_validate_the_new);
        assertThat(ex.getErrors(), hasSize(1));
        assertThat(ex.getErrorCodes(), contains("EXTERNAL_ID_TOO_LONG"));
    }

    @Test
    void when_validate_new_and_external_id_already_exists_then_error() {
        final String externalId = randomString();
        given_an_existing_person_with_external_id(externalId);
        and_the_person_external_id_is(externalId);
        final IDPException ex = assertThrows(IDPException.class, this::when_validate_the_new);
        assertThat(ex.getErrors(), hasSize(1));
        assertThat(ex.getErrorCodes(), contains("EXTERNAL_ID_ALREADY_EXISTS"));
    }

    @ParameterizedTest
    @MethodSource("emptyStrings")
    void when_validate_new_and_first_name_is_empty_then_error(final String empty) {
        and_the_person_first_name_is(empty);
        final IDPException ex = assertThrows(IDPException.class, this::when_validate_the_new);
        assertThat(ex.getErrors(), hasSize(1));
        assertThat(ex.getErrorCodes(), contains("FIRST_NAME_EMPTY"));
    }

    @ParameterizedTest
    @MethodSource("tooLongFirstNames")
    void when_validate_new_and_first_name_is_too_long_then_error(final String tooLong) {
        and_the_person_first_name_is(tooLong);
        final IDPException ex = assertThrows(IDPException.class, this::when_validate_the_new);
        assertThat(ex.getErrors(), hasSize(1));
        assertThat(ex.getErrorCodes(), contains("FIRST_NAME_TOO_LONG"));
    }

    @ParameterizedTest
    @MethodSource("emptyStrings")
    void when_validate_new_and_last_name_is_empty_then_error(final String empty) {
        and_the_person_last_name_is(empty);
        final IDPException ex = assertThrows(IDPException.class, this::when_validate_the_new);
        assertThat(ex.getErrors(), hasSize(1));
        assertThat(ex.getErrorCodes(), contains("LAST_NAME_EMPTY"));
    }

    @ParameterizedTest
    @MethodSource("tooLongLastNames")
    void when_validate_new_and_last_name_is_too_long_then_error(final String tooLong) {
        and_the_person_last_name_is(tooLong);
        final IDPException ex = assertThrows(IDPException.class, this::when_validate_the_new);
        assertThat(ex.getErrors(), hasSize(1));
        assertThat(ex.getErrorCodes(), contains("LAST_NAME_TOO_LONG"));
    }

    @ParameterizedTest
    @MethodSource("emptyStrings")
    void when_validate_new_and_birth_date_is_empty_then_ok(final String empty) {
        and_the_person_birth_date_is(empty);
        when_validate_the_new();
    }

    @ParameterizedTest
    @MethodSource("badFormattedBirthDates")
    void when_validate_new_and_birth_date_format_is_bad_then_error(final String badFormatted) {
        and_the_person_birth_date_is(badFormatted);
        final IDPException ex = assertThrows(IDPException.class, this::when_validate_the_new);
        assertThat(ex.getErrors(), hasSize(1));
        assertThat(ex.getErrorCodes(), Matchers.contains("BIRTH_DATE_BAD_FORMAT"));
    }


    @ParameterizedTest
    @MethodSource("emptyStrings")
    void when_validate_new_and_role_code_is_empty_then_error(final String empty) {
        and_the_person_role_is(empty);
        final IDPException ex = assertThrows(IDPException.class, this::when_validate_the_new);
        assertThat(ex.getErrors(), hasSize(1));
        assertThat(ex.getErrorCodes(), contains("ROLE_EMPTY"));
    }

    @Test
    void when_validate_new_and_role_code_is_bad_then_error() {
        and_the_person_role_is(randomString());
        final IDPException ex = assertThrows(IDPException.class, this::when_validate_the_new);
        assertThat(ex.getErrors(), hasSize(1));
        assertThat(ex.getErrorCodes(), contains("ROLE_INVALID"));
    }

    @Test
    void when_validate_update_and_valid_then_ok() {
        given_the_person_already_exists();
        when_validate_the_update();
    }

    @Test
    void when_validate_update_the_same_existing_person_then_ok() {
        final Person p = given_an_existing_person();
        this.person.setExternalId(p.getExternalId()).setEmail(p.getEmail());
        when_validate_the_update();
    }

    @Test
    void when_validate_update_person_and_null_then_error() {
        final IDPException ex = assertThrows(IDPException.class, () -> validator.validateUpdate(null));
        assertThat(ex.getErrors(), hasSize(1));
        assertThat(ex.getErrors().stream().findFirst().get().getCode(), equalTo("PERSON_NULL"));
    }

    @ParameterizedTest
    @MethodSource("emptyStrings")
    void when_validate_update_and_email_address_is_empty_then_ok(final String email) {
        given_the_person_already_exists();
        and_the_person_email_address_is(email);
        when_validate_the_update();
    }

    @Test
    void when_validate_update_and_email_address_is_not_valid_then_error() {
        given_the_person_already_exists();
        and_the_person_email_address_is(randomString());
        final IDPException ex = assertThrows(IDPException.class, this::when_validate_the_update);
        assertThat(ex.getErrors(), hasSize(1));
        assertThat(ex.getErrorCodes(), contains("EMAIL_INVALID"));
    }

    @ParameterizedTest
    @MethodSource("tooLongEmails")
    void when_validate_update_and_email_address_is_too_long_then_error(final String tooLongEmail) {
        given_the_person_already_exists();
        and_the_person_email_address_is(tooLongEmail);
        final IDPException ex = assertThrows(IDPException.class, this::when_validate_the_update);
        assertThat(ex.getErrors(), hasSize(1));
        assertThat(ex.getErrorCodes(), contains("EMAIL_TOO_LONG"));
    }

    @Test
    void when_validate_update_and_email_address_already_exists_then_error() {
        given_the_person_already_exists();
        given_the_person_with_already_existing_email();
        final IDPException ex = assertThrows(IDPException.class, this::when_validate_the_update);
        assertThat(ex.getErrors(), hasSize(1));
        assertThat(ex.getErrorCodes(), contains("EMAIL_ALREADY_EXISTS"));
    }

    @ParameterizedTest
    @MethodSource("emptyStrings")
    void when_validate_update_and_external_id_is_empty_then_error(final String externalId) {
        and_the_person_external_id_is(externalId);
        final IDPException ex = assertThrows(IDPException.class, this::when_validate_the_update);
        assertThat(ex.getErrors(), hasSize(1));
        assertThat(ex.getErrorCodes(), contains("EXTERNAL_ID_EMPTY"));
    }

    @Test
    void when_validate_update_and_the_person_does_not_exist_then_error() {
        final IDPException ex = assertThrows(IDPException.class, this::when_validate_the_update);
        assertThat(ex.getErrors(), hasSize(1));
        assertThat(ex.getErrorCodes(), contains("PERSON_NOT_FOUND"));
    }

    @ParameterizedTest
    @MethodSource("emptyStrings")
    void when_validate_update_and_first_name_is_empty_then_ok(final String empty) {
        given_the_person_already_exists();
        and_the_person_first_name_is(empty);
        when_validate_the_update();
    }

    @ParameterizedTest
    @MethodSource("tooLongFirstNames")
    void when_validate_update_and_first_name_is_too_long_then_error(final String tooLong) {
        given_the_person_already_exists();
        and_the_person_first_name_is(tooLong);
        final IDPException ex = assertThrows(IDPException.class, this::when_validate_the_update);
        assertThat(ex.getErrors(), hasSize(1));
        assertThat(ex.getErrorCodes(), contains("FIRST_NAME_TOO_LONG"));
    }

    @ParameterizedTest
    @MethodSource("emptyStrings")
    void when_validate_update_and_last_name_is_empty_then_ok(final String empty) {
        given_the_person_already_exists();
        and_the_person_last_name_is(empty);
        when_validate_the_update();
    }

    @ParameterizedTest
    @MethodSource("tooLongLastNames")
    void when_validate_update_and_last_name_is_too_long_then_error(final String tooLong) {
        given_the_person_already_exists();
        and_the_person_last_name_is(tooLong);
        final IDPException ex = assertThrows(IDPException.class, this::when_validate_the_update);
        assertThat(ex.getErrors(), hasSize(1));
        assertThat(ex.getErrorCodes(), contains("LAST_NAME_TOO_LONG"));
    }

    @ParameterizedTest
    @MethodSource("emptyStrings")
    void when_validate_date_and_birth_date_is_empty_then_ok(final String empty) {
        given_the_person_already_exists();
        and_the_person_birth_date_is(empty);
        when_validate_the_update();
    }

    @ParameterizedTest
    @MethodSource("badFormattedBirthDates")
    void when_validate_update_and_birth_date_format_is_bad_then_error(final String badFormatted) {
        given_the_person_already_exists();
        and_the_person_birth_date_is(badFormatted);
        final IDPException ex = assertThrows(IDPException.class, this::when_validate_the_update);
        assertThat(ex.getErrors(), hasSize(1));
        assertThat(ex.getErrorCodes(), Matchers.contains("BIRTH_DATE_BAD_FORMAT"));
    }

    @ParameterizedTest
    @MethodSource("emptyStrings")
    void when_validate_update_and_role_code_is_empty_then_ok(final String empty) {
        given_the_person_already_exists();
        and_the_person_role_is(empty);
        when_validate_the_update();
    }

    @Test
    void when_validate_update_and_role_code_is_bad_then_error() {
        given_the_person_already_exists();
        and_the_person_role_is(randomString());
        final IDPException ex = assertThrows(IDPException.class, this::when_validate_the_update);
        assertThat(ex.getErrors(), hasSize(1));
        assertThat(ex.getErrorCodes(), contains("ROLE_INVALID"));
    }

    private static Stream<String> emptyStrings() {
        return Stream.of(null, "");
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

    private static Stream<String> tooLongExternalIds() {
        return Stream.of(
                //CHECKSTYLE:OFF
                RandomStringUtils.random(101, true, true),
                RandomStringUtils.random(102, true, true),
                RandomStringUtils.random(230, true, true)
                //CHECKSTYLE:ON
        );
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

    private static Stream<String> tooLongLastNames() {
        return Stream.of(
                //CHECKSTYLE:OFF
                RandomStringUtils.random(101, true, true),
                RandomStringUtils.random(105, true, true),
                RandomStringUtils.random(501, true, true)
                //CHECKSTYLE:ON
        );
    }

    private static Stream<String> badFormattedBirthDates() {
        return Stream.of(
                randomString(),
                "19841112",
                "891009"
        );
    }

    private void given_the_person_already_exists() {
        final Person p = given_an_existing_person();
        person.setExternalId(p.getExternalId());
    }

    private void given_an_existing_person_with_email(final String emailAddress) {
        final Person p = Person.newInstance().setExternalId(randomString()).setEmail(emailAddress);
        final PersonByEmail pbe = PersonByEmail.newInstance().setPersonId(p.getExternalId()).setEmail(p.getEmail());
        MockPersonDAO.getInstance().save(p);
        MockPersonByEmailDAO.getInstance().save(pbe);
    }

    private void and_the_person_email_address_is(final String email) {
        person.setEmail(email);
    }

    private void given_the_person_with_already_existing_email() {
        final String emailAddress = randomEmail();
        given_an_existing_person_with_email(emailAddress);
        and_the_person_email_address_is(emailAddress);
    }

    private Person given_an_existing_person() {
        final Person p = Person.newInstance().setExternalId(randomString()).setEmail(randomEmail());
        final PersonByEmail pbe = PersonByEmail.newInstance().setPersonId(p.getExternalId()).setEmail(p.getEmail());
        MockPersonDAO.getInstance().save(p);
        MockPersonByEmailDAO.getInstance().save(pbe);
        return p;
    }

    private void and_the_person_external_id_is(final String tooLongExternalId) {
        person.setExternalId(tooLongExternalId);
    }

    private void given_an_existing_person_with_external_id(final String externalId) {
        final Person p = Person.newInstance().setExternalId(externalId).setEmail(randomEmail());
        final PersonByEmail pbe = PersonByEmail.newInstance().setPersonId(p.getExternalId()).setEmail(p.getEmail());
        MockPersonDAO.getInstance().save(p);
        MockPersonByEmailDAO.getInstance().save(pbe);
    }

    private void and_the_person_first_name_is(final String firstName) {
        person.setFirstname(firstName);
    }

    private void and_the_person_last_name_is(final String empty) {
        person.setLastname(empty);
    }

    private void and_the_person_birth_date_is(final String empty) {
        person.setBirthdate(empty);
    }

    private void and_the_person_role_is(final String empty) {
        person.setRole(empty);
    }

    private void given_the_person_is_valid() {
        person = PersonBean.newInstance()
                .setExternalId(randomString())
                .setFirstname(randomName().getFirstName())
                .setLastname(randomName().getLastName())
                .setBirthdate(randomBirthdate())
                .setRole(RoleCode.CLIENT.getValue())
                .setEmail(randomEmail());
    }

    private void when_validate_the_new() {
        validator.validateNew(person);
    }

    private void when_validate_the_update() {
        validator.validateUpdate(person);
    }

}
