package com.mlyauth.services;

import com.mlyauth.beans.AttributeBean;
import com.mlyauth.beans.AuthNavigation;
import com.mlyauth.dao.ApplicationDAO;
import com.mlyauth.dao.PersonDAO;
import com.mlyauth.domain.Application;
import com.mlyauth.domain.Person;
import com.mlyauth.exception.AuthError;
import com.mlyauth.exception.AuthException;
import com.mlyauth.security.sso.SAMLHelper;
import com.mlyauth.security.sso.idp.saml.response.IDPSAMLResponseGenerator;
import org.opensaml.saml2.core.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import static com.mlyauth.beans.AttributeBean.*;

@Service
public class NavigationService {

    @Autowired
    private PersonDAO personDAO;

    @Autowired
    private ApplicationDAO applicationDAO;

    @Autowired
    private IDPSAMLResponseGenerator idpsamlResponseGenerator;

    @Autowired
    private SAMLHelper samlHelper;

    public AuthNavigation newNavigation(String protocole, String appname) {

        Collection<AttributeBean> navigationAttributes = new LinkedList<>();
        AuthNavigation authNavigation = new AuthNavigation();

        if ("basic".equalsIgnoreCase(protocole)) {

            final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            final UserDetails userdetails = (UserDetails) authentication.getPrincipal();
            final Person person = personDAO.findByUsername(userdetails.getUsername());
            Collection<Application> applications = person.getApplications();
            if (applications.stream().noneMatch(app -> app.getAppname().equals(appname)))
                throw AuthException.newInstance().setErrors(Arrays.asList(new AuthError("", "")));


            navigationAttributes.add(BASIC_AUTH_ENDPOINT.setAlias("authurl").setValue("https://localhost/j_spring_security_check"));
            navigationAttributes.add(BASIC_AUTH_USERNAME.setAlias("j_username").setValue("gestF"));
            navigationAttributes.add(BASIC_AUTH_PASSWORD.setAlias("j_password").setValue("gestF"));

        } else {

            try {

                final Application application = applicationDAO.findByAppname(appname);
                final Response response = idpsamlResponseGenerator.generate(application);
                navigationAttributes.add(SAML_RESPONSE.setAlias("SAMLResponse").setValue(org.opensaml.xml.util.Base64.encodeBytes(samlHelper.toString(response).getBytes())));
                authNavigation.setTarget(response.getDestination());

            } catch (Exception e) {
                throw AuthException.newInstance().setErrors(Arrays.asList(AuthError.newInstance("SAML_RESPONSE_ERR")));
            }

        }


        authNavigation.setAttributes(navigationAttributes);
        authNavigation.setPosterPage("post-navigation");

        return authNavigation;
    }

}
