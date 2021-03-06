package com.hohou.federation.idp.authentication.sp.jose;

import com.hohou.federation.idp.authentication.AuthInfo;
import com.hohou.federation.idp.authentication.AuthInfoDAO;
import com.hohou.federation.idp.authentication.AuthenticationInfoLookuper;
import com.hohou.federation.idp.context.IContext;
import com.hohou.federation.idp.context.IContextHolder;
import com.hohou.federation.idp.context.IDPUser;
import com.hohou.federation.idp.exception.IDPException;
import com.hohou.federation.idp.token.Claims;
import com.hohou.federation.idp.token.jose.JOSEAccessToken;
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
    private AuthInfoDAO authInfoDAO;

    @Autowired
    private IContextHolder contextHolder;

    @Override
    public IDPUser loadUserByJOSE(final JOSEAccessToken credential) throws UsernameNotFoundException {
        checkParams(credential);
        return new IDPUser(setAttributesIntoTheContext(credential, loadContext(credential)));
    }

    private IContext loadContext(final JOSEAccessToken credential) {
        final AuthInfo authenticationInfo = authenticationInfoLookuper.byLogin(credential.getSubject());
        Assert.notNull(authenticationInfo, "No AuthInfo found for " + credential.getSubject());

        if (authenticationInfo.isPerson()) {
            return contextHolder.newPersonContext(authenticationInfo.getPerson());
        } else if (authenticationInfo.isApplication()) {
            return contextHolder.newApplicationContext(authenticationInfo.getApplication());
        }

        throw IDPException.newInstance("AuthInfo found (" + credential.getSubject() + ") is not valid.");
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
