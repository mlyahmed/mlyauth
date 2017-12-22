package com.mlyauth.mvc;

import com.mlyauth.dao.PersonDAO;
import com.mlyauth.domain.Application;
import com.mlyauth.domain.Person;
import com.mlyauth.exception.AuthError;
import com.mlyauth.exception.AuthException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Arrays;
import java.util.Collection;

import static com.mlyauth.beans.AttributeBean.*;

@Controller
@RequestMapping("/navigate")
public class NavigationController {


    @Autowired
    private PersonDAO personDAO;

    @GetMapping("/to/{appname}")
    public String navigateTo(@PathVariable String appname, Model model) {

        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final UserDetails userdetails = (UserDetails) authentication.getPrincipal();
        final Person person = personDAO.findByUsername(userdetails.getUsername());
        Collection<Application> applications = person.getApplications();
        if (applications.stream().noneMatch(app -> app.getAppname().equals(appname)))
            throw AuthException.newInstance().setErrors(Arrays.asList(new AuthError("", "")));


        model.addAttribute(BASIC_AUTH_ENDPOINT.getCode(), BASIC_AUTH_ENDPOINT.setAlias("authurl").setValue("https://localhost/j_spring_security_check"));
        model.addAttribute(BASIC_AUTH_USERNAME.getCode(), BASIC_AUTH_USERNAME.setAlias("j_username").setValue("gestF"));
        model.addAttribute(BASIC_AUTH_PASSWORD.getCode(), BASIC_AUTH_PASSWORD.setAlias("j_password").setValue("gestF"));

        return "post-navigation";
    }


}
