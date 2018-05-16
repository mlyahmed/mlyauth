package com.primasolutions.idp.person;

import com.primasolutions.idp.authentication.mocks.MockAuthenticationInfoBuilder;
import com.primasolutions.idp.constants.RoleCode;
import com.primasolutions.idp.exception.IDPException;
import com.primasolutions.idp.person.mocks.MockPersonBuilder;
import com.primasolutions.idp.person.mocks.MockPersonLookuper;
import com.primasolutions.idp.person.mocks.MockPersonSaver;
import com.primasolutions.idp.person.mocks.MockPersonValidator;
import com.primasolutions.idp.tools.MockReseter;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static com.primasolutions.idp.tools.RandomForTests.randomBirthdate;
import static com.primasolutions.idp.tools.RandomForTests.randomFrenchEmail;
import static com.primasolutions.idp.tools.RandomForTests.randomString;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class PersonServiceTest {

    private MockPersonValidator personValidator;

    private PersonService personService;
    private PersonBean person;

    @Before
    public void setup() {
        personService = new PersonService();
        personValidator = new MockPersonValidator();
        setField(personService, "personValidator", personValidator);
        setField(personService, "personSaver", new MockPersonSaver());
        setField(personService, "personBuilder", new MockPersonBuilder());
        setField(personService, "authInfoBuilder", new MockAuthenticationInfoBuilder());
        setField(personService, "personLookuper", new MockPersonLookuper());
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
        personService.createPerson(person);
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

    @Test
    public void when_create_a_new_valid_person_then_save_her() {
        given_new_person_to_create();
        final PersonBean result = personService.createPerson(person);
        Assert.assertThat(result, Matchers.notNullValue());
    }
}
