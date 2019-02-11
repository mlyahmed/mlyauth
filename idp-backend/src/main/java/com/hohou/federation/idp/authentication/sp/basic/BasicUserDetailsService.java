package com.hohou.federation.idp.authentication.sp.basic;

import com.hohou.federation.idp.authentication.AuthInfo;
import com.hohou.federation.idp.authentication.AuthInfoDAO;
import com.hohou.federation.idp.authentication.AuthenticationInfoLookuper;
import com.hohou.federation.idp.context.IContextHolder;
import com.hohou.federation.idp.context.IDPUser;
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
    private AuthInfoDAO authInfoDAO;

    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        final AuthInfo authenticationInfo = authenticationInfoLookuper.byLogin(username);
        if (authenticationInfo == null) return null;

        if (authenticationInfo.isPerson()) {
            return new IDPUser(contextHolder.newPersonContext(authenticationInfo.getPerson()));
        } else if (authenticationInfo.isApplication()) {
            return new IDPUser(contextHolder.newApplicationContext(authenticationInfo.getApplication()));
        }

        return null;
    }

}
