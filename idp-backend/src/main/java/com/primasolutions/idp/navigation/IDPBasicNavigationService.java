package com.primasolutions.idp.navigation;

import com.primasolutions.idp.beans.AttributeBean;
import com.primasolutions.idp.beans.NavigationBean;
import com.primasolutions.idp.constants.AspectType;
import com.primasolutions.idp.domain.Application;
import com.primasolutions.idp.domain.Person;
import com.primasolutions.idp.exception.IDPException;
import com.primasolutions.idp.person.PersonLookuper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.LinkedList;

import static com.primasolutions.idp.beans.AttributeBean.newAttribute;
import static com.primasolutions.idp.constants.AspectAttribute.SP_BASIC_PASSWORD;
import static com.primasolutions.idp.constants.AspectAttribute.SP_BASIC_SSO_URL;
import static com.primasolutions.idp.constants.AspectAttribute.SP_BASIC_USERNAME;

@Service
@Transactional
public class IDPBasicNavigationService extends AbstractIDPNavigationService {

    @Autowired
    private PersonLookuper personLookuper;


    @Override
    public NavigationBean process(final String appname) {

        Collection<AttributeBean> navigationAttributes = new LinkedList<>();
        NavigationBean navigation = new NavigationBean();
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final UserDetails userdetails = (UserDetails) authentication.getPrincipal();
        final Person person = personLookuper.byEmail(userdetails.getUsername());
        Collection<Application> applications = person.getApplications();
        if (applications.stream().noneMatch(a -> a.getAppname().equals(appname))) throw IDPException.newInstance("");

        navigationAttributes.add(newAttribute(SP_BASIC_SSO_URL.getValue())
                .setAlias("authurl").setValue("https://localhost/j_spring_security_check"));
        navigationAttributes.add(newAttribute(SP_BASIC_USERNAME.getValue()).setAlias("j_username").setValue("gestF"));
        navigationAttributes.add(newAttribute(SP_BASIC_PASSWORD.getValue()).setAlias("j_password").setValue("gestF"));
        navigation.setAttributes(navigationAttributes);
        navigation.setTarget("https://localhost/j_spring_security_check");

        return navigation;
    }

    @Override
    public AspectType getSupportedAspect() {
        return AspectType.SP_BASIC;
    }

}
