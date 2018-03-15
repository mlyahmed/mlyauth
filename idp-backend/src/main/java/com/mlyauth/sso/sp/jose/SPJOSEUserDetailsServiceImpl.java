package com.mlyauth.sso.sp.jose;

import com.mlyauth.context.IContext;
import com.mlyauth.context.IContextHolder;
import com.mlyauth.context.IDPUser;
import com.mlyauth.dao.PersonDAO;
import com.mlyauth.domain.Person;
import com.mlyauth.token.jose.JOSEAccessToken;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.mlyauth.token.Claims.*;
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
        checkParams(credential);
        final Person person = getPerson(credential);
        checkTheApplicationAssignement(credential, person);
        final IContext context = contextHolder.newPersonContext(person);
        setAttributesIntoTheContext(credential, context);
        return new IDPUser(context);
    }

    private void checkParams(JOSEAccessToken credential) {
        notNull(credential, "The JOSE Token is null");
        isTrue(isNotBlank(credential.getSubject()), "The JOSE Token Subject is blank");
        isTrue(isNotBlank(credential.getClaim(CLIENT_ID.getValue())), "The JOSE Token Client Id is blank");
        isTrue(isNotBlank(credential.getClaim(CLIENT_PROFILE.getValue())), "The JOSE Token Client Profile is blank");
    }

    private Person getPerson(JOSEAccessToken credential) {
        final Person person = personDAO.findByExternalId(credential.getSubject());
        notNull(person, "The Person is not found");
        return person;
    }

    private void checkTheApplicationAssignement(JOSEAccessToken credential, Person person) {
        if (StringUtils.isNotBlank(credential.getClaim(APPLICATION.getValue()))) {
            final boolean assigned = person.getApplications().stream().anyMatch(app -> app.getAppname().equals(credential.getClaim(APPLICATION.getValue())));
            isTrue(assigned, "The application is not assigned to the person");
        }
    }

    private void setAttributesIntoTheContext(JOSEAccessToken credential, IContext context) {
        context.putAttribute(CLIENT_ID.getValue(), credential.getClaim(CLIENT_ID.getValue()));
        context.putAttribute(CLIENT_PROFILE.getValue(), credential.getClaim(CLIENT_PROFILE.getValue()));
        context.putAttribute(ENTITY_ID.getValue(), credential.getClaim(ENTITY_ID.getValue()));
        context.putAttribute(ACTION.getValue(), credential.getClaim(ACTION.getValue()));
        context.putAttribute(APPLICATION.getValue(), credential.getClaim(APPLICATION.getValue()));
    }


}
