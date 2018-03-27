package com.mlyauth.token.jose;

import com.mlyauth.beans.TokenBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

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
    TokenBean newAccessToken(@RequestBody String refresh) {
        JOSERefreshToken refreshToken = tokenDecoder.decodeRefresh(refresh, CL_JOSE);
        return joseTokenService.refreshAccess(refreshToken);
    }
}
