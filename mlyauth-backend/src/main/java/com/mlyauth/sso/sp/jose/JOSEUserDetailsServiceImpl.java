package com.mlyauth.sso.sp.jose;

import com.mlyauth.context.IContext;
import com.mlyauth.context.IContextHolder;
import com.mlyauth.context.IDPUser;
import com.mlyauth.dao.PersonDAO;
import com.mlyauth.domain.Person;
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
