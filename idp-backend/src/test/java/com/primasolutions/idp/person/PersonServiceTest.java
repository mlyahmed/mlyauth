package com.primasolutions.idp.person;

import com.primasolutions.idp.authentication.AuthInfo;
import com.primasolutions.idp.authentication.AuthInfoByLogin;
import com.primasolutions.idp.authentication.mocks.MockAuthInfoByLoginDAO;
import com.primasolutions.idp.authentication.mocks.MockAuthInfoDAO;
import com.primasolutions.idp.authentication.mocks.MockAuthenticationInfoBuilder;
import com.primasolutions.idp.constants.AuthInfoStatus;
import com.primasolutions.idp.constants.RoleCode;
import com.primasolutions.idp.exception.IDPException;
import com.primasolutions.idp.person.mocks.MockPersonBuilder;
import com.primasolutions.idp.person.mocks.MockPersonByEmailDAO;
import com.primasolutions.idp.person.mocks.MockPersonDAO;
import com.primasolutions.idp.person.mocks.MockPersonLookuper;
import com.primasolutions.idp.person.mocks.MockPersonSaver;
import com.primasolutions.idp.person.mocks.MockPersonValidator;
import com.primasolutions.idp.tools.MockReseter;
import org.exparity.hamcrest.date.DateMatchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.text.ParseException;
import java.util.Date;
import java.util.Set;
import java.util.stream.Stream;

import static com.primasolutions.idp.person.PersonBuilder.DATE_FORMAT;
import static com.primasolutions.idp.tools.RandomForTests.randomBirthdate;
import static com.primasolutions.idp.tools.RandomForTests.randomEmail;
import static com.primasolutions.idp.tools.RandomForTests.randomString;
import static org.apache.commons.lang.time.DateUtils.parseDate;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.util.ReflectionTestUtils.setField;

class PersonServiceTest {

    private  static final int ONE_SECOND = 1000;

    private MockPersonDAO personDAO;

    private MockPersonByEmailDAO personByEmailDAO;

    private MockAuthInfoDAO authInfoDAO;

    private MockAuthInfoByLoginDAO authInfoByLoginDAO;

    private MockPersonValidator personValidator;

    private PersonService personService;

    private PersonBean person;

    @BeforeEach
    void setup() {
        personDAO = MockPersonDAO.getInstance();
        personByEmailDAO = MockPersonByEmailDAO.getInstance();
        authInfoDAO = MockAuthInfoDAO.getInstance();
        authInfoByLoginDAO = MockAuthInfoByLoginDAO.getInstance();
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
    void when_create_null_then_error() {
        assertThrows(IDPException.class, () -> personService.createPerson(null));
    }

    @Test
    void when_create_a_new_person_and_validation_ko_then_error() {
        personValidator.setForcedError(IDPException.newInstance());
        given_new_person_to_create();
        assertThrows(IDPException.class, this::when_create_new_person);
    }

    @Test
    void when_create_a_new_valid_person_then_return_valid_result() {
        given_new_person_to_create();
        final PersonBean result = when_create_new_person();
        assertThat(result, notNullValue());
    }

    @Test
    void when_create_a_new_valid_person_then_save_her() throws ParseException {
        given_new_person_to_create();
        when_create_new_person();

        final Set<PersonByEmail> byEmail = personByEmailDAO.findByEmail(person.getEmail());
        assertThat(byEmail, hasSize(1));

        final Person p = personDAO.findByExternalId(byEmail.iterator().next().getPersonId());
        assertThat(p, notNullValue());
        assertThat(p.getExternalId(), equalTo(person.getExternalId()));
        assertThat(p.getEmail(), equalTo(person.getEmail()));
        assertThat(p.getFirstname(), equalTo(person.getFirstname()));
        assertThat(p.getLastname(), equalTo(person.getLastname()));
        assertThat(p.getBirthdate(), equalTo(parseDate(person.getBirthdate(), new String[]{DATE_FORMAT})));
        assertThat(p.getRole(), notNullValue());
        assertThat(p.getRole().getCode().getValue(), equalTo(person.getRole()));
    }

    @Test
    void when_create_a_new_valid_person_then_save_her_authentication() {
        given_new_person_to_create();
        when_create_new_person();

        final Set<AuthInfoByLogin> byLogin = authInfoByLoginDAO.findByLogin(person.getEmail());
        assertThat(byLogin, hasSize(1));

        final AuthInfo authInfo = authInfoDAO.findOne(byLogin.iterator().next().getAuthInfoId());
        assertThat(authInfo, notNullValue());
        assertThat(authInfo.getLogin(), equalTo(person.getEmail()));
        assertThat(authInfo.getEffectiveAt(), DateMatchers.before(new Date(System.currentTimeMillis() + ONE_SECOND)));
        assertThat(authInfo.getStatus(), equalTo(AuthInfoStatus.ACTIVE));
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

    @Test
    void when_update_null_then_error() {
        assertThrows(IDPException.class, () -> personService.updatePerson(null));
    }


    private void given_an_existing_person_with_email(final String emailAddress) {
        final Person p = Person.newInstance().setExternalId(randomString()).setEmail(emailAddress);
        final PersonByEmail pbe = PersonByEmail.newInstance().setPersonId(p.getExternalId()).setEmail(p.getEmail());
        personDAO.save(p);
        personByEmailDAO.save(pbe);
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
