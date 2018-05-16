package com.primasolutions.idp.person;

import com.primasolutions.idp.authentication.mocks.MockAuthenticationInfoSaver;

public class MockPersonSaver extends PersonSaver {

    public MockPersonSaver() {
        personDAO = MockPersonDAO.getInstance();
        personByEmailDAO = MockPersonByEmailDAO.getInstance();
        authenticationInfoSaver = new MockAuthenticationInfoSaver();
    }
}
