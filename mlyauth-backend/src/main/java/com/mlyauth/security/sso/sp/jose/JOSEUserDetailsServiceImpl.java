package com.mlyauth.security.sso.sp.jose;

import com.mlyauth.dao.PersonDAO;
import com.mlyauth.domain.Person;
import com.mlyauth.security.context.IContext;
import com.mlyauth.security.context.IContextHolder;
import com.mlyauth.security.context.IDPUser;
import com.mlyauth.token.jose.JOSEAccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class JOSEUserDetailsServiceImpl implements JOSEUserDetailsService {

    @Autowired
    private PersonDAO personDAO;


    @Autowired
    private IContextHolder contextHolder;


    @Override
    public IDPUser loadUserByJOSE(JOSEAccessToken credential) throws UsernameNotFoundException {
        final Person person = personDAO.findByExternalId("1");
        final IContext context = contextHolder.newContext(person);
        return new IDPUser(context);
    }


}
