package com.mlyauth.mvc;

import com.mlyauth.beans.NavigationBean;
import com.mlyauth.services.navigation.BasicNavigationService;
import com.mlyauth.services.navigation.SAMLNavigationService;
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
    private SAMLNavigationService samlNavigationService;

    @Autowired
    private BasicNavigationService basicNavigationService;

    @GetMapping("/saml/to/{appname}")
    public String samlNavigateTo(@PathVariable String appname, Model model) {
        final NavigationBean navigation = samlNavigationService.newNavigation(appname);
        model.addAttribute("navigation", navigation);
        return "post-navigation";
    }

    @GetMapping("/basic/to/{appname}")
    public String basicNavigateTo(@PathVariable String appname, Model model) {
        final NavigationBean navigation = basicNavigationService.newNavigation(appname);
        model.addAttribute("navigation", navigation);
        return "post-navigation";
    }

}
