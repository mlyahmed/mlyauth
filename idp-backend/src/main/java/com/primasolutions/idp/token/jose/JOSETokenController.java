package com.primasolutions.idp.token.jose;

import com.primasolutions.idp.constants.AspectType;
import com.primasolutions.idp.token.TokenBean;
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
    private JOSETokenDecoder tokenDecoder;

    @Autowired
    private JOSETokenService joseTokenService;

    @PostMapping(value = "/access", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody
    TokenBean newAccessToken(@RequestBody final String refresh) {
        return joseTokenService.refreshAccess(tokenDecoder.decodeRefresh(refresh, AspectType.CL_JOSE));
    }

    @PostMapping(value = "/access/_check", consumes = MediaType.TEXT_PLAIN_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void checkAccessToken(@RequestBody final String access) {
        joseTokenService.checkAccess(access);
    }
}
