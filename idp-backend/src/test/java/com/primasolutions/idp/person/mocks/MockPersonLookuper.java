package com.primasolutions.idp.person.mocks;

import com.primasolutions.idp.person.PersonLookuper;

public final class MockPersonLookuper extends PersonLookuper {

    private static MockPersonLookuper instance;

    public static MockPersonLookuper getInstance() {
        if (instance == null) {
            synchronized (MockPersonLookuper.class) {
                if (instance == null)
                    instance = new MockPersonLookuper();
            }
        }
        return instance;
    }

    private MockPersonLookuper() {
        personDAO = MockPersonDAO.getInstance();
        personByEmailDAO = MockPersonByEmailDAO.getInstance();
    }
}
