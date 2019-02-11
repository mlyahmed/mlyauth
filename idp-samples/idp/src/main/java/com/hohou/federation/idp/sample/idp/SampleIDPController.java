package com.hohou.federation.idp.sample.idp;

import com.hohou.federation.idp.sample.idp.jose.IDPJOSETokenInitializer;
import com.hohou.federation.idp.sample.idp.navigation.IDPNavigationService;
import com.hohou.federation.idp.sample.idp.saml.IDPSAMLTokenInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class SampleIDPController {

    @Autowired
    private IDPJOSETokenInitializer joseInitializer;

    @Autowired
    private IDPSAMLTokenInitializer samlInitializer;

    @Autowired
    private IDPNavigationService navigationService;

    @GetMapping("/login")
    public String login(){
        return "login";
    }

    @GetMapping("/")
    public String entry(Model model){
        return samlForm(model);
    }

    @GetMapping("/idp-simulator")
    public String simulator(Model model) {
        model.addAttribute("token", joseInitializer.newToken());
        return "idp-simulator";
    }

    @GetMapping("/idp-form-jose")
    public String joseForm(Model model) {
        model.addAttribute("token", joseInitializer.newToken());
        return "idp-simulator";
    }

    @GetMapping("/idp-form-saml")
    public String samlForm(Model model) {
        model.addAttribute("token", samlInitializer.newToken());
        return "idp-simulator";
    }

    @PostMapping("/idp-navigation")
    public String greetingSubmit(@ModelAttribute SampleIDPToken token, Model model, HttpServletRequest request, HttpServletResponse response) {
        model.addAttribute("navigation", navigationService.buildNavigation(token, request, response));
        return "idp-navigation";
    }

}
