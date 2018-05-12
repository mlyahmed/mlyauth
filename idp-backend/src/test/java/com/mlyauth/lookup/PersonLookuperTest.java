package com.mlyauth.lookup;

import com.mlyauth.dao.PersonByEmailDAO;
import com.mlyauth.dao.PersonDAO;
import com.mlyauth.domain.Person;
import com.mlyauth.domain.PersonByEmail;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashSet;

import static com.mlyauth.tools.RandomForTests.randomFrenchEmail;
import static com.mlyauth.tools.RandomForTests.randomString;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(DataProviderRunner.class)
public class PersonLookuperTest {

    private PersonLookuper personLookuper;
    private PersonDAO personDAO;
    private PersonByEmailDAO personByEmailDAO;
    private Person person;
    private PersonByEmail personByEmail;

    @Before
    public void setup() {
        personLookuper = new PersonLookuper();
        personDAO = mock(PersonDAO.class);
        personByEmailDAO = mock(PersonByEmailDAO.class);
        ReflectionTestUtils.setField(personLookuper, "personDAO", personDAO);
        ReflectionTestUtils.setField(personLookuper, "personByEmailDAO", personByEmailDAO);
    }

    @DataProvider
    public static String[] emails() {
        // @formatter:off
        return new String[]{
                randomFrenchEmail(),
                randomFrenchEmail(),
                randomFrenchEmail(),
                randomFrenchEmail(),
                randomFrenchEmail(),
                randomFrenchEmail(),
                randomFrenchEmail(),
        };
        // @formatter:on
    }


    @Test
    @UseDataProvider("emails")
    public void when_find_person_by_email_and_exists_then_return_the_person(final String email) {
        person = Person.newInstance().setExternalId(randomString()).setEmail(email);
        personByEmail = PersonByEmail.newInstance().setPersonId(person.getExternalId()).setEmail(email);
        when(personByEmailDAO.findByEmail(email)).thenReturn(new HashSet<>(asList(personByEmail)));
        when(personDAO.findByExternalId(personByEmail.getPersonId())).thenReturn(person);
        final Person expected = personLookuper.byEmail(email);
        assertThat(expected, Matchers.notNullValue());
        assertThat(expected, Matchers.sameInstance(person));
    }

    @Test
    @UseDataProvider("emails")
    public void when_the_person_by_email_index_returns_empty_result_then_return_null(final String email) {
        person = Person.newInstance().setExternalId(randomString()).setEmail(email);
        when(personByEmailDAO.findByEmail(email)).thenReturn(new HashSet<>());
        final Person expected = personLookuper.byEmail(email);
        assertThat(expected, Matchers.nullValue());
    }

    @Test
    @UseDataProvider("emails")
    public void when_the_person_by_email_index_returns_null_then_return_null(final String email) {
        person = Person.newInstance().setExternalId(randomString()).setEmail(email);
        when(personByEmailDAO.findByEmail(email)).thenReturn(null);
        final Person expected = personLookuper.byEmail(email);
        assertThat(expected, Matchers.nullValue());
    }

    @Test
    @UseDataProvider("emails")
    public void when_many_emails_token_match_the_email_then_return_the_right_one(final String email) {
        person = Person.newInstance().setExternalId(randomString()).setEmail(email);

        personByEmail = PersonByEmail.newInstance().setPersonId(person.getExternalId()).setEmail(email);
        PersonByEmail bias = PersonByEmail.newInstance().setPersonId(randomString()).setEmail(randomFrenchEmail());

        when(personByEmailDAO.findByEmail(email)).thenReturn(new HashSet<>(asList(personByEmail, bias)));
        when(personDAO.findByExternalId(personByEmail.getPersonId())).thenReturn(person);

        final Person expected = personLookuper.byEmail(email);
        assertThat(expected, Matchers.notNullValue());
        assertThat(expected, Matchers.sameInstance(person));
    }
}
