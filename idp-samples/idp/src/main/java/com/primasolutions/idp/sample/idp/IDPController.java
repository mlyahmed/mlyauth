package com.primasolutions.idp.sample.idp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Controller
public class IDPController {

    @Value("${sp.endpoint}")
    private String targetURL;

    @Autowired
    private IDPService idpService;

    @Autowired
    private IDPTokenInitializer tokenInitializer;

    @GetMapping("/idp-form")
    public String greetingForm(Model model) {
        model.addAttribute("token", tokenInitializer.newToken());
        return "idp-form";
    }

    @PostMapping("/idp-navigation")
    public String greetingSubmit(@ModelAttribute Token token, Model model, HttpServletResponse response) {
        Navigation navigation = new Navigation();
        navigation.setTarget(targetURL);
        model.addAttribute("navigation", navigation);
        Cookie foo = new Cookie("Bearer", idpService.generateJOSEAccess(token));
        foo.setMaxAge(30);
        response.addCookie(foo);
        return "idp-navigation";
    }




}
