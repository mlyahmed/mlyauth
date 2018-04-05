package com.mlyauth.token.jose;

import com.mlyauth.beans.TokenBean;
import com.mlyauth.constants.TokenVerdict;
import com.mlyauth.context.IContext;
import com.mlyauth.credentials.CredentialManager;
import com.mlyauth.dao.ApplicationAspectAttributeDAO;
import com.mlyauth.dao.ApplicationDAO;
import com.mlyauth.dao.TokenDAO;
import com.mlyauth.domain.Application;
import com.mlyauth.domain.ApplicationAspectAttribute;
import com.mlyauth.domain.Token;
import com.mlyauth.token.TokenIdGenerator;
import com.mlyauth.token.TokenMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.security.PublicKey;
import java.util.Date;
import java.util.List;

import static com.mlyauth.constants.AspectAttribute.RS_JOSE_ENTITY_ID;
import static com.mlyauth.constants.AspectType.CL_JOSE;
import static com.mlyauth.constants.AspectType.RS_JOSE;
import static com.mlyauth.constants.AttributeType.ENTITYID;
import static com.mlyauth.constants.TokenPurpose.DELEGATION;
import static com.mlyauth.constants.TokenStatus.READY;
import static java.time.format.DateTimeFormatter.ofPattern;
import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

@Service
@Transactional
public class JOSETokenService {

    @Value("${idp.jose.entityId}")
    private String localEntityId;

    @Autowired
    private TokenIdGenerator idGenerator;

    @Autowired
    private IContext context;

    @Autowired
    private CredentialManager credManager;

    @Autowired
    private ApplicationAspectAttributeDAO attributeDAO;

    @Autowired
    private JOSETokenFactory tokenFactory;

    @Autowired
    private TokenDAO tokenDAO;

    @Autowired
    private TokenMapper tokenMapper;

    @Autowired
    private ApplicationDAO applicationDAO;

    public TokenBean refreshAccess(JOSERefreshToken refresh){
        final Application client = context.getApplication();


        final List<ApplicationAspectAttribute> attributes = attributeDAO.findByAppAndAspect(client.getId(), CL_JOSE.getValue());
        final ApplicationAspectAttribute clientEntityId = attributes.stream().filter(attr -> attr.getAttributeCode().getType() == ENTITYID).findFirst().get();
        notNull(clientEntityId, "The Client Entity ID not found");

        isTrue(clientEntityId.getValue().equals(refresh.getIssuer()), "Untrusted peer");

        ApplicationAspectAttribute rsEntityId = attributeDAO.findByAttribute(RS_JOSE_ENTITY_ID.getValue(), refresh.getAudience());
        notNull(rsEntityId, "The Resource Server Entity ID not found");

        final Token readyRefresh = tokenDAO.findByStamp(DigestUtils.sha256Hex(refresh.getStamp()));
        notNull(readyRefresh, "No Ready Refresh token found");

        final PublicKey resourceServerKey = (localEntityId.equals(rsEntityId.getValue())) ? credManager.getPublicKey() : credManager.getPeerKey(refresh.getAudience(), RS_JOSE);
        final JOSEAccessToken accessToken = tokenFactory.createAccessToken(credManager.getPrivateKey(), resourceServerKey);
        accessToken.setStamp(idGenerator.generateId());
        accessToken.setSubject(refresh.getSubject());
        accessToken.setIssuer(localEntityId);
        accessToken.setAudience(refresh.getAudience());
        accessToken.setVerdict(TokenVerdict.SUCCESS);
        accessToken.cypher();

        final Token token = tokenMapper.toToken(accessToken);
        token.setPurpose(DELEGATION);
        token.setStatus(READY);
        token.setChecksum(DigestUtils.sha256Hex(accessToken.serialize()));
        token.setSession(context.getAuthenticationSession());
        token.setApplication(applicationDAO.findOne(rsEntityId.getId().getApplicationId()));
        tokenDAO.saveAndFlush(token);

        return new TokenBean(accessToken.serialize(), accessToken.getExpiryTime().format(ofPattern("YYYYMMddHHmmss")));
    }


    public void checkAccess(String access){
        final Token token = tokenDAO.findByChecksum(DigestUtils.sha256Hex(access));
        Assert.notNull(token, "Token not found");
        Assert.isTrue(token.getExpiryTime().after(new Date()), "The token is expired");
        Assert.isTrue(token.getStatus() == READY, "The token was already used");
    }
}
