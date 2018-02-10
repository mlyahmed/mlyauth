package com.mlyauth.api;

import com.mlyauth.security.saml.SAMLHelper;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/idp/saml/metadata")
public class IDPSAMLMetadataController {


    @Autowired
    private SAMLHelper samlHelper;

    @GetMapping(produces = {MediaType.APPLICATION_XML_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    String getMetadata() throws Exception {

        EntityDescriptor metadata = samlHelper.buildSAMLObject(EntityDescriptor.class);
        metadata.setEntityID("app4primainsure");
        metadata.setID("app4primainsure");

        return samlHelper.toString(metadata);

    }

}
