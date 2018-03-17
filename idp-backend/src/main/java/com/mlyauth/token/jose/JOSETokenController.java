package com.mlyauth.token.jose;

import com.mlyauth.constants.TokenVerdict;
import com.mlyauth.context.IContext;
import com.mlyauth.credentials.CredentialManager;
import com.mlyauth.dao.ApplicationAspectAttributeDAO;
import com.mlyauth.dao.TokenDAO;
import com.mlyauth.domain.Application;
import com.mlyauth.domain.ApplicationAspectAttribute;
import com.mlyauth.domain.Token;
import com.mlyauth.token.ITokenFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.security.PublicKey;
import java.util.List;
import java.util.UUID;

import static com.mlyauth.constants.AspectType.CL_JOSE;
import static com.mlyauth.constants.AspectType.RS_JOSE;
import static com.mlyauth.constants.AttributeType.ENTITYID;

@RestController
@RequestMapping("/token/jose")
public class JOSETokenController {

    @Value("${idp.jose.entityId}")
    private String localEntityId;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private IContext context;

    @Autowired
    private CredentialManager credentialManager;

    @Autowired
    private ApplicationAspectAttributeDAO attributeDAO;

    @Autowired
    private JOSEHelper joseHelper;

    @Autowired
    private ITokenFactory tokenFactory;

    @Autowired
    private TokenDAO tokenDAO;

    @PostMapping(value = "/access", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody
    String newAccessToken(@RequestBody String refresh) {

        final String issuer = joseHelper.loadIssuer(refresh, credentialManager.getLocalPrivateKey());
        final Application application = context.getApplication();

        final List<ApplicationAspectAttribute> attributes = attributeDAO.findByAppAndAspect(application.getId(), CL_JOSE.getValue());
        final ApplicationAspectAttribute entityId = attributes.stream().filter(attr -> attr.getAttributeCode().getType() == ENTITYID).findFirst().get();
        Assert.notNull(entityId, "entity ID not found");
        Assert.isTrue(entityId.getValue().equals(issuer), "Untrusted peer");

        final PublicKey clientKey = credentialManager.getPeerKey(issuer, CL_JOSE);
        final JOSERefreshToken refreshToken = tokenFactory.createJOSERefreshToken(refresh, credentialManager.getLocalPrivateKey(), clientKey);
        refreshToken.decipher();

        final List<Token> readyRefreshs = tokenDAO.findReadyJOSERefreshToken(application.getId());
        final Token readyRefresh = readyRefreshs.stream().filter(token -> passwordEncoder.matches(refreshToken.getStamp(), token.getStamp())).findFirst().orElse(null);


        final PublicKey resourceServerKey = credentialManager.getPeerKey(refreshToken.getAudience(), RS_JOSE);
        final JOSEAccessToken accessToken = tokenFactory.createJOSEAccessToken(credentialManager.getLocalPrivateKey(), resourceServerKey);
        accessToken.setStamp(UUID.randomUUID().toString());
        accessToken.setIssuer(localEntityId);
        accessToken.setAudience(refreshToken.getAudience());
        accessToken.setVerdict(TokenVerdict.SUCCESS);
        accessToken.cypher();

        return accessToken.serialize();
    }
}
