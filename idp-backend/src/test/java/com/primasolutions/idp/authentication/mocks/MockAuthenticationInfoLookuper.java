package com.primasolutions.idp.authentication.mocks;

import com.primasolutions.idp.authentication.AuthenticationInfoLookuper;

public final class MockAuthenticationInfoLookuper extends AuthenticationInfoLookuper {

    private static class LazyHolder {
        static final MockAuthenticationInfoLookuper INSTANCE = new MockAuthenticationInfoLookuper();
    }

    public static MockAuthenticationInfoLookuper getInstance() {
        return LazyHolder.INSTANCE;
    }

    private MockAuthenticationInfoLookuper() {
        authInfoDAO = MockAuthenticationInfoDAO.getInstance();
        authInfoByLoginDAO = MockAuthenticationInfoByLoginDAO.getInstance();
    }

}
