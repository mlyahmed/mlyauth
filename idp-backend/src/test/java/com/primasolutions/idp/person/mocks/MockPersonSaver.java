package com.primasolutions.idp.person.mocks;

import com.primasolutions.idp.authentication.mocks.MockAuthenticationInfoSaver;
import com.primasolutions.idp.person.PersonSaver;
import com.primasolutions.idp.tools.MockReseter;
import com.primasolutions.idp.tools.ResettableMock;

public final class MockPersonSaver extends PersonSaver implements ResettableMock {

    private static MockPersonSaver instance;

    public static MockPersonSaver getInstance() {
        if (instance == null) {
            synchronized (MockPersonSaver.class) {
                if (instance == null)
                    instance = new MockPersonSaver();
            }
        }
        return instance;
    }

    private MockPersonSaver() {
        MockReseter.register(this);
        personDAO = MockPersonDAO.getInstance();
        personByEmailDAO = MockPersonByEmailDAO.getInstance();
        authenticationInfoSaver = new MockAuthenticationInfoSaver();
    }

    @Override
    public void reset() {
        instance = null;
    }
}
