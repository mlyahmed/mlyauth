package com.primasolutions.idp.navigation;

import com.primasolutions.idp.application.Application;
import com.primasolutions.idp.application.ApplicationDAO;
import com.primasolutions.idp.application.AttributeBean;
import com.primasolutions.idp.constants.AspectType;
import com.primasolutions.idp.constants.TokenPurpose;
import com.primasolutions.idp.constants.TokenStatus;
import com.primasolutions.idp.context.IContext;
import com.primasolutions.idp.exception.ApplicationNotFoundException;
import com.primasolutions.idp.exception.NotSPSAMLApplicationException;
import com.primasolutions.idp.token.Token;
import com.primasolutions.idp.token.TokenDAO;
import com.primasolutions.idp.token.TokenMapper;
import com.primasolutions.idp.token.saml.SAMLAccessToken;
import com.primasolutions.idp.token.saml.SAMLAccessTokenProducer;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.LinkedList;

import static com.primasolutions.idp.application.AttributeBean.newAttribute;
import static com.primasolutions.idp.constants.AspectType.SP_SAML;

@Service
public class IDPSAMLNavigationService extends AbstractIDPNavigationService {

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
    public NavigationBean process(final String appname) {
        checkApplication(applicationDAO.findByAppname(appname));
        final SAMLAccessToken access = generateAccess(appname);
        return buildNavigation(access, saveToken(appname, access));
    }

    private SAMLAccessToken generateAccess(final String appname) {
        return responseGenerator.produce(applicationDAO.findByAppname(appname));
    }

    private Token saveToken(final String appname, final SAMLAccessToken access) {
        Token token = tokenMapper.toToken(access);
        token.setPurpose(TokenPurpose.NAVIGATION);
        token.setApplication(applicationDAO.findByAppname(appname));
        token.setSession(context.getAuthenticationSession());
        token.setChecksum(DigestUtils.sha256Hex(access.serialize()));
        token.setStatus(TokenStatus.CHECKED);
        token = tokenDAO.save(token);
        return token;
    }

    private NavigationBean buildNavigation(final SAMLAccessToken access, final Token token) {
        NavigationBean navigation = new NavigationBean();
        navigation.setTarget(access.getTargetURL());
        navigation.setTokenId(token.getId());
        navigation.setAttributes(buildAttributes(access));
        return navigation;
    }

    private Collection<AttributeBean> buildAttributes(final SAMLAccessToken access) {
        Collection<AttributeBean> attributes = new LinkedList<>();
        attributes.add(newAttribute("SAMLResponse").setValue(access.serialize()));
        return attributes;
    }

    @Override
    public AspectType getSupportedAspect() {
        return SP_SAML;
    }

    private void checkApplication(final Application application) {
        if (application == null) throw ApplicationNotFoundException.newInstance();
        if (!application.getAspects().contains(SP_SAML)) throw NotSPSAMLApplicationException.newInstance();
    }
}
