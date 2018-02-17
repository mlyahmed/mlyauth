package com.mlyauth.services;

import com.mlyauth.beans.AttributeBean;
import com.mlyauth.beans.AuthNavigation;
import com.mlyauth.dao.ApplicationDAO;
import com.mlyauth.domain.Application;
import com.mlyauth.exception.AuthError;
import com.mlyauth.exception.IDPException;
import com.mlyauth.exception.NotSPSAMLApplication;
import com.mlyauth.security.sso.SAMLHelper;
import com.mlyauth.security.sso.idp.saml.response.SAMLResponseGenerator;
import org.opensaml.saml2.core.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
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
        AuthNavigation authNavigation = new AuthNavigation();
        final Application application = applicationDAO.findByAppname(appname);

        if (!application.getAspects().contains(SP_SAML))
            throw NotSPSAMLApplication.newInstance();

        try {

            final Response response = responseGenerator.generate(application);
            navigationAttributes.add(SAML_RESPONSE.setAlias("SAMLResponse").setValue(encodeBytes(samlHelper.toString(response).getBytes())));
            authNavigation.setTarget(response.getDestination());

        } catch (Exception e) {
            throw IDPException.newInstance().setErrors(Arrays.asList(AuthError.newInstance("SAML_RESPONSE_ERR")));
        }


        authNavigation.setAttributes(navigationAttributes);
        return authNavigation;
    }
}
