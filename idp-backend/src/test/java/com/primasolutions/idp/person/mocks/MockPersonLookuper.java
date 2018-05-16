package com.primasolutions.idp.person.mocks;

import com.primasolutions.idp.person.PersonLookuper;

public class MockPersonLookuper extends PersonLookuper {

    public MockPersonLookuper() {
        personDAO = MockPersonDAO.getInstance();
        personByEmailDAO = MockPersonByEmailDAO.getInstance();
    }
}
