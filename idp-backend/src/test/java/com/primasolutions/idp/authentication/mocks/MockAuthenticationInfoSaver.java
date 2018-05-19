package com.primasolutions.idp.authentication.mocks;

import com.primasolutions.idp.authentication.AuthenticationInfoSaver;
import com.primasolutions.idp.tools.MockReseter;
import com.primasolutions.idp.tools.ResettableMock;

public final class MockAuthenticationInfoSaver extends AuthenticationInfoSaver implements ResettableMock {

    private static volatile MockAuthenticationInfoSaver instance;

    public static MockAuthenticationInfoSaver getInstance() {
        if (instance == null) {
            synchronized (MockAuthenticationInfoSaver.class) {
                if (instance == null)
                    instance = new MockAuthenticationInfoSaver();
            }
        }
        return instance;
    }

    private MockAuthenticationInfoSaver() {
        MockReseter.register(this);
        authInfoDAO = MockAuthInfoDAO.getInstance();
        authInfoByLoginDAO = MockAuthInfoByLoginDAO.getInstance();
    }

    @Override
    public void reset() {
        instance = null;
    }
}
