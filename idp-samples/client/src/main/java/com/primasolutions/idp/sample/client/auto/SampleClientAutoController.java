package com.primasolutions.idp.sample.client.auto;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auto")
public class SampleClientAutoController {

    @GetMapping
    public String auto(Model model) {
        return "auto/auto";
    }

    @GetMapping("/eligibility")
    public String eligibility(Model model) {
        return "auto/auto-ws-eligibility";
    }

}
