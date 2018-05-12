package com.mlyauth.sp.jose;

import com.mlyauth.context.IContext;
import com.mlyauth.context.IContextHolder;
import com.mlyauth.context.IDPUser;
import com.mlyauth.dao.AuthenticationInfoDAO;
import com.mlyauth.domain.AuthenticationInfo;
import com.mlyauth.exception.IDPException;
import com.mlyauth.security.authentication.AuthenticationInfoLookuper;
import com.mlyauth.token.jose.JOSEAccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import static com.mlyauth.token.Claims.ACTION;
import static com.mlyauth.token.Claims.APPLICATION;
import static com.mlyauth.token.Claims.CLIENT_ID;
import static com.mlyauth.token.Claims.CLIENT_PROFILE;
import static com.mlyauth.token.Claims.ENTITY_ID;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

@Component
@Transactional
public class SPJOSEUserDetailsServiceImpl implements SPJOSEUserDetailsService {

    @Autowired
    private AuthenticationInfoLookuper authenticationInfoLookuper;

    @Autowired
    private AuthenticationInfoDAO authenticationInfoDAO;

    @Autowired
    private IContextHolder contextHolder;

    @Override
    public IDPUser loadUserByJOSE(final JOSEAccessToken credential) throws UsernameNotFoundException {
        checkParams(credential);
        return new IDPUser(setAttributesIntoTheContext(credential, loadContext(credential)));
    }

    private IContext loadContext(final JOSEAccessToken credential) {
        final AuthenticationInfo authenticationInfo = authenticationInfoLookuper.byLogin(credential.getSubject());
        Assert.notNull(authenticationInfo, "No AuthenticationInfo found for " + credential.getSubject());

        if (authenticationInfo.isPerson()) {
            return contextHolder.newPersonContext(authenticationInfo.getPerson());
        } else if (authenticationInfo.isApplication()) {
            return contextHolder.newApplicationContext(authenticationInfo.getApplication());
        }

        throw IDPException.newInstance("AuthenticationInfo found (" + credential.getSubject() + ") is not valid.");
    }

    private void checkParams(final JOSEAccessToken credential) {
        notNull(credential, "The JOSE Token is null");
        isTrue(isNotBlank(credential.getSubject()), "The JOSE Token Subject is blank");
        if ("SSO".equals(credential.getBP())) {
            isTrue(isNotBlank(credential.getClaim(CLIENT_ID.getValue())), "The ClientId claim is blank");
            isTrue(isNotBlank(credential.getClaim(CLIENT_PROFILE.getValue())), "The profile claim is blank");
        }
    }

    private IContext setAttributesIntoTheContext(final JOSEAccessToken credential, final IContext context) {
        context.putAttribute(CLIENT_ID.getValue(), credential.getClaim(CLIENT_ID.getValue()));
        context.putAttribute(CLIENT_PROFILE.getValue(), credential.getClaim(CLIENT_PROFILE.getValue()));
        context.putAttribute(ENTITY_ID.getValue(), credential.getClaim(ENTITY_ID.getValue()));
        context.putAttribute(ACTION.getValue(), credential.getClaim(ACTION.getValue()));
        context.putAttribute(APPLICATION.getValue(), credential.getClaim(APPLICATION.getValue()));
        return context;
    }


}
