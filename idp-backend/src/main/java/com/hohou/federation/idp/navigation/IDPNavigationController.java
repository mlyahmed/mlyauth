package com.hohou.federation.idp.navigation;

import com.hohou.federation.idp.constants.AspectType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/navigate/forward")
public class IDPNavigationController {

    private final Map<AspectType, IDPNavigationService> services = new HashMap<>();

    @Autowired
    public void init(final Collection<IDPNavigationService> services) {
        services.forEach(service -> this.services.put(service.getSupportedAspect(), service));
    }

    @GetMapping("/to/{appname}")
    public String navigateTo(@PathVariable final String appname, final Model model) {
        return samlNavigateTo(appname, model);
    }

    @GetMapping("/saml/to/{appname}")
    public String samlNavigateTo(@PathVariable final String appname, final Model model) {
        final NavigationBean navigation = services.get(AspectType.SP_SAML).newNavigation(appname);
        model.addAttribute("navigation", navigation);
        return "post-navigation";
    }

    @GetMapping("/basic/to/{appname}")
    public String basicNavigateTo(@PathVariable final String appname, final Model model) {
        final NavigationBean navigation = services.get(AspectType.SP_BASIC).newNavigation(appname);
        model.addAttribute("navigation", navigation);
        return "post-navigation";
    }

}
