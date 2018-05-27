package com.primasolutions.idp.token.jose;

import com.primasolutions.idp.constants.AspectType;
import com.primasolutions.idp.token.OAuthAccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/token/jose")
public class JOSETokenController {

    @Autowired
    private JOSETokenDecoderImpl tokenDecoder;

    @Autowired
    private JOSETokenServiceImpl tokenService;

    @PostMapping(value = "/access", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody
    OAuthAccessToken newAccessToken(@RequestBody final String refresh) {
        return tokenService.refreshAccess(tokenDecoder.decodeRefresh(refresh, AspectType.CL_JOSE));
    }

    @PostMapping(value = "/access/_check", consumes = MediaType.TEXT_PLAIN_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void checkAccessToken(@RequestBody final String access) {
        tokenService.checkAccess(access);
    }
}
