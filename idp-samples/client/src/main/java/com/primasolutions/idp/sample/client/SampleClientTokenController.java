package com.primasolutions.idp.sample.client;

import com.google.common.base.Stopwatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@RestController
@RequestMapping("/token")
public class SampleClientTokenController {

    @Autowired
    private SampleClientTokenService clientTokenService;


    @Autowired
    private SampleClientJOSETokenInitializer tokenInitializer;

    @PostMapping("/refreshAccess")
    public SampleClientTokenBean refreshAccess(@RequestBody SampleClientToken token) {
        final Stopwatch started = Stopwatch.createStarted();
        final SampleClientTokenBean accessToken = clientTokenService.refreshAccess(tokenInitializer.newToken().setDelegator(token.getDelegator()));
        accessToken.setElapsed(started.elapsed(MILLISECONDS));
        return accessToken;
    }

}
