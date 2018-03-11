package com.mlyauth.navigation;

import com.mlyauth.AbstractIntegrationTest;
import com.mlyauth.beans.NavigationBean;
import com.mlyauth.constants.AspectAttribute;
import com.mlyauth.dao.ApplicationAspectAttributeDAO;
import com.mlyauth.dao.ApplicationDAO;
import com.mlyauth.domain.Application;
import com.mlyauth.domain.ApplicationAspectAttribute;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static com.mlyauth.constants.AspectAttribute.SP_SAML_SSO_URL;
import static com.mlyauth.constants.AspectType.SP_SAML;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class IDPSAMLNavigationServiceIT extends AbstractIntegrationTest {

    public static final String POLICY_DEV = "PolicyDev";

    @Autowired
    private IDPSAMLNavigationService navigationService;

    @Autowired
    private ApplicationDAO applicationDAO;

    @Autowired
    private ApplicationAspectAttributeDAO attributesDAO;

    @Test
    public void when_generate_a_saml_navigation_to_Policy_Dev_then_generate_it(){
        final Application policyDev = applicationDAO.findByAppname(POLICY_DEV);
        final Map<AspectAttribute, ApplicationAspectAttribute> attributes = attributesDAO.findAndIndex(policyDev.getId(), SP_SAML.name());
        final NavigationBean navigation = navigationService.newNavigation(POLICY_DEV);
        assertThat(navigation, notNullValue());
        assertThat(navigation.getTarget(), equalTo(attributes.get(SP_SAML_SSO_URL).getValue()));
        assertThat(navigation.getAttribute("SAMLResponse"), notNullValue());
        assertThat(navigation.getAttribute("SAMLResponse").getValue(), notNullValue());
    }
}
