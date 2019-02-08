package com.hohou.federation.idp.idp.saml;

import com.hohou.federation.idp.exception.Error;
import com.hohou.federation.idp.exception.IDPException;
import com.hohou.federation.idp.token.saml.SAMLHelper;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml2.metadata.KeyDescriptor;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.credential.UsageType;
import org.opensaml.xml.security.keyinfo.KeyInfoGenerator;
import org.opensaml.xml.security.x509.X509KeyInfoGeneratorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.saml.key.KeyManager;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class IDPSAMLMetadataGenerator {

    @Autowired
    private SAMLHelper samlHelper;

    @Autowired
    private KeyManager keyManeger;

    @Value("${idp.saml.entityId}")
    private String idpEntityId;

    public EntityDescriptor generateMetadata() {

        try {
            final KeyInfoGenerator keyInfoGenerator = keyInfoGenerator();
            final IDPSSODescriptor idpDescriptor = idpDescriptor(keyInfoGenerator);
            final EntityDescriptor metadata = samlHelper.buildSAMLObject(EntityDescriptor.class);
            metadata.getRoleDescriptors().add(idpDescriptor);
            metadata.setEntityID(idpEntityId);
            metadata.setID(idpEntityId);
            return metadata;
        } catch (Exception e) {
            throw IDPException.newInstance()
                    .setErrors(Arrays.asList(new Error("IDP_SAML_METADATA_ERROR", e.getMessage())));
        }

    }

    private KeyInfoGenerator keyInfoGenerator() {
        X509KeyInfoGeneratorFactory keyInfoGeneratorFactory = new X509KeyInfoGeneratorFactory();
        keyInfoGeneratorFactory.setEmitEntityCertificate(true);
        return keyInfoGeneratorFactory.newInstance();
    }

    private IDPSSODescriptor idpDescriptor(final KeyInfoGenerator keyInfoGenerator) throws SecurityException {
        final IDPSSODescriptor idpDescriptor = samlHelper.buildSAMLObject(IDPSSODescriptor.class);
        KeyDescriptor signKeyDescriptor = samlHelper.buildSAMLObject(KeyDescriptor.class);
        signKeyDescriptor.setUse(UsageType.SIGNING);
        signKeyDescriptor.setKeyInfo(keyInfoGenerator.generate(keyManeger.getDefaultCredential()));
        idpDescriptor.getKeyDescriptors().add(signKeyDescriptor);
        idpDescriptor.addSupportedProtocol(SAMLConstants.SAML20P_NS);
        return idpDescriptor;
    }
}
