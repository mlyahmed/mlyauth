package com.primasolutions.idp.person;

import com.primasolutions.idp.person.mocks.MockPersonDAO;
import org.junit.jupiter.api.Test;

import static org.springframework.test.util.ReflectionTestUtils.setField;

class PersonSaverTest {

    @Test
    void when_create_a_new_person_then_save_him() {
        PersonSaver saver = new PersonSaver();
        setField(saver, "personDAO", MockPersonDAO.getInstance());
    }


}
