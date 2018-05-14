package com.primasolutions.idp.person;

import com.primasolutions.idp.exception.IDPException;
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
        Mockito.doThrow(IDPException.newInstance()).when(personValidator).validateNew(Mockito.any());
        personService.createPerson(null);
    }


    @Test
    public void when_create_a_new_person_and_email_is_not_valid_then_error() {

    }
}
