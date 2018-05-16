package com.primasolutions.idp.authentication.mocks;

import com.primasolutions.idp.authentication.AuthenticationInfoSaver;

public class MockAuthenticationInfoSaver extends AuthenticationInfoSaver {

    public MockAuthenticationInfoSaver() {
        authenticationInfoDAO = MockAuthenticationInfoDAO.getInstance();
        authInfoByLoginDAO = MockAuthenticationInfoByLoginDAO.getInstance();
    }
}
