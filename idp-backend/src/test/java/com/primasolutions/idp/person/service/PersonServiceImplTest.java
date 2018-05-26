package com.primasolutions.idp.person.service;

import com.primasolutions.idp.application.Application;
import com.primasolutions.idp.application.mocks.MockApplicationDAO;
import com.primasolutions.idp.application.mocks.MockApplicationLookuper;
import com.primasolutions.idp.authentication.AuthInfo;
import com.primasolutions.idp.authentication.AuthInfoByLogin;
import com.primasolutions.idp.authentication.Role;
import com.primasolutions.idp.authentication.mocks.MockAuthInfoByLoginDAO;
import com.primasolutions.idp.authentication.mocks.MockAuthInfoDAO;
import com.primasolutions.idp.authentication.mocks.MockAuthenticationInfoBuilder;
import com.primasolutions.idp.constants.AuthInfoStatus;
import com.primasolutions.idp.constants.RoleCode;
import com.primasolutions.idp.exception.ApplicationNotFoundExc;
import com.primasolutions.idp.exception.IDPException;
import com.primasolutions.idp.exception.PersonNotFoundExc;
import com.primasolutions.idp.person.mapper.PersonMapperImpl;
import com.primasolutions.idp.person.mocks.MockPersonByEmailDAO;
import com.primasolutions.idp.person.mocks.MockPersonDAO;
import com.primasolutions.idp.person.mocks.MockPersonLookuper;
import com.primasolutions.idp.person.mocks.MockPersonMapper;
import com.primasolutions.idp.person.mocks.MockPersonSaver;
import com.primasolutions.idp.person.mocks.MockPersonValidator;
import com.primasolutions.idp.person.model.Person;
import com.primasolutions.idp.person.model.PersonBean;
import com.primasolutions.idp.person.model.PersonByEmail;
import com.primasolutions.idp.tools.MockReseter;
import org.apache.commons.lang.time.DateUtils;
import org.exparity.hamcrest.date.DateMatchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.text.ParseException;
import java.util.Date;
import java.util.Set;
import java.util.stream.Stream;

import static com.primasolutions.idp.person.mapper.PersonMapperImpl.DATE_FORMAT;
import static com.primasolutions.idp.tools.RandomForTests.randomBirthdate;
import static com.primasolutions.idp.tools.RandomForTests.randomEmail;
import static com.primasolutions.idp.tools.RandomForTests.randomString;
import static org.apache.commons.lang.time.DateUtils.parseDate;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.util.ReflectionTestUtils.setField;

class PersonServiceImplTest {

    private  static final int ONE_SECOND = 1000;

    private MockApplicationDAO applicationDAO;

    private MockPersonDAO personDAO;

    private MockPersonByEmailDAO personByEmailDAO;

    private MockAuthInfoDAO authInfoDAO;

    private MockAuthInfoByLoginDAO authInfoByLoginDAO;

    private MockPersonValidator personValidator;

    private MockPersonSaver personSaver;

    private PersonServiceImpl personService;

    private PersonBean person;

    private Person origin;

    @BeforeEach
    void setup() {
        applicationDAO = MockApplicationDAO.getInstance();
        personDAO = MockPersonDAO.getInstance();
        personByEmailDAO = MockPersonByEmailDAO.getInstance();
        authInfoDAO = MockAuthInfoDAO.getInstance();
        authInfoByLoginDAO = MockAuthInfoByLoginDAO.getInstance();
        personService = new PersonServiceImpl();
        personValidator = MockPersonValidator.getInstance();
        personSaver = MockPersonSaver.getInstance();
        setField(personService, "personValidator", personValidator);
        setField(personService, "personSaver", personSaver);
        setField(personService, "personMapper", MockPersonMapper.getInstance());
        setField(personService, "authInfoBuilder", MockAuthenticationInfoBuilder.getInstance());
        setField(personService, "personLookuper", MockPersonLookuper.getInstance());
        setField(personService, "applicationLookuper", MockApplicationLookuper.getInstance());
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
    void when_create_a_new_valid_person_then_save_her() {
        given_new_person_to_create();
        when_create_new_person();
        then_the_person_is_created();
    }

    @Test
    void when_create_a_new_valid_person_then_save_her_authentication() {
        given_new_person_to_create();
        when_create_new_person();
        then_the_authentication_info_is_created();
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

    @Test
    void when_update_valid_person_then_ok() throws ParseException {
        given_the_person_already_exists();
        given_the_person_updates();
        when_update_the_person();
        then_the_person_is_updated();
        then_the_authentication_info_is_updated();
    }

    @Test
    void when_update_and_all_properties_empty_then_do_not_update() {
        given_the_person_already_exists();
        given_the_person_empty_updates();
        when_update_the_person();
        then_the_person_is_not_updated();
    }

    @ParameterizedTest
    @ValueSource(strings = {"policy", "claims", "sefas", "lens"})
    void when_assign_existing_application_to_existing_person_then_link_them(final String appname) {
        given_the_person_already_exists();
        final Application application = Application.newInstance().setAppname(appname);
        applicationDAO.save(application);
        personService.assignApplication(appname, person.getExternalId());
        final Person p = personDAO.findByExternalId(person.getExternalId());
        assertThat(p.getApplications(), hasSize(1));
        assertThat(p.getApplications().iterator().next(), sameInstance(application));
    }

    @Test
    void when_assign_unexisting_application_to_existing_user_then_error() {
        given_the_person_already_exists();

        final ApplicationNotFoundExc exc = assertThrows(ApplicationNotFoundExc.class,
                () -> personService.assignApplication(randomString(), person.getExternalId()));

        assertThat(exc.getErrorCodes(), hasSize(1));
        assertThat(exc.getErrorCodes(), contains("APPLICATION_NOT_FOUND"));
    }

    @Test
    void when_assign_existing_application_to_unexisting_person_then_error() {
        final Application application = Application.newInstance().setAppname(randomString());
        applicationDAO.save(application);

        final PersonNotFoundExc exc = assertThrows(PersonNotFoundExc.class,
                () -> personService.assignApplication(application.getAppname(), randomString()));

        assertThat(exc.getErrorCodes(), hasSize(1));
        assertThat(exc.getErrorCodes(), contains("PERSON_NOT_FOUND"));
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

    private void given_the_person_already_exists() {
        origin = Person.newInstance()
                .setExternalId(randomString())
                .setFirstname(randomString())
                .setLastname(randomString())
                .setBirthdate(parseBirthDate(randomBirthdate()))
                .setRole(Role.newInstance().setCode(RoleCode.MANAGER))
                .setEmail(randomEmail());
        origin.setAuthenticationInfo(AuthInfo.newInstance().setLogin(origin.getEmail()).setPerson(origin));
        personSaver.create(origin);
        person = PersonBean.newInstance().setExternalId(origin.getExternalId());
        origin = origin.clone();
    }

    private Date parseBirthDate(final String date) {
        try {
            return DateUtils.parseDate(date, new String[] {PersonMapperImpl.DATE_FORMAT});
        } catch (Exception e) {
            throw IDPException.newInstance(e);
        }
    }

    private void given_the_person_updates() {
        person = person.setFirstname(randomString())
                .setLastname(randomString())
                .setBirthdate(randomBirthdate())
                .setRole(RoleCode.MANAGER.getValue())
                .setEmail(randomEmail());
    }

    private void given_the_person_empty_updates() {
        person = person.setFirstname("")
                .setLastname(null)
                .setBirthdate(null)
                .setRole(null)
                .setEmail(null);
    }

    private PersonBean when_create_new_person() {
        return personService.createPerson(person);
    }

    private void when_update_the_person() {
        personService.updatePerson(person);
    }

    private void then_the_person_is_created() {
        final Set<PersonByEmail> byEmail = personByEmailDAO.findByEmail(person.getEmail());
        assertThat(byEmail, hasSize(1));

        final Person p = personDAO.findByExternalId(byEmail.iterator().next().getPersonId());
        assertThat(p, notNullValue());
        assertThat(p.getExternalId(), equalTo(person.getExternalId()));
        assertThat(p.getEmail(), equalTo(person.getEmail()));
        assertThat(p.getFirstname(), equalTo(person.getFirstname()));
        assertThat(p.getLastname(), equalTo(person.getLastname()));
        assertThat(p.getBirthdate(), equalTo(parseBirthDate(person.getBirthdate())));
        assertThat(p.getRole(), notNullValue());
        assertThat(p.getRole().getCode().getValue(), equalTo(person.getRole()));
    }

    private void then_the_person_is_updated() throws ParseException {
        assertThat(personByEmailDAO.count(), equalTo(1L));
        assertThat(personDAO.count(), equalTo(1L));
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

    private void then_the_authentication_info_is_created() {
        final Set<AuthInfoByLogin> byLogin = authInfoByLoginDAO.findByLogin(person.getEmail());
        assertThat(byLogin, hasSize(1));

        final AuthInfo authInfo = authInfoDAO.findOne(byLogin.iterator().next().getAuthInfoId());
        assertThat(authInfo, notNullValue());
        assertThat(authInfo.getLogin(), equalTo(person.getEmail()));
        assertThat(authInfo.getEffectiveAt(), DateMatchers.before(new Date(System.currentTimeMillis() + ONE_SECOND)));
        assertThat(authInfo.getStatus(), equalTo(AuthInfoStatus.ACTIVE));
    }

    private void then_the_authentication_info_is_updated() {
        assertThat(authInfoByLoginDAO.count(), equalTo(1L));
        assertThat(authInfoDAO.count(), equalTo(1L));
        final Set<AuthInfoByLogin> byLogin = authInfoByLoginDAO.findByLogin(person.getEmail());
        assertThat(byLogin, hasSize(1));

        final AuthInfo authInfo = authInfoDAO.findOne(byLogin.iterator().next().getAuthInfoId());
        assertThat(authInfo, notNullValue());
        assertThat(authInfo.getLogin(), equalTo(person.getEmail()));
    }

    private void then_the_person_is_not_updated() {
        final Person p = personDAO.findByExternalId(person.getExternalId());
        assertThat(p, notNullValue());
        assertThat(p.getFirstname(), equalTo(origin.getFirstname()));
        assertThat(p.getLastname(), equalTo(origin.getLastname()));
        assertThat(p.getBirthdate(), equalTo(origin.getBirthdate()));
        assertThat(p.getEmail(), equalTo(origin.getEmail()));
        assertThat(p.getRole(), equalTo(origin.getRole()));
        assertThat(p.getRole(), sameInstance(origin.getRole()));
        assertThat(personByEmailDAO.findByEmail(origin.getEmail()), hasSize(1));

        final Set<AuthInfoByLogin> byLogin = authInfoByLoginDAO.findByLogin(p.getEmail());
        assertThat(byLogin, hasSize(1));
        assertThat(authInfoDAO.findOne(byLogin.iterator().next().getAuthInfoId()), equalTo(p.getAuthenticationInfo()));
    }

}
