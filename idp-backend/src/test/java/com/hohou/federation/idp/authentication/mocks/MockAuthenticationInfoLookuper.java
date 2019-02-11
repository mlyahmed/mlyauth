package com.hohou.federation.idp.authentication.mocks;

import com.hohou.federation.idp.authentication.AuthenticationInfoLookuper;
import com.hohou.federation.idp.tools.ResettableMock;

public final class MockAuthenticationInfoLookuper extends AuthenticationInfoLookuper implements ResettableMock {

    private static volatile MockAuthenticationInfoLookuper instance;

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
        authInfoDAO = MockAuthInfoDAO.getInstance();
        authInfoByLoginDAO = MockAuthInfoByLoginDAO.getInstance();
    }

    @Override
    public void reset() {
        instance = null;
    }
}
