package com.mlyauth.idp.saml;

import com.mlyauth.exception.AuthError;
import com.mlyauth.exception.IDPException;
import com.mlyauth.token.saml.SAMLHelper;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml2.metadata.KeyDescriptor;
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

            EntityDescriptor metadata = samlHelper.buildSAMLObject(EntityDescriptor.class);
            IDPSSODescriptor idpDescriptor = samlHelper.buildSAMLObject(IDPSSODescriptor.class);

            X509KeyInfoGeneratorFactory keyInfoGeneratorFactory = new X509KeyInfoGeneratorFactory();
            keyInfoGeneratorFactory.setEmitEntityCertificate(true);
            KeyInfoGenerator keyInfoGenerator = keyInfoGeneratorFactory.newInstance();

            KeyDescriptor signKeyDescriptor = samlHelper.buildSAMLObject(KeyDescriptor.class);
            signKeyDescriptor.setUse(UsageType.SIGNING);
            signKeyDescriptor.setKeyInfo(keyInfoGenerator.generate(keyManeger.getDefaultCredential()));
            idpDescriptor.getKeyDescriptors().add(signKeyDescriptor);
            idpDescriptor.addSupportedProtocol(SAMLConstants.SAML20P_NS);

            metadata.getRoleDescriptors().add(idpDescriptor);
            metadata.setEntityID(idpEntityId);
            metadata.setID(idpEntityId);

            return metadata;
        } catch (Exception e) {
            throw IDPException.newInstance()
                    .setErrors(Arrays.asList(new AuthError("IDP_SAML_METADATA_ERROR", e.getMessage())));
        }

    }
}
