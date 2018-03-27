package com.primasolutions.idp.sample.client;

import com.google.common.base.Stopwatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@RestController
@RequestMapping("/token")
public class SampleClientTokenController {

    @Autowired
    private SampleClientTokenService clientTokenService;


    @Autowired
    private SampleClientJOSETokenInitializer tokenInitializer;

    @PostMapping("/refreshAccess")
    public Map<String, String> refreshAccess() {
        Map<String, String> access = new HashMap<>();
        final Stopwatch started = Stopwatch.createStarted();
        final SampleClientToken token = tokenInitializer.newToken();
        access.put("access", clientTokenService.refreshAccess(token));
        access.put("elapsed", started.elapsed(MILLISECONDS)+"");
        return access;
    }

}
