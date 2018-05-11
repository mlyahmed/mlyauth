package com.mlyauth.token.jose;

import com.mlyauth.beans.TokenBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static com.mlyauth.constants.AspectType.CL_JOSE;

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
        return joseTokenService.refreshAccess(tokenDecoder.decodeRefresh(refresh, CL_JOSE));
    }

    @PostMapping(value = "/access/_check", consumes = MediaType.TEXT_PLAIN_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public void checkAccessToken(@RequestBody final String access) {
        joseTokenService.checkAccess(access);
    }
}
