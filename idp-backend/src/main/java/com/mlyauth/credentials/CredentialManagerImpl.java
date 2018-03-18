package com.mlyauth.credentials;

import com.mlyauth.constants.AspectAttribute;
import com.mlyauth.constants.AspectType;
import com.mlyauth.dao.ApplicationAspectAttributeDAO;
import com.mlyauth.domain.ApplicationAspectAttribute;
import com.nimbusds.jose.util.Base64URL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.saml.key.KeyManager;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Map;

import static com.mlyauth.constants.AspectAttribute.get;
import static com.mlyauth.constants.AttributeType.CERTIFICATE;
import static com.mlyauth.constants.AttributeType.ENTITYID;

@Component
public class CredentialManagerImpl implements CredentialManager {

    @Autowired
    private KeyManager keyManager;

    @Autowired
    private ApplicationAspectAttributeDAO attributeDAO;

    @Override
    public PrivateKey getPrivateKey() {
        return keyManager.getDefaultCredential().getPrivateKey();
    }

    @Override
    public PublicKey getPublicKey() {
        return keyManager.getDefaultCredential().getPublicKey();
    }

    @Override
    public Certificate getCertificate() {
        return keyManager.getCertificate(keyManager.getDefaultCredentialName());
    }

    @Override
    public Certificate getPeerCertificate(String entityId, AspectType aspectType) {
        return loadCertificate(entityId, aspectType);
    }

    @Override
    public PublicKey getPeerKey(String entityId, AspectType aspectType) {
        return loadCertificate(entityId, aspectType).getPublicKey();
    }


    private Certificate loadCertificate(String entityId, AspectType aspect) {
        try {
            final ApplicationAspectAttribute issAttributes = attributeDAO.findByAttribute(get(aspect, ENTITYID).getValue(), entityId);
            final Map<AspectAttribute, ApplicationAspectAttribute> attributes = attributeDAO.findAndIndex(issAttributes.getId().getApplicationId(), aspect.name());
            final ApplicationAspectAttribute certificate = attributes.get(get(aspect, CERTIFICATE));
            ByteArrayInputStream inputStream = new ByteArrayInputStream(new Base64URL(certificate.getValue()).decode());
            return CertificateFactory.getInstance("X.509").generateCertificate(inputStream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
