package com.mlyauth.token.jose;

import com.mlyauth.constants.TokenVerdict;
import com.mlyauth.context.IContext;
import com.mlyauth.credentials.CredentialManager;
import com.mlyauth.dao.ApplicationAspectAttributeDAO;
import com.mlyauth.dao.ApplicationDAO;
import com.mlyauth.dao.TokenDAO;
import com.mlyauth.domain.Application;
import com.mlyauth.domain.ApplicationAspectAttribute;
import com.mlyauth.domain.Token;
import com.mlyauth.token.ITokenFactory;
import com.mlyauth.token.TokenMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.security.PublicKey;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.mlyauth.constants.AspectAttribute.RS_JOSE_ENTITY_ID;
import static com.mlyauth.constants.AspectType.CL_JOSE;
import static com.mlyauth.constants.AspectType.RS_JOSE;
import static com.mlyauth.constants.AttributeType.ENTITYID;
import static com.mlyauth.constants.TokenNorm.JOSE;
import static com.mlyauth.constants.TokenPurpose.DELEGATION;
import static com.mlyauth.constants.TokenStatus.READY;
import static com.mlyauth.constants.TokenType.REFRESH;

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

    @Autowired
    private TokenMapper tokenMapper;

    @Autowired
    private ApplicationDAO applicationDAO;

    @PostMapping(value = "/access", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody
    String newAccessToken(@RequestBody String refresh) {

        final String issuer = joseHelper.loadIssuer(refresh, credentialManager.getLocalPrivateKey());
        final Application client = context.getApplication();


        final List<ApplicationAspectAttribute> attributes = attributeDAO.findByAppAndAspect(client.getId(), CL_JOSE.getValue());
        final ApplicationAspectAttribute clientEntityId = attributes.stream().filter(attr -> attr.getAttributeCode().getType() == ENTITYID).findFirst().get();
        Assert.notNull(clientEntityId, "The Client Entity ID not found");
        Assert.isTrue(clientEntityId.getValue().equals(issuer), "Untrusted peer");

        final PublicKey clientKey = credentialManager.getPeerKey(issuer, CL_JOSE);
        final JOSERefreshToken refreshToken = tokenFactory.createJOSERefreshToken(refresh, credentialManager.getLocalPrivateKey(), clientKey);
        refreshToken.decipher();

        final ApplicationAspectAttribute rsEntityId = attributeDAO.findByAttribute(RS_JOSE_ENTITY_ID.getValue(), refreshToken.getAudience());
        Assert.notNull(rsEntityId, "The Resource Server Entity ID not found");

        final List<Token> tokens = tokenDAO.findByApplicationAndNormAndType(client, JOSE, REFRESH);
        final Token readyRefresh = tokens.stream().filter(t -> t.getPurpose() == DELEGATION)
                .filter(t -> t.getStatus() == READY)
                .filter(t -> t.getExpiryTime().after(new Date()))
                .filter(t -> passwordEncoder.matches(refreshToken.getStamp(), t.getStamp()))
                .findFirst().orElse(null);
        Assert.notNull(readyRefresh, "No Ready Refresh token found");

        final PublicKey resourceServerKey = credentialManager.getPeerKey(refreshToken.getAudience(), RS_JOSE);
        final JOSEAccessToken accessToken = tokenFactory.createJOSEAccessToken(credentialManager.getLocalPrivateKey(), resourceServerKey);
        accessToken.setStamp(UUID.randomUUID().toString());
        accessToken.setIssuer(localEntityId);
        accessToken.setAudience(refreshToken.getAudience());
        accessToken.setVerdict(TokenVerdict.SUCCESS);
        accessToken.cypher();
        final String serialized = accessToken.serialize();

        final Token token = tokenMapper.toToken(accessToken);
        token.setPurpose(DELEGATION);
        token.setStatus(READY);
        token.setChecksum(DigestUtils.sha256Hex(serialized));
        token.setSession(context.getAuthenticationSession());
        token.setApplication(applicationDAO.findOne(rsEntityId.getId().getApplicationId()));
        tokenDAO.save(token);


        return serialized;
    }
}
