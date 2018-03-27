package com.mlyauth.idp.saml;

import com.mlyauth.token.saml.SAMLHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

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
