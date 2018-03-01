package com.mlyauth.services.navigation;

import com.mlyauth.beans.AttributeBean;
import com.mlyauth.beans.AuthNavigation;
import com.mlyauth.dao.ApplicationDAO;
import com.mlyauth.domain.Application;
import com.mlyauth.exception.ApplicationNotFoundException;
import com.mlyauth.exception.NotSPSAMLApplicationException;
import com.mlyauth.security.sso.idp.saml.response.SAMLResponseGenerator;
import com.mlyauth.security.token.IDPToken;
import org.opensaml.saml2.core.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.LinkedList;

import static com.mlyauth.beans.AttributeBean.SAML_RESPONSE;
import static com.mlyauth.constants.AuthAspectType.SP_SAML;

@Service
public class SAMLNavigationService {

    @Autowired
    private ApplicationDAO applicationDAO;

    @Autowired
    private SAMLResponseGenerator responseGenerator;

    public AuthNavigation newNavigation(String appname) {
        Collection<AttributeBean> navigationAttributes = new LinkedList<>();
        AuthNavigation navigation = new AuthNavigation();
        checkApplication(applicationDAO.findByAppname(appname));
        final IDPToken<Response> token = responseGenerator.generate(applicationDAO.findByAppname(appname));
        navigationAttributes.add(SAML_RESPONSE.setValue(token.serialize()));
        navigation.setTarget(token.getTargetURL());
        navigation.setAttributes(navigationAttributes);
        return navigation;
    }

    private void checkApplication(Application application) {
        if (application == null)
            throw ApplicationNotFoundException.newInstance();

        if (!application.getAspects().contains(SP_SAML))
            throw NotSPSAMLApplicationException.newInstance();
    }
}
