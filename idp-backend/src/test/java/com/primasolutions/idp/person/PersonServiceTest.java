package com.primasolutions.idp.person;

import com.primasolutions.idp.authentication.AuthenticationInfo;
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
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.primasolutions.idp.tools.RandomForTests.randomBirthdate;
import static com.primasolutions.idp.tools.RandomForTests.randomEmail;
import static com.primasolutions.idp.tools.RandomForTests.randomString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@RunWith(DataProviderRunner.class)
public class PersonServiceTest {

    private MockPersonValidator personValidator;

    private PersonService personService;

    private PersonBean person;

    @Before
    public void setup() {
        personService = new PersonService();
        personValidator = MockPersonValidator.getInstance();
        setField(personService, "personValidator", personValidator);
        setField(personService, "personSaver", MockPersonSaver.getInstance());
        setField(personService, "personBuilder", MockPersonBuilder.getInstance());
        setField(personService, "authInfoBuilder", MockAuthenticationInfoBuilder.getInstance());
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

    @Test
    public void when_create_a_new_valid_person_then_save_her_auth_info() {
        given_new_person_to_create();
        when_create_new_person();
        final AuthenticationInfo actual = MockAuthenticationInfoLookuper.getInstance().byLogin((person.getEmail()));
        assertThat(actual, notNullValue());
        assertThat(actual.getLogin(), Matchers.equalTo(person.getEmail()));
    }

    @DataProvider
    public static Object[] emailAddresses() {
        // @formatter:off
        return new Object[]{
                randomEmail(),
                randomEmail(),
                randomEmail(),
                randomEmail(),
        };
        // @formatter:on
    }

    @Test(expected = IDPException.class)
    @UseDataProvider("emailAddresses")
    public void when_create_a_new_person_with_an_elready_existing_email_then_error(final String emailAddress) {
        given_an_existing_person_with_email(emailAddress);
        given_new_person_to_create_with_email(emailAddress);
        when_create_new_person();
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
                .setRole(RoleCode.CLIENT)
                .setEmail(randomEmail());
    }

    private PersonBean when_create_new_person() {
        return personService.createPerson(person);
    }
}
