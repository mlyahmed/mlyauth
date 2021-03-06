package com.hohou.federation.idp.person.saver;

import com.hohou.federation.idp.authentication.AuthInfo;
import com.hohou.federation.idp.authentication.AuthInfoByLogin;
import com.hohou.federation.idp.authentication.Role;
import com.hohou.federation.idp.authentication.mocks.MockAuthInfoByLoginDAO;
import com.hohou.federation.idp.authentication.mocks.MockAuthInfoDAO;
import com.hohou.federation.idp.authentication.mocks.MockAuthenticationInfoSaver;
import com.hohou.federation.idp.constants.RoleCode;
import com.hohou.federation.idp.person.mocks.MockPersonByEmailDAO;
import com.hohou.federation.idp.person.mocks.MockPersonDAO;
import com.hohou.federation.idp.person.model.Person;
import com.hohou.federation.idp.person.model.PersonByEmail;
import com.hohou.federation.idp.tools.MockReseter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.hohou.federation.idp.tools.RandomForTests.randomEmail;
import static com.hohou.federation.idp.tools.RandomForTests.randomName;
import static com.hohou.federation.idp.tools.RandomForTests.randomString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.util.ReflectionTestUtils.setField;

class PersonSaverImplTest {

    private MockPersonDAO personDAO;

    private MockPersonByEmailDAO personByEmailDAO;

    private MockAuthInfoDAO authInfoDAO;

    private MockAuthInfoByLoginDAO authInfoByLoginDAO;

    private MockAuthenticationInfoSaver authenticationInfoSaver;

    private PersonSaverImpl saver;

    private Person person;

    private AuthInfo authInfo;

    @BeforeEach
    void before() {
        personDAO = MockPersonDAO.getInstance();
        personByEmailDAO = MockPersonByEmailDAO.getInstance();
        authInfoDAO = MockAuthInfoDAO.getInstance();
        authInfoByLoginDAO = MockAuthInfoByLoginDAO.getInstance();
        authenticationInfoSaver = MockAuthenticationInfoSaver.getInstance();
        saver = new PersonSaverImpl();
        setField(saver, "personDAO", personDAO);
        setField(saver, "byEmailDAO", personByEmailDAO);
        setField(saver, "authenticationInfoSaver", authenticationInfoSaver);
    }

    @AfterEach
    void tearsDown() {
        MockReseter.resetAllMocks();
    }

    @Test
    void when_create_a_new_person_then_save_him() {
        given_the_person_to_process();
        and_the_person_authentication_info();
        when_create_person();
        then_the_person_is_created();
        then_the_person_authentication_info_is_created();
    }

    @Test
    void when_create_and_person_is_nul_then_error() {
        person = null;
        assertThrows(IllegalArgumentException.class, this::when_create_person);
    }

    @Test
    void when_create_and_authentication_info_is_null_then_error() {
        given_the_person_to_process();
        assertThrows(IllegalArgumentException.class, this::when_create_person);
    }

    @Test
    void when_update_and_person_is_null_then_error() {
        person = null;
        assertThrows(IllegalArgumentException.class, this::when_update_person);
    }


    private void given_the_person_to_process() {
        person = Person.newInstance()
                .setExternalId(randomString())
                .setFirstname(randomName().getFirstName())
                .setLastname(randomName().getLastName())
                .setRole(Role.newInstance().setCode(RoleCode.CLIENT))
                .setEmail(randomEmail());
    }

    private void and_the_person_authentication_info() {
        authInfo = AuthInfo.newInstance();
        person.setAuthenticationInfo(authInfo.setLogin(person.getEmail()).setPerson(person));
    }

    private void when_create_person() {
        saver.create(person);
    }

    private void when_update_person() {
        saver.update(person);
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
        assertThat(p.getBirthdate(), equalTo(person.getBirthdate()));
        assertThat(p.getRole(), notNullValue());
        assertThat(p.getRole().getCode(), equalTo(person.getRole().getCode()));
    }

    private void then_the_person_authentication_info_is_created() {
        final Set<AuthInfoByLogin> byLogin = authInfoByLoginDAO.findByLogin(person.getEmail());
        assertThat(byLogin, hasSize(1));

        final AuthInfo auth = authInfoDAO.findById(byLogin.iterator().next().getAuthInfoId()).get();
        assertThat(auth, sameInstance(person.getAuthenticationInfo()));
    }

}
