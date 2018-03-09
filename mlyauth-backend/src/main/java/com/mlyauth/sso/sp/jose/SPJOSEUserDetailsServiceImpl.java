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

import static org.springframework.util.Assert.notNull;

@Service
@Transactional
public class SPJOSEUserDetailsServiceImpl implements SPJOSEUserDetailsService {

    @Autowired
    private PersonDAO personDAO;


    @Autowired
    private IContextHolder contextHolder;


    @Override
    public IDPUser loadUserByJOSE(JOSEAccessToken credential) throws UsernameNotFoundException {
        notNull(credential, "The JOSE Token is null");
        final Person person = personDAO.findByExternalId(credential.getSubject());
        final IContext context = contextHolder.newContext(person);
        return new IDPUser(context);
    }


}
