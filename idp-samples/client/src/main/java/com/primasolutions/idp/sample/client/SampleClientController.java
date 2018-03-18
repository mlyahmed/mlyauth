package com.primasolutions.idp.sample.client;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SampleClientController {

    @GetMapping("/")
    public String joseForm(Model model) {
        return "main";
    }

}
