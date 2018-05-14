package com.primasolutions.idp.security.authentication;

import com.primasolutions.idp.dao.AuthenticationInfoByLoginDAO;
import com.primasolutions.idp.dao.AuthenticationInfoDAO;
import com.primasolutions.idp.domain.AuthenticationInfo;
import com.primasolutions.idp.domain.AuthenticationInfoByLogin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationInfoSaver {

    @Autowired
    private AuthenticationInfoDAO authenticationInfoDAO;

    @Autowired
    private AuthenticationInfoByLoginDAO authInfoByLoginDAO;

    public void create(final AuthenticationInfo auth) {
        final AuthenticationInfo authInfo = authenticationInfoDAO.saveAndFlush(auth);
        authInfoByLoginDAO.saveAndFlush(AuthenticationInfoByLogin.newInstance()
                .setAuthInfoId(authInfo.getId())
                .setLogin(authInfo.getLogin()));
    }
}
