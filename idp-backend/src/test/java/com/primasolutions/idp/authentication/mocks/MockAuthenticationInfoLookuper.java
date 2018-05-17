package com.primasolutions.idp.authentication.mocks;

import com.primasolutions.idp.authentication.AuthenticationInfoLookuper;
import com.primasolutions.idp.tools.ResettableMock;

public final class MockAuthenticationInfoLookuper extends AuthenticationInfoLookuper implements ResettableMock {

    private static MockAuthenticationInfoLookuper instance;

    public static MockAuthenticationInfoLookuper getInstance() {
        if (instance == null) {
            synchronized (MockAuthenticationInfoLookuper.class) {
                if (instance == null)
                    instance = new MockAuthenticationInfoLookuper();
            }
        }
        return instance;
    }

    private MockAuthenticationInfoLookuper() {
        authInfoDAO = MockAuthenticationInfoDAO.getInstance();
        authInfoByLoginDAO = MockAuthenticationInfoByLoginDAO.getInstance();
    }

    @Override
    public void reset() {
        instance = null;
    }
}
