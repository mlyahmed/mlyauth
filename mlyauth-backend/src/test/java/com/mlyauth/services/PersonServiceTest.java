package com.mlyauth.services;

import com.mlyauth.exception.IDPException;
import com.mlyauth.services.domain.PersonService;
import com.mlyauth.validators.IPersonValidator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class PersonServiceTest {

    @Mock
    private IPersonValidator personValidator;

    @InjectMocks
    private PersonService personService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test(expected = IDPException.class)
    public void when_validation_is_ko_then_error() {
        Mockito.doThrow(IDPException.newInstance()).when(personValidator).validateNewPerson(Mockito.any());
        personService.createPerson(null);
    }


    @Test
    public void when_create_a_new_person_and_email_is_not_valid_then_error() {

    }
}