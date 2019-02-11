package com.hohou.federation.idp.navigation;

import com.hohou.federation.idp.application.Application;
import com.hohou.federation.idp.application.ApplicationDAO;
import com.hohou.federation.idp.application.AttributeBean;
import com.hohou.federation.idp.constants.AspectType;
import com.hohou.federation.idp.constants.TokenPurpose;
import com.hohou.federation.idp.constants.TokenStatus;
import com.hohou.federation.idp.context.IContext;
import com.hohou.federation.idp.exception.ApplicationNotFoundExc;
import com.hohou.federation.idp.exception.NotSPSAMLApplicationExc;
import com.hohou.federation.idp.token.Token;
import com.hohou.federation.idp.token.TokenDAO;
import com.hohou.federation.idp.token.TokenMapper;
import com.hohou.federation.idp.token.saml.SAMLAccessToken;
import com.hohou.federation.idp.token.saml.SAMLAccessTokenProducer;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.LinkedList;

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
        attributes.add(AttributeBean.newAttribute("SAMLResponse").setValue(access.serialize()));
        return attributes;
    }

    @Override
    public AspectType getSupportedAspect() {
        return AspectType.SP_SAML;
    }

    private void checkApplication(final Application application) {
        if (application == null) throw ApplicationNotFoundExc.newInstance();
        if (!application.getAspects().contains(AspectType.SP_SAML)) throw NotSPSAMLApplicationExc.newInstance();
    }
}
