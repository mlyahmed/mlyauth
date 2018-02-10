package com.mlyauth.utests.security.sso.idp.saml;

import com.mlyauth.domain.Application;
import com.mlyauth.security.sso.idp.saml.response.IDPSAMLResponseGenerator;
import org.junit.Test;

public class IDPSAMLResponseGeneratorTest {

    @Test(expected = IllegalArgumentException.class)
    public void when_generate_response_from_null_then_error() {
        IDPSAMLResponseGenerator generator = new IDPSAMLResponseGenerator();
        Application app = null;
        generator.generate(app);
    }

    @Test
    public void given_a_saml_sp_app_when_generate_a_response_then_generate_it() {
        IDPSAMLResponseGenerator generator = new IDPSAMLResponseGenerator();
        Application app = Application.newInstance();


    }
}
