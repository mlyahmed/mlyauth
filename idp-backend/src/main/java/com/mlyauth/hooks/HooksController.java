package com.mlyauth.hooks;

import com.mlyauth.beans.ApplicationBean;
import com.mlyauth.context.IContext;
import com.mlyauth.domain.Application;
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

    @RequestMapping("/")
    public String entry(Model model) {
        return home(model);
    }

    @RequestMapping("/home")
    public String home(Model model) {
        final Set<Application> applications = context.getPerson().getApplications();
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
    public String Unauthorized() {
        return "error/401";
    }

    @RequestMapping("/login-error.html")
    public String loginError(Model model) {
        model.addAttribute("loginError", true);
        return "login";
    }

}
