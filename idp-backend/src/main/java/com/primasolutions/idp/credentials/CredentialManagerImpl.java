package com.primasolutions.idp.credentials;

import com.nimbusds.jose.util.Base64URL;
import com.primasolutions.idp.application.AppAspAttr;
import com.primasolutions.idp.application.ApplicationAspectAttributeDAO;
import com.primasolutions.idp.application.ApplicationAspectAttributeId;
import com.primasolutions.idp.constants.AspectType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.saml.key.KeyManager;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import static com.primasolutions.idp.constants.AspectAttribute.get;
import static com.primasolutions.idp.constants.AttributeType.CERTIFICATE;
import static com.primasolutions.idp.constants.AttributeType.ENTITYID;

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
    public Certificate getPeerCertificate(final String entityId, final AspectType aspectType) {
        return loadCertificate(entityId, aspectType);
    }

    @Override
    public PublicKey getPeerKey(final String entityId, final AspectType aspectType) {
        return loadCertificate(entityId, aspectType).getPublicKey();
    }


    private Certificate loadCertificate(final String entityId, final AspectType aspect) {
        try {
            final AppAspAttr certificate = getApplicationCertificate(entityId, aspect);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(new Base64URL(certificate.getValue()).decode());
            return CertificateFactory.getInstance("X.509").generateCertificate(inputStream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private AppAspAttr getApplicationCertificate(final String entityId, final AspectType aspect) {
        final ApplicationAspectAttributeId entity = getEntityAttribute(entityId, aspect);
        return attributeDAO.findAndIndex(entity.getApplicationId(), aspect.name()).get(get(aspect, CERTIFICATE));
    }

    private ApplicationAspectAttributeId getEntityAttribute(final String entityId, final AspectType aspect) {
        return attributeDAO.findByAttribute(get(aspect, ENTITYID).getValue(), entityId).getId();
    }

}
