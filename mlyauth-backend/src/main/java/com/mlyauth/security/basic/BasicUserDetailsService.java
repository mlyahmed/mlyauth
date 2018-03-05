package com.mlyauth.security.basic;

import com.mlyauth.context.IContextHolder;
import com.mlyauth.context.IDPUser;
import com.mlyauth.dao.PersonDAO;
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
    private PersonDAO personDAO;

    @Autowired
    private IContextHolder contextHolder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        final Person person = personDAO.findByEmail(username);
        return person == null ? null : new IDPUser(contextHolder.newContext(person));
    }

}
