package com.mlyauth.token.saml;

import com.mlyauth.constants.TokenVerdict;
import com.mlyauth.context.IContext;
import com.mlyauth.dao.ApplicationAspectAttributeDAO;
import com.mlyauth.domain.Application;
import com.mlyauth.domain.ApplicationAspectAttribute;
import com.mlyauth.sp.saml.ISPSAMLAspectValidator;
import com.mlyauth.token.TokenIdGenerator;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.saml.key.KeyManager;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.security.cert.X509Certificate;
import java.util.List;

import static com.mlyauth.constants.AspectAttribute.SP_SAML_ENCRYPTION_CERTIFICATE;
import static com.mlyauth.constants.AspectAttribute.SP_SAML_ENTITY_ID;
import static com.mlyauth.constants.AspectAttribute.SP_SAML_SSO_URL;
import static com.mlyauth.constants.AspectType.SP_SAML;


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

    private SAMLAccessToken buildToken(final List<ApplicationAspectAttribute> attributes) {
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

    private List<ApplicationAspectAttribute> loadAttributes(final Application app) {
        return appAspectAttrDAO.findByAppAndAspect(app.getId(), SP_SAML.name());
    }

    private BasicX509Credential buildCredential(final List<ApplicationAspectAttribute> attributes) {
        BasicX509Credential credential = new BasicX509Credential();
        credential.setEntityCertificate(loadApplicationEncryptionCertificate(attributes));
        credential.setPrivateKey(keyManager.getDefaultCredential().getPrivateKey());
        return credential;
    }


    private X509Certificate loadApplicationEncryptionCertificate(final List<ApplicationAspectAttribute> attributes) {
        return samlHelper.toX509Certificate(getEncryptionCertificate(attributes).getValue());
    }

    private ApplicationAspectAttribute getEntityId(final List<ApplicationAspectAttribute> attributes) {
        return attributes.stream()
                .filter(att -> SP_SAML_ENTITY_ID.equals(att.getId().getAttributeCode()))
                .findFirst().orElseGet(null);
    }

    private ApplicationAspectAttribute getEncryptionCertificate(final List<ApplicationAspectAttribute> attributes) {
        return attributes.stream()
                .filter(att -> SP_SAML_ENCRYPTION_CERTIFICATE.equals(att.getId().getAttributeCode()))
                .findFirst().orElseGet(null);
    }

    private ApplicationAspectAttribute getTargetURL(final List<ApplicationAspectAttribute> attributes) {
        return attributes.stream()
                .filter(att -> SP_SAML_SSO_URL.equals(att.getId().getAttributeCode()))
                .findFirst().orElseGet(null);
    }
}
