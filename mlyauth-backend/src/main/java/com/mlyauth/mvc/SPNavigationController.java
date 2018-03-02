package com.mlyauth.mvc;

import com.mlyauth.beans.NavigationBean;
import com.mlyauth.constants.AuthAspectType;
import com.mlyauth.services.navigation.ISPNavigationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.mlyauth.constants.AuthAspectType.AUTH_BASIC;
import static com.mlyauth.constants.AuthAspectType.SP_SAML;

@Controller
@RequestMapping("/navigate")
public class SPNavigationController {

    private final Map<AuthAspectType, ISPNavigationService> services = new HashMap<>();

    @Autowired
    public void init(Collection<ISPNavigationService> services) {
        services.forEach(service -> this.services.put(service.getSupportedAspect(), service));
    }

    @GetMapping("/saml/to/{appname}")
    public String samlNavigateTo(@PathVariable String appname, Model model) {
        final NavigationBean navigation = services.get(SP_SAML).newNavigation(appname);
        model.addAttribute("navigation", navigation);
        return "post-navigation";
    }

    @GetMapping("/basic/to/{appname}")
    public String basicNavigateTo(@PathVariable String appname, Model model) {
        final NavigationBean navigation = services.get(AUTH_BASIC).newNavigation(appname);
        model.addAttribute("navigation", navigation);
        return "post-navigation";
    }

}