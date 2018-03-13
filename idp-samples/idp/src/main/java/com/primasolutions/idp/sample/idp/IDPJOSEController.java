package com.primasolutions.idp.sample.idp;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class IDPJOSEController {

    @GetMapping("/idp-jose")
    public String greetingForm(Model model) {
        model.addAttribute("token", new Token());
        return "idp-jose-form";
    }

    @PostMapping("/idp-jose")
    public String greetingSubmit(@ModelAttribute Token token) {
        return "idp-jose-submit";
    }

}
