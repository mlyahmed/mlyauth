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

import static com.mlyauth.token.IDPClaims.*;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.springframework.util.Assert.isTrue;
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
        isTrue(isNotBlank(credential.getSubject()), "The JOSE Token Subject is blank");
        isTrue(isNotBlank(credential.getClaim(CLIENT_ID.getValue())), "The JOSE Token Client Id is blank");
        isTrue(isNotBlank(credential.getClaim(CLIENT_PROFILE.getValue())), "The JOSE Token Client Profile is blank");

        final Person person = personDAO.findByExternalId(credential.getSubject());
        final IContext context = contextHolder.newContext(person);

        context.putAttribute(CLIENT_ID.getValue(), credential.getClaim(CLIENT_ID.getValue()));
        context.putAttribute(CLIENT_PROFILE.getValue(), credential.getClaim(CLIENT_PROFILE.getValue()));
        context.putAttribute(ENTITY_ID.getValue(), credential.getClaim(ENTITY_ID.getValue()));
        context.putAttribute(ACTION.getValue(), credential.getClaim(ACTION.getValue()));
        context.putAttribute(APPLICATION.getValue(), credential.getClaim(APPLICATION.getValue()));

        return new IDPUser(context);
    }


}
