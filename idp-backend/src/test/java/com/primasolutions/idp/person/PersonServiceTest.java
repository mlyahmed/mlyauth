package com.primasolutions.idp.person;

import com.primasolutions.idp.authentication.mocks.MockAuthenticationInfoBuilder;
import com.primasolutions.idp.constants.RoleCode;
import com.primasolutions.idp.exception.IDPException;
import com.primasolutions.idp.person.mocks.MockPersonBuilder;
import com.primasolutions.idp.person.mocks.MockPersonDAO;
import com.primasolutions.idp.person.mocks.MockPersonLookuper;
import com.primasolutions.idp.person.mocks.MockPersonSaver;
import com.primasolutions.idp.person.mocks.MockPersonValidator;
import com.primasolutions.idp.tools.MockReseter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.primasolutions.idp.tools.RandomForTests.randomBirthdate;
import static com.primasolutions.idp.tools.RandomForTests.randomFrenchEmail;
import static com.primasolutions.idp.tools.RandomForTests.randomString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class PersonServiceTest {

    private MockPersonValidator personValidator;

    private PersonService personService;
    private PersonBean person;

    @Before
    public void setup() {
        personService = new PersonService();
        personValidator = MockPersonValidator.getInstance();
        setField(personService, "personValidator", personValidator);
        setField(personService, "personSaver", new MockPersonSaver());
        setField(personService, "personBuilder", new MockPersonBuilder());
        setField(personService, "authInfoBuilder", new MockAuthenticationInfoBuilder());
        setField(personService, "personLookuper", MockPersonLookuper.getInstance());
    }

    @After
    public void tearsDown() {
        MockReseter.resetAllMocks();
    }

    @Test(expected = IDPException.class)
    public void when_validation_is_ko_then_error() {
        personService.createPerson(null);
    }

    @Test(expected = IDPException.class)
    public void when_create_a_new_person_and_not_valid_then_error() {
        personValidator.setForcedError(IDPException.newInstance());
        given_new_person_to_create();
        when_create_new_person();
    }

    @Test
    public void when_create_a_new_valid_person_then_save_her() {
        given_new_person_to_create();
        final PersonBean result = when_create_new_person();
        assertThat(result, notNullValue());
        assertThat(MockPersonDAO.getInstance().exists(result.getId()), equalTo(true));
    }

    private void given_new_person_to_create() {
        person = PersonBean.newInstance()
                .setExternalId(randomString())
                .setFirstname(randomString())
                .setLastname(randomString())
                .setBirthdate(randomBirthdate())
                .setRole(RoleCode.CLIENT)
                .setEmail(randomFrenchEmail());
    }

    private PersonBean when_create_new_person() {
        return personService.createPerson(person);
    }
}
