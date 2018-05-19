package com.primasolutions.idp.person;

import com.primasolutions.idp.authentication.AuthInfo;
import com.primasolutions.idp.authentication.Role;
import com.primasolutions.idp.authentication.mocks.MockAuthenticationInfoSaver;
import com.primasolutions.idp.constants.RoleCode;
import com.primasolutions.idp.person.mocks.MockPersonByEmailDAO;
import com.primasolutions.idp.person.mocks.MockPersonDAO;
import com.primasolutions.idp.tools.MockReseter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.primasolutions.idp.tools.RandomForTests.randomEmail;
import static com.primasolutions.idp.tools.RandomForTests.randomName;
import static com.primasolutions.idp.tools.RandomForTests.randomString;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.util.ReflectionTestUtils.setField;

class PersonSaverTest {

    private MockPersonDAO personDAO;

    private MockPersonByEmailDAO personByEmailDAO;

    private MockAuthenticationInfoSaver authenticationInfoSaver;

    private PersonSaver saver;
    private Person person;
    private AuthInfo authInfo;


    @BeforeEach
    void before() {
        personDAO = MockPersonDAO.getInstance();
        personByEmailDAO = MockPersonByEmailDAO.getInstance();
        authenticationInfoSaver = MockAuthenticationInfoSaver.getInstance();
        saver = new PersonSaver();
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

    }

    @Test
    void when_person_is_nul_then_error() {
        person = null;
        assertThrows(IllegalArgumentException.class, this::when_create_person);
    }

    @Test
    void when_authentication_info_is_null_then_error() {
        given_the_person_to_process();
        assertThrows(IllegalArgumentException.class, this::when_create_person);
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

}
