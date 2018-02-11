package com.mlyauth.itests.sso.idp.saml;

import com.mlyauth.constants.AuthAspectType;
import com.mlyauth.dao.ApplicationAspectAttributeDAO;
import com.mlyauth.dao.ApplicationDAO;
import com.mlyauth.domain.Application;
import com.mlyauth.domain.ApplicationAspectAttribute;
import com.mlyauth.itests.AbstractIntegrationTest;
import com.mlyauth.security.sso.idp.saml.response.IDPSAMLResponseGenerator;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.opensaml.saml2.core.Response;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class IDPSAMLResponseGeneratorIT extends AbstractIntegrationTest {


    @Autowired
    private ApplicationDAO applicationDAO;

    @Autowired
    private ApplicationAspectAttributeDAO appAspectAttrDAO;

    @Autowired
    private IDPSAMLResponseGenerator generator;


    @Test
    public void when_generate_a_saml_response_to_app_then_return_valid_response() throws Exception {
        final Application policy = applicationDAO.findByAppname("PolicyDev");

        final List<ApplicationAspectAttribute> byApplicationIdAndAspectCode = appAspectAttrDAO.findByAppAndAspect(policy.getId(), AuthAspectType.SP_SAML.name());

        Response response = generator.generate(policy);
        Assert.assertThat(response, Matchers.notNullValue());
    }

}
