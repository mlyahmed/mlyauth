package com.primasolutions.idp.sp.jose;

import com.primasolutions.idp.context.IContext;
import com.primasolutions.idp.context.IContextHolder;
import com.primasolutions.idp.context.IDPUser;
import com.primasolutions.idp.dao.AuthenticationInfoDAO;
import com.primasolutions.idp.domain.AuthenticationInfo;
import com.primasolutions.idp.exception.IDPException;
import com.primasolutions.idp.security.authentication.AuthenticationInfoLookuper;
import com.primasolutions.idp.token.Claims;
import com.primasolutions.idp.token.jose.JOSEAccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

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
            isTrue(isNotBlank(credential.getClaim(Claims.CLIENT_ID.getValue())), "The ClientId claim is blank");
            isTrue(isNotBlank(credential.getClaim(Claims.CLIENT_PROFILE.getValue())), "The profile claim is blank");
        }
    }

    private IContext setAttributesIntoTheContext(final JOSEAccessToken credential, final IContext context) {
        context.putAttribute(Claims.CLIENT_ID.getValue(), credential.getClaim(Claims.CLIENT_ID.getValue()));
        context.putAttribute(Claims.CLIENT_PROFILE.getValue(), credential.getClaim(Claims.CLIENT_PROFILE.getValue()));
        context.putAttribute(Claims.ENTITY_ID.getValue(), credential.getClaim(Claims.ENTITY_ID.getValue()));
        context.putAttribute(Claims.ACTION.getValue(), credential.getClaim(Claims.ACTION.getValue()));
        context.putAttribute(Claims.APPLICATION.getValue(), credential.getClaim(Claims.APPLICATION.getValue()));
        return context;
    }


}
