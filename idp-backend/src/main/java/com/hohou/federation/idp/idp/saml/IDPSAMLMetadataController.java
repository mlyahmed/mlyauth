package com.hohou.federation.idp.idp.saml;

import com.hohou.federation.idp.token.saml.SAMLHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/idp/saml/metadata")
public class IDPSAMLMetadataController {


    @Autowired
    private SAMLHelper samlHelper;

    @Autowired
    private IDPSAMLMetadataGenerator generator;

    @GetMapping(produces = {MediaType.APPLICATION_XML_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    String getMetadata() throws Exception {
        return samlHelper.toString(generator.generateMetadata());
    }

}
