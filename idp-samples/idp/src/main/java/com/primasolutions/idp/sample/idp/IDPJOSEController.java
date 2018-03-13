package com.primasolutions.idp.sample.idp;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class IDPJOSEController {

    @GetMapping("/idp-jose")
    public String greetingForm(Model model) {
        model.addAttribute("token", new Token());
        return "idp-jose-form";
    }

    @PostMapping("/idp-jose")
    public String greetingSubmit(@ModelAttribute Token token, Model model, HttpServletRequest request, HttpServletResponse response) {
        Navigation navigation = new Navigation();
        navigation.setTarget("http://localhost:16666/sp/jose/sso");
        model.addAttribute("navigation", navigation);
        return "idp-jose-submit";
    }

}
