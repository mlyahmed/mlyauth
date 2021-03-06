package com.hohou.federation.idp.sample.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SampleClientController {

    @Autowired
    private SampleClientJOSETokenInitializer initializer;

    @GetMapping("/")
    public String joseForm(Model model) {
        model.addAttribute("token", initializer.newToken());
        return "auto/auto";
    }

}
