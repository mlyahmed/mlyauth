package com.mlyauth.mvc;

import com.mlyauth.beans.AuthNavigation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/navigate")
public class NavigationController {

    @Autowired
    private NavigationService navigationService;

    @GetMapping("/to/{appname}")
    public String navigateTo(@PathVariable String appname, Model model) {
        final AuthNavigation authNavigation = navigationService.newNavigation(appname);
        authNavigation.getAttributes().forEach(att -> model.addAttribute(att.getCode(), att));
        return authNavigation.getPosterPage();
    }


}
