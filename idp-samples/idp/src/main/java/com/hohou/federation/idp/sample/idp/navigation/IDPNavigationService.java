package com.hohou.federation.idp.sample.idp.navigation;

import com.hohou.federation.idp.sample.idp.SampleIDPToken;
import com.hohou.federation.idp.sample.idp.jose.IDPJoseService;
import com.hohou.federation.idp.sample.idp.saml.IDPSamlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashSet;

import static java.util.Arrays.asList;

@Component
public class IDPNavigationService {

    @Autowired
    private IDPJoseService joseService;

    @Autowired
    private IDPSamlService samlService;

    @Value("${sp.jose.endpoint}")
    private String joseTargetURL;

    @Value("${sp.saml.endpoint}")
    private String samlTargetURL;


    public Navigation buildNavigation(SampleIDPToken token, HttpServletRequest request, HttpServletResponse response) {

        Navigation navigation = new Navigation();

        if ("JOSE".equalsIgnoreCase(token.getNorm())) {
            navigation.setTarget(joseTargetURL);
            navigation.setAttributes(new HashSet<>(asList(new NavigationAttribute("Bearer", joseService.generateJOSEAccess(token)))));
        } else {
            navigation.setTarget(samlTargetURL);
            navigation.setAttributes(new HashSet<>(asList(new NavigationAttribute("SAMLResponse", samlService.generateSAMLAccess(token)))));
        }

        return navigation;
    }


}
