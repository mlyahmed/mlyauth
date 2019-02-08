package com.hohou.federation.idp.person.mocks;

import com.hohou.federation.idp.person.lookuper.PersonLookuperImpl;
import com.hohou.federation.idp.tools.MockReseter;
import com.hohou.federation.idp.tools.ResettableMock;

public final class MockPersonLookuper extends PersonLookuperImpl implements ResettableMock {

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
