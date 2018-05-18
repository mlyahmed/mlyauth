package com.primasolutions.idp.person.mocks;

import com.primasolutions.idp.person.PersonLookuper;
import com.primasolutions.idp.tools.MockReseter;
import com.primasolutions.idp.tools.ResettableMock;

public final class MockPersonLookuper extends PersonLookuper implements ResettableMock {

    private static volatile MockPersonLookuper instance;

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
        MockReseter.register(this);
        personDAO = MockPersonDAO.getInstance();
        personByEmailDAO = MockPersonByEmailDAO.getInstance();
    }

    @Override
    public void reset() {
        instance = null;
    }
}
