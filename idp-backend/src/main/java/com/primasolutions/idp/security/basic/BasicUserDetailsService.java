package com.primasolutions.idp.security.basic;

import com.primasolutions.idp.authentication.AuthenticationInfo;
import com.primasolutions.idp.authentication.AuthenticationInfoDAO;
import com.primasolutions.idp.authentication.AuthenticationInfoLookuper;
import com.primasolutions.idp.context.IContextHolder;
import com.primasolutions.idp.context.IDPUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class BasicUserDetailsService implements UserDetailsService {

    @Autowired
    private AuthenticationInfoLookuper authenticationInfoLookuper;

    @Autowired
    private IContextHolder contextHolder;

    @Autowired
    private AuthenticationInfoDAO authenticationInfoDAO;

    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        final AuthenticationInfo authenticationInfo = authenticationInfoLookuper.byLogin(username);
        if (authenticationInfo == null) return null;

        if (authenticationInfo.isPerson()) {
            return new IDPUser(contextHolder.newPersonContext(authenticationInfo.getPerson()));
        } else if (authenticationInfo.isApplication()) {
            return new IDPUser(contextHolder.newApplicationContext(authenticationInfo.getApplication()));
        }

        return null;
    }

}
