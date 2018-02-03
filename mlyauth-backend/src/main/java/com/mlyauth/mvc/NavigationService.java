package com.mlyauth.mvc;

import com.mlyauth.beans.AttributeBean;
import com.mlyauth.beans.AuthNavigation;
import com.mlyauth.dao.PersonDAO;
import com.mlyauth.domain.Application;
import com.mlyauth.domain.Person;
import com.mlyauth.exception.AuthError;
import com.mlyauth.exception.AuthException;
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


    public AuthNavigation newNavigation(String appname) {

        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final UserDetails userdetails = (UserDetails) authentication.getPrincipal();
        final Person person = personDAO.findByUsername(userdetails.getUsername());
        Collection<Application> applications = person.getApplications();
        if (applications.stream().noneMatch(app -> app.getAppname().equals(appname)))
            throw AuthException.newInstance().setErrors(Arrays.asList(new AuthError("", "")));


        Collection<AttributeBean> navigationAttributes = new LinkedList<>();
        navigationAttributes.add(BASIC_AUTH_ENDPOINT.setAlias("authurl").setValue("https://localhost/j_spring_security_check"));
        navigationAttributes.add(BASIC_AUTH_USERNAME.setAlias("j_username").setValue("gestF"));
        navigationAttributes.add(BASIC_AUTH_PASSWORD.setAlias("j_password").setValue("gestF"));


        AuthNavigation authNavigation = new AuthNavigation();
        authNavigation.setAttributes(navigationAttributes);
        authNavigation.setPosterPage("post-navigation");

        return authNavigation;
    }

}
