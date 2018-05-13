package com.primasolutions.idp.security.authentication;

import com.primasolutions.idp.dao.AuthenticationInfoByLoginDAO;
import com.primasolutions.idp.dao.AuthenticationInfoDAO;
import com.primasolutions.idp.domain.AuthenticationInfo;
import com.primasolutions.idp.domain.AuthenticationInfoByLogin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class AuthenticationInfoLookuper {

    @Autowired
    private AuthenticationInfoDAO authInfoDAO;

    @Autowired
    private AuthenticationInfoByLoginDAO authInfoByLoginDAO;

    public AuthenticationInfo byLogin(final String login) {
        final Set<AuthenticationInfoByLogin> byLogin = authInfoByLoginDAO.findByLogin(login);
        return authInfoDAO.findOne(byLogin.iterator().next().getAuthInfoId());
    }

}
