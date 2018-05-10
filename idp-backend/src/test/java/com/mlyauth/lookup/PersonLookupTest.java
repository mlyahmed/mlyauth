package com.mlyauth.lookup;

import com.mlyauth.dao.PersonByEmailDAO;
import com.mlyauth.dao.PersonDAO;
import com.mlyauth.domain.Person;
import com.mlyauth.domain.PersonByEmail;
import com.mlyauth.tools.RandomForTests;
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
import static java.util.Arrays.asList;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(DataProviderRunner.class)
public class PersonLookupTest {

    private PersonLookup personLookup;
    private PersonDAO personDAO;
    private PersonByEmailDAO personByEmailDAO;
    private Person person;
    private PersonByEmail personByEmail;

    @Before
    public void setup(){
        personLookup = new PersonLookup();
        personDAO = mock(PersonDAO.class);
        personByEmailDAO = mock(PersonByEmailDAO.class);
        ReflectionTestUtils.setField(personLookup, "personDAO", personDAO);
        ReflectionTestUtils.setField(personLookup, "personByEmailDAO", personByEmailDAO);
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
    public void when_find_person_by_email_and_exists_then_return_the_person(String email){
        person = Person.newInstance().setExternalId(RandomForTests.randomString()).setEmail(email);
        personByEmail = PersonByEmail.newInstance().setPersonId(person.getExternalId()).setEmail(email);
        when(personByEmailDAO.findByEmail(personByEmail.getEmail())).thenReturn(new HashSet<>(asList(personByEmail)));
        when(personDAO.findByExternalId(personByEmail.getPersonId())).thenReturn(person);
        final Person expected = personLookup.byEmail(email);
        assertThat(expected, Matchers.notNullValue());
        assertThat(expected, Matchers.sameInstance(person));
    }
}