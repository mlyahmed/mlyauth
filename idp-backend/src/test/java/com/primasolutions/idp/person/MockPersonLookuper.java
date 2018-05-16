package com.primasolutions.idp.person;

public class MockPersonLookuper extends PersonLookuper {

    public MockPersonLookuper() {
        personDAO = MockPersonDAO.getInstance();
        personByEmailDAO = MockPersonByEmailDAO.getInstance();
    }
}
