package com.primasolutions.idp.sample.client.auto;

import com.primasolutions.idp.sample.client.SampleClientJOSETokenInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auto")
public class SampleClientAutoController {

    @Autowired
    private SampleClientJOSETokenInitializer initializer;

    @GetMapping
    public String auto(Model model) {
        model.addAttribute("token", initializer.newToken());
        return "auto/auto";
    }

    @GetMapping("/eligibility")
    public String eligibility(Model model) {
        model.addAttribute("token", initializer.newToken());
        return "auto/auto-ws-eligibility";
    }

}
