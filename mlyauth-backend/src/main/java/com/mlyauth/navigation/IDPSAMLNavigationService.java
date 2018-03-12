package com.mlyauth.navigation;

import com.mlyauth.beans.AttributeBean;
import com.mlyauth.beans.NavigationBean;
import com.mlyauth.constants.AspectType;
import com.mlyauth.constants.TokenPurpose;
import com.mlyauth.context.IContext;
import com.mlyauth.dao.ApplicationDAO;
import com.mlyauth.dao.TokenDAO;
import com.mlyauth.domain.Application;
import com.mlyauth.domain.Token;
import com.mlyauth.exception.ApplicationNotFoundException;
import com.mlyauth.exception.NotSPSAMLApplicationException;
import com.mlyauth.token.TokenMapper;
import com.mlyauth.token.saml.SAMLAccessToken;
import com.mlyauth.token.saml.SAMLAccessTokenProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.LinkedList;

import static com.mlyauth.beans.AttributeBean.newAttribute;
import static com.mlyauth.constants.AspectType.SP_SAML;

@Service
public class IDPSAMLNavigationService extends IDPAbstractNavigationService {

    @Autowired
    private TokenDAO tokenDAO;

    @Autowired
    private ApplicationDAO applicationDAO;

    @Autowired
    private SAMLAccessTokenProducer responseGenerator;

    @Autowired
    private TokenMapper tokenMapper;

    @Autowired
    private IContext context;

    @Override
    public NavigationBean process(String appname) {
        checkApplication(applicationDAO.findByAppname(appname));
        final SAMLAccessToken access = generateAccess(appname);
        return buildNavigation(access, saveToken(appname, access));
    }

    private SAMLAccessToken generateAccess(String appname) {
        return responseGenerator.produce(applicationDAO.findByAppname(appname));
    }

    private Token saveToken(String appname, SAMLAccessToken access) {
        Token token = tokenMapper.toToken(access);
        token.setPurpose(TokenPurpose.NAVIGATION);
        token.setApplication(applicationDAO.findByAppname(appname));
        token.setSession(context.getAuthenticationSession());
        token = tokenDAO.save(token);
        return token;
    }

    private NavigationBean buildNavigation(SAMLAccessToken access, Token token) {
        NavigationBean navigation = new NavigationBean();
        navigation.setTarget(access.getTargetURL());
        navigation.setTokenId(token.getId());
        navigation.setAttributes(buildAttributes(access));
        return navigation;
    }

    private Collection<AttributeBean> buildAttributes(SAMLAccessToken access) {
        Collection<AttributeBean> attributes = new LinkedList<>();
        attributes.add(newAttribute("SAMLResponse").setValue(access.serialize()));
        return attributes;
    }

    @Override
    public AspectType getSupportedAspect() {
        return SP_SAML;
    }

    private void checkApplication(Application application) {
        if (application == null)
            throw ApplicationNotFoundException.newInstance();

        if (!application.getAspects().contains(SP_SAML))
            throw NotSPSAMLApplicationException.newInstance();
    }
}
