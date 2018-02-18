package com.mlyauth.services;

import com.mlyauth.beans.AttributeBean;
import com.mlyauth.beans.AuthNavigation;
import com.mlyauth.dao.ApplicationDAO;
import com.mlyauth.domain.Application;
import com.mlyauth.exception.ApplicationNotFound;
import com.mlyauth.exception.NotSPSAMLApplication;
import com.mlyauth.security.sso.SAMLHelper;
import com.mlyauth.security.sso.idp.saml.response.SAMLResponseGenerator;
import org.opensaml.saml2.core.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.LinkedList;

import static com.mlyauth.beans.AttributeBean.SAML_RESPONSE;
import static com.mlyauth.constants.AuthAspectType.SP_SAML;
import static org.opensaml.xml.util.Base64.encodeBytes;

@Service
public class SAMLNavigationService {

    @Autowired
    private ApplicationDAO applicationDAO;

    @Autowired
    private SAMLResponseGenerator responseGenerator;

    @Autowired
    private SAMLHelper samlHelper;

    public AuthNavigation newNavigation(String appname) {
        Collection<AttributeBean> navigationAttributes = new LinkedList<>();
        AuthNavigation navigation = new AuthNavigation();
        checkApplication(applicationDAO.findByAppname(appname));
        final Response response = responseGenerator.generate(applicationDAO.findByAppname(appname));
        navigationAttributes.add(SAML_RESPONSE.setValue(encodeBytes(samlHelper.toString(response).getBytes())));
        navigation.setTarget(response.getDestination());
        navigation.setAttributes(navigationAttributes);
        return navigation;
    }

    private void checkApplication(Application application) {
        if (application == null)
            throw ApplicationNotFound.newInstance();

        if (!application.getAspects().contains(SP_SAML))
            throw NotSPSAMLApplication.newInstance();
    }
}
