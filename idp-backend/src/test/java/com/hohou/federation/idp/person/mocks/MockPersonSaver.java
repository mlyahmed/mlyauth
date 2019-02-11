package com.hohou.federation.idp.person.mocks;

import com.hohou.federation.idp.authentication.mocks.MockAuthenticationInfoSaver;
import com.hohou.federation.idp.person.saver.PersonSaverImpl;
import com.hohou.federation.idp.tools.MockReseter;
import com.hohou.federation.idp.tools.ResettableMock;

public final class MockPersonSaver extends PersonSaverImpl implements ResettableMock {

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
