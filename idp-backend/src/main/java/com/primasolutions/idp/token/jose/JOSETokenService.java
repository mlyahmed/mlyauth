package com.primasolutions.idp.token.jose;

import com.primasolutions.idp.application.AppAspAttr;
import com.primasolutions.idp.application.Application;
import com.primasolutions.idp.application.ApplicationAspectAttributeDAO;
import com.primasolutions.idp.application.ApplicationDAO;
import com.primasolutions.idp.constants.AspectAttribute;
import com.primasolutions.idp.constants.AspectType;
import com.primasolutions.idp.constants.AttributeType;
import com.primasolutions.idp.constants.TokenPurpose;
import com.primasolutions.idp.constants.TokenStatus;
import com.primasolutions.idp.constants.TokenVerdict;
import com.primasolutions.idp.context.IContext;
import com.primasolutions.idp.credentials.CredentialManager;
import com.primasolutions.idp.token.Token;
import com.primasolutions.idp.token.TokenBean;
import com.primasolutions.idp.token.TokenDAO;
import com.primasolutions.idp.token.TokenIdGenerator;
import com.primasolutions.idp.token.TokenMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.security.PublicKey;
import java.util.Date;
import java.util.List;

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

    public TokenBean refreshAccess(final JOSERefreshToken refresh) {
        final Application client = context.getApplication();
        final List<AppAspAttr> at = attributeDAO.findByAppAndAspect(client.getId(), AspectType.CL_JOSE.getValue());
        notNull(getClientEntityId(at), "The Client Entity ID not found");
        isTrue(getClientEntityId(at).getValue().equals(refresh.getIssuer()), "Untrusted peer");
        AppAspAttr rsEntityId = getResourceHolderEntityId(refresh);
        notNull(rsEntityId, "The Resource Server Entity ID not found");
        final Token readyRefresh = tokenDAO.findByStamp(DigestUtils.sha256Hex(refresh.getStamp()));
        notNull(readyRefresh, "No Ready Refresh token found");

        final PublicKey rsKey = getResourceServerKey(refresh, rsEntityId);
        final JOSEAccessToken accessToken = tokenFactory.newAccessToken(credManager.getPrivateKey(), rsKey);
        accessToken.setStamp(idGenerator.generateId());
        accessToken.setSubject(refresh.getSubject());
        accessToken.setIssuer(localEntityId);
        accessToken.setDelegator(refresh.getDelegator());
        accessToken.setAudience(refresh.getAudience());
        accessToken.setVerdict(TokenVerdict.SUCCESS);
        accessToken.cypher();

        final Token token = tokenMapper.toToken(accessToken);
        token.setPurpose(TokenPurpose.DELEGATION);
        token.setStatus(TokenStatus.READY);
        token.setChecksum(DigestUtils.sha256Hex(accessToken.serialize()));
        token.setSession(context.getAuthenticationSession());
        token.setApplication(applicationDAO.findOne(rsEntityId.getId().getApplicationId()));
        tokenDAO.saveAndFlush(token);

        return new TokenBean(accessToken.serialize(), accessToken.getExpiryTime().format(ofPattern("YYYYMMddHHmmss")));
    }

    private PublicKey getResourceServerKey(final JOSERefreshToken refresh, final AppAspAttr rsId) {
        return (localEntityId.equals(rsId.getValue()))
                ? credManager.getPublicKey()
                : credManager.getPeerKey(refresh.getAudience(), AspectType.RS_JOSE);
    }

    private AppAspAttr getResourceHolderEntityId(final JOSERefreshToken refresh) {
        return attributeDAO.findByAttribute(AspectAttribute.RS_JOSE_ENTITY_ID.getValue(), refresh.getAudience());
    }

    private AppAspAttr getClientEntityId(final List<AppAspAttr> attributes) {
        return attributes.stream()
                .filter(attr -> attr.getAttributeCode().getType() == AttributeType.ENTITYID)
                .findFirst()
                .get();
    }


    public void checkAccess(final String access) {
        final Token token = tokenDAO.findByChecksum(DigestUtils.sha256Hex(access));
        Assert.notNull(token, "Token not found");
        Assert.isTrue(token.getExpiryTime().after(new Date()), "The token is expired");
        Assert.isTrue(token.getStatus() == TokenStatus.READY, "The token was already used");
    }
}
