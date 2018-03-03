package com.mlyauth.producers;

import com.mlyauth.constants.TokenVerdict;
import com.mlyauth.dao.ApplicationAspectAttributeDAO;
import com.mlyauth.domain.Application;
import com.mlyauth.domain.ApplicationAspectAttribute;
import com.mlyauth.security.context.IContext;
import com.mlyauth.token.IDPToken;
import com.mlyauth.token.saml.SAMLAccessToken;
import com.mlyauth.tools.SAMLHelper;
import com.mlyauth.validators.ISPSAMLAspectValidator;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.saml.key.KeyManager;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.security.cert.X509Certificate;
import java.util.List;

import static com.mlyauth.constants.AuthAspectType.SP_SAML;
import static com.mlyauth.constants.SPSAMLAttribute.*;

@Component
public class SAMLAccessTokenProducer implements ITokenProducer {

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

    @Override
    public IDPToken produce(Application app) {
        Assert.notNull(app, "The application parameter is null");
        validator.validate(app);
        final List<ApplicationAspectAttribute> attributes = loadAttributes(app);

        SAMLAccessToken token = new SAMLAccessToken(buildCredential(attributes));
        token.setId(samlHelper.generateRandomId());
        token.setIssuer(idpEntityId);
        token.setTargetURL(getTargetURL(attributes).getValue());
        token.setVerdict(TokenVerdict.SUCCESS);
        token.setSubject("");
        token.setAudience(getEntityId(attributes).getValue());
        context.getAttributes().forEach((k, v) -> token.setClaim(k, v));
        token.cypher();
        return token;
    }

    private List<ApplicationAspectAttribute> loadAttributes(Application app) {
        return appAspectAttrDAO.findByAppAndAspect(app.getId(), SP_SAML.name());
    }

    private BasicX509Credential buildCredential(List<ApplicationAspectAttribute> attributes) {
        BasicX509Credential credential = new BasicX509Credential();
        credential.setEntityCertificate(loadApplicationEncryptionCertificate(attributes));
        credential.setPrivateKey(keyManager.getDefaultCredential().getPrivateKey());
        return credential;
    }


    private X509Certificate loadApplicationEncryptionCertificate(List<ApplicationAspectAttribute> attributes) {
        return samlHelper.toX509Certificate(getEncryptionCertificate(attributes).getValue());
    }

    private ApplicationAspectAttribute getEntityId(List<ApplicationAspectAttribute> attributes) {
        return attributes.stream()
                .filter(att -> SP_SAML_ENTITY_ID.equals(att.getId().getAttributeCode()))
                .findFirst().orElseGet(null);
    }

    private ApplicationAspectAttribute getEncryptionCertificate(List<ApplicationAspectAttribute> attributes) {
        return attributes.stream()
                .filter(att -> SP_SAML_ENCRYPTION_CERTIFICATE.equals(att.getId().getAttributeCode()))
                .findFirst().orElseGet(null);
    }

    private ApplicationAspectAttribute getTargetURL(List<ApplicationAspectAttribute> attributes) {
        return attributes.stream()
                .filter(att -> SP_SAML_SSO_URL.equals(att.getId().getAttributeCode()))
                .findFirst().orElseGet(null);
    }
}
