package com.mlyauth.api;

import com.mlyauth.security.saml.SAMLHelper;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml2.metadata.KeyDescriptor;
import org.opensaml.xml.security.credential.UsageType;
import org.opensaml.xml.security.keyinfo.KeyInfoGenerator;
import org.opensaml.xml.security.x509.X509KeyInfoGeneratorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.saml.key.KeyManager;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/idp/saml/metadata")
public class IDPSAMLMetadataController {


    @Autowired
    private SAMLHelper samlHelper;

    @Autowired
    private KeyManager keyManeger;

    @GetMapping(produces = {MediaType.APPLICATION_XML_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    String getMetadata() throws Exception {

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
        metadata.setEntityID("app4primainsure");
        metadata.setID("app4primainsure");

        return samlHelper.toString(metadata);
    }

}
