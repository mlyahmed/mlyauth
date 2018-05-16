package com.primasolutions.idp.authentication;

public class MockAuthenticationInfoSaver extends AuthenticationInfoSaver {

    public MockAuthenticationInfoSaver() {
        authenticationInfoDAO = MockAuthenticationInfoDAO.getInstance();
        authInfoByLoginDAO = MockAuthenticationInfoByLoginDAO.getInstance();
    }
}
