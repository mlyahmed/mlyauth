package com.primasolutions.idp.sample.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/token")
public class SampleClientTokenController {

    @Autowired
    private SampleClientTokenService clientTokenService;


    @Autowired
    private SampleClientJOSETokenInitializer tokenInitializer;

    @PostMapping("/refreshAccess")
    public String refreshAccess() {
        final SampleClientToken token = tokenInitializer.newToken();
        return clientTokenService.refreshAccess(token);
    }

}
