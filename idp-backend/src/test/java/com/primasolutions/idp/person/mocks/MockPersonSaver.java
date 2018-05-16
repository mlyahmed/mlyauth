package com.primasolutions.idp.person.mocks;

import com.primasolutions.idp.authentication.mocks.MockAuthenticationInfoSaver;
import com.primasolutions.idp.person.PersonSaver;

public class MockPersonSaver extends PersonSaver {

    public MockPersonSaver() {
        personDAO = MockPersonDAO.getInstance();
        personByEmailDAO = MockPersonByEmailDAO.getInstance();
        authenticationInfoSaver = new MockAuthenticationInfoSaver();
    }
}
