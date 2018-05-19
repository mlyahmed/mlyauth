package com.primasolutions.idp.person.mocks;

import com.primasolutions.idp.authentication.mocks.MockAuthenticationInfoSaver;
import com.primasolutions.idp.person.PersonSaver;
import com.primasolutions.idp.tools.MockReseter;
import com.primasolutions.idp.tools.ResettableMock;

public final class MockPersonSaver extends PersonSaver implements ResettableMock {

    private static volatile MockPersonSaver instance;

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
        byEmailDAO = MockPersonByEmailDAO.getInstance();
        authenticationInfoSaver = MockAuthenticationInfoSaver.getInstance();
    }

    @Override
    public void reset() {
        instance = null;
    }
}
