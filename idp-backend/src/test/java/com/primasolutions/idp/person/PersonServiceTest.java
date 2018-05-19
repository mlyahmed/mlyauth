package com.primasolutions.idp.person;

import com.primasolutions.idp.authentication.AuthInfo;
import com.primasolutions.idp.authentication.AuthInfoByLogin;
import com.primasolutions.idp.authentication.mocks.MockAuthInfoByLoginDAO;
import com.primasolutions.idp.authentication.mocks.MockAuthInfoDAO;
import com.primasolutions.idp.authentication.mocks.MockAuthenticationInfoBuilder;
import com.primasolutions.idp.authentication.mocks.MockAuthenticationInfoLookuper;
import com.primasolutions.idp.constants.RoleCode;
import com.primasolutions.idp.exception.IDPException;
import com.primasolutions.idp.person.mocks.MockPersonBuilder;
import com.primasolutions.idp.person.mocks.MockPersonByEmailDAO;
import com.primasolutions.idp.person.mocks.MockPersonDAO;
import com.primasolutions.idp.person.mocks.MockPersonLookuper;
import com.primasolutions.idp.person.mocks.MockPersonSaver;
import com.primasolutions.idp.person.mocks.MockPersonValidator;
import com.primasolutions.idp.tools.MockReseter;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;
import java.util.stream.Stream;

import static com.primasolutions.idp.tools.RandomForTests.randomBirthdate;
import static com.primasolutions.idp.tools.RandomForTests.randomEmail;
import static com.primasolutions.idp.tools.RandomForTests.randomString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.util.ReflectionTestUtils.setField;

class PersonServiceTest {

    private MockPersonValidator personValidator;

    private PersonService personService;

    private PersonBean person;

    @BeforeEach
    void setup() {
        personService = new PersonService();
        personValidator = MockPersonValidator.getInstance();
        setField(personService, "personValidator", personValidator);
        setField(personService, "personSaver", MockPersonSaver.getInstance());
        setField(personService, "personBuilder", MockPersonBuilder.getInstance());
        setField(personService, "authInfoBuilder", MockAuthenticationInfoBuilder.getInstance());
        setField(personService, "personLookuper", MockPersonLookuper.getInstance());
    }

    @AfterEach
    void tearsDown() {
        MockReseter.resetAllMocks();
    }

    @Test
    void when_validation_is_ko_then_error() {
        assertThrows(IDPException.class, () -> personService.createPerson(null));
    }

    @Test
    void when_create_a_new_person_and_not_valid_then_error() {
        personValidator.setForcedError(IDPException.newInstance());
        given_new_person_to_create();
        assertThrows(IDPException.class, this::when_create_new_person);
    }

    @Test
    void when_create_a_new_valid_person_then_save_her() {
        given_new_person_to_create();
        final PersonBean result = when_create_new_person();
        assertThat(result, notNullValue());
        assertThat(MockPersonDAO.getInstance().exists(result.getId()), equalTo(true));
    }

    @Test
    void when_create_a_new_valid_person_then_save_her_authentication() {
        given_new_person_to_create();
        when_create_new_person();

        final Set<AuthInfoByLogin> byLogin = MockAuthInfoByLoginDAO.getInstance()
                .findByLogin(person.getEmail());
        assertThat(byLogin, hasSize(1));
        assertThat(MockAuthInfoDAO.getInstance()
                .findOne(byLogin.iterator().next().getAuthInfoId()), notNullValue());
    }


    @Test
    void when_create_a_new_valid_person_then_save_her_auth_info() {
        given_new_person_to_create();
        when_create_new_person();
        final AuthInfo actual = MockAuthenticationInfoLookuper.getInstance().byLogin((person.getEmail()));
        assertThat(actual, notNullValue());
        assertThat(actual.getLogin(), Matchers.equalTo(person.getEmail()));
    }

    private static Stream<String> emailAddresses() {
        // @formatter:off
        return Stream.of(
                randomEmail(),
                randomEmail(),
                randomEmail(),
                randomEmail()
                );
        // @formatter:on
    }

    @ParameterizedTest
    @MethodSource("emailAddresses")
    void when_create_a_new_person_with_an_already_existing_email_then_error(final String emailAddress) {
        given_an_existing_person_with_email(emailAddress);
        given_new_person_to_create_with_email(emailAddress);
        assertThrows(IDPException.class, this::when_create_new_person);
    }

    private void given_an_existing_person_with_email(final String emailAddress) {
        final Person p = Person.newInstance().setExternalId(randomString()).setEmail(emailAddress);
        final PersonByEmail pbe = PersonByEmail.newInstance().setPersonId(p.getExternalId()).setEmail(p.getEmail());
        MockPersonDAO.getInstance().save(p);
        MockPersonByEmailDAO.getInstance().save(pbe);
    }

    private void given_new_person_to_create_with_email(final String emailAddress) {
        given_new_person_to_create();
        person.setEmail(emailAddress);
    }

    private void given_new_person_to_create() {
        person = PersonBean.newInstance()
                .setExternalId(randomString())
                .setFirstname(randomString())
                .setLastname(randomString())
                .setBirthdate(randomBirthdate())
                .setRole(RoleCode.CLIENT.getValue())
                .setEmail(randomEmail());
    }

    private PersonBean when_create_new_person() {
        return personService.createPerson(person);
    }
}
