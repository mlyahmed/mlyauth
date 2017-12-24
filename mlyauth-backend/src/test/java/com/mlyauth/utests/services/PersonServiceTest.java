package com.mlyauth.utests.services;

import com.mlyauth.exception.AuthException;
import com.mlyauth.services.PersonService;
import org.junit.Test;

public class PersonServiceTest {

    @Test(expected = AuthException.class)
    public void when_create_a_new_person_and_null_then_error() {
        PersonService personService = new PersonService();
        personService.createPerson(null);
    }
}