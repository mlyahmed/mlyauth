package com.primasolutions.idp.person;

import com.primasolutions.idp.exception.IDPException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

public class PersonServiceTest {

    private MockPersonValidator personValidator;

    private PersonService personService;

    @Before
    public void setup() {
        personService = new PersonService();
        personValidator = new MockPersonValidator();
        ReflectionTestUtils.setField(personService, "personValidator", personValidator);
    }

    @Test(expected = IDPException.class)
    public void when_validation_is_ko_then_error() {
        personService.createPerson(null);
    }


    @Test
    public void when_create_a_new_person_and_email_is_not_valid_then_error() {

    }
}
