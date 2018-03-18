package com.mlyauth.token.jose;

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

    @PostMapping(value = "/access", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody
    String newAccessToken(@RequestBody String refresh) {
        JOSERefreshToken refreshToken = tokenDecoder.decodeRefresh(refresh, CL_JOSE);
        final JOSEAccessToken accessToken = joseTokenService.refreshAccess(refreshToken);
        return accessToken.serialize();
    }
}
