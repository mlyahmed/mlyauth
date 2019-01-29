package com.primasolutions.idp.hooks;

import com.primasolutions.idp.application.Application;
import com.primasolutions.idp.application.ApplicationBean;
import com.primasolutions.idp.context.IContext;
import com.primasolutions.idp.person.model.Person;
import com.primasolutions.idp.person.model.PersonDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashSet;
import java.util.Set;

@Controller
@Transactional
public class HooksController {

    @Autowired
    private IContext context;

    @Autowired
    private PersonDAO personDAO;

    @RequestMapping("/")
    public String entry(final Model model) {
        return home(model);
    }

    @RequestMapping("/home")
    public String home(final Model model) {
        final Person person = personDAO.findById(context.getPerson().getId()).orElse(null);
        final Set<Application> applications = person.getApplications();
        final Set<ApplicationBean> beans = new HashSet<>();
        applications.stream().forEach(app -> beans.add(ApplicationBean.newInstance()
                .setAppname(app.getAppname()).setTitle(app.getTitle())));
        model.addAttribute("applications", beans);
        return "home";
    }

    @RequestMapping("/login.html")
    public String login() {
        return "login";
    }

    @RequestMapping("/401.html")
    public String unauthorized() {
        return "error/401";
    }

    @RequestMapping("/login-error.html")
    public String loginError(final Model model) {
        model.addAttribute("loginError", true);
        return "login";
    }

}
