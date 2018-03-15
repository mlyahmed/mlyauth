package com.mlyauth.security.basic;

import com.mlyauth.context.IContextHolder;
import com.mlyauth.context.IDPUser;
import com.mlyauth.dao.AuthenticationInfoDAO;
import com.mlyauth.domain.AuthenticationInfo;
import com.mlyauth.domain.Person;
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
    private IContextHolder contextHolder;

    @Autowired
    private AuthenticationInfoDAO authenticationInfoDAO;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        final AuthenticationInfo authenticationInfo = authenticationInfoDAO.findByLogin(username);
        final Person person = authenticationInfo.getPerson();
        return person == null ? null : new IDPUser(contextHolder.newPersonContext(person));
    }

}
