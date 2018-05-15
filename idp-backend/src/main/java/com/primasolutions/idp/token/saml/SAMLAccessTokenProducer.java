package com.primasolutions.idp.token.saml;

import com.primasolutions.idp.application.AppAspAttr;
import com.primasolutions.idp.application.Application;
import com.primasolutions.idp.application.ApplicationAspectAttributeDAO;
import com.primasolutions.idp.constants.AspectAttribute;
import com.primasolutions.idp.constants.AspectType;
import com.primasolutions.idp.constants.TokenVerdict;
import com.primasolutions.idp.context.IContext;
import com.primasolutions.idp.sp.saml.ISPSAMLAspectValidator;
import com.primasolutions.idp.token.TokenIdGenerator;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.saml.key.KeyManager;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.security.cert.X509Certificate;
import java.util.List;


@Component
public class SAMLAccessTokenProducer {

    @Autowired
    private TokenIdGenerator idGenerator;

    @Autowired
    private SAMLTokenFactory tokenFactory;

    @Autowired
    private ISPSAMLAspectValidator validator;

    @Autowired
    private IContext context;

    @Autowired
    private KeyManager keyManager;

    @Autowired
    private SAMLHelper samlHelper;

    @Autowired
    private ApplicationAspectAttributeDAO appAspectAttrDAO;

    @Value("${idp.saml.entityId}")
    private String idpEntityId;

    public SAMLAccessToken produce(final Application app) {
        Assert.notNull(app, "The application parameter is null");
        validator.validate(app);
        return buildToken(loadAttributes(app));
    }

    private SAMLAccessToken buildToken(final List<AppAspAttr> attributes) {
        SAMLAccessToken token = tokenFactory.createAccessToken(buildCredential(attributes));
        token.setStamp(idGenerator.generateId());
        token.setIssuer(idpEntityId);
        token.setTargetURL(getTargetURL(attributes).getValue());
        token.setVerdict(TokenVerdict.SUCCESS);
        token.setSubject("");
        token.setAudience(getEntityId(attributes).getValue());
        context.getAttributes().forEach((k, v) -> token.setClaim(k, v));
        token.cypher();
        return token;
    }

    private List<AppAspAttr> loadAttributes(final Application app) {
        return appAspectAttrDAO.findByAppAndAspect(app.getId(), AspectType.SP_SAML.name());
    }

    private BasicX509Credential buildCredential(final List<AppAspAttr> attributes) {
        BasicX509Credential credential = new BasicX509Credential();
        credential.setEntityCertificate(loadApplicationEncryptionCertificate(attributes));
        credential.setPrivateKey(keyManager.getDefaultCredential().getPrivateKey());
        return credential;
    }


    private X509Certificate loadApplicationEncryptionCertificate(final List<AppAspAttr> attributes) {
        return samlHelper.toX509Certificate(getEncryptionCertificate(attributes).getValue());
    }

    private AppAspAttr getEntityId(final List<AppAspAttr> attributes) {
        return attributes.stream()
                .filter(att -> AspectAttribute.SP_SAML_ENTITY_ID.equals(att.getId().getAttributeCode()))
                .findFirst().orElseGet(null);
    }

    private AppAspAttr getEncryptionCertificate(final List<AppAspAttr> attributes) {
        return attributes.stream()
                .filter(att -> AspectAttribute.SP_SAML_ENCRYPTION_CERTIFICATE.equals(att.getId().getAttributeCode()))
                .findFirst().orElseGet(null);
    }

    private AppAspAttr getTargetURL(final List<AppAspAttr> attributes) {
        return attributes.stream()
                .filter(att -> AspectAttribute.SP_SAML_SSO_URL.equals(att.getId().getAttributeCode()))
                .findFirst().orElseGet(null);
    }
}
