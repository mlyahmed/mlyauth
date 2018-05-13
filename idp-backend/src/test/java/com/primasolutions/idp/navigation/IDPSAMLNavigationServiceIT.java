package com.primasolutions.idp.navigation;

import com.primasolutions.idp.AbstractIntegrationTest;
import com.primasolutions.idp.beans.NavigationBean;
import com.primasolutions.idp.constants.AspectAttribute;
import com.primasolutions.idp.context.IContext;
import com.primasolutions.idp.dao.ApplicationAspectAttributeDAO;
import com.primasolutions.idp.dao.ApplicationDAO;
import com.primasolutions.idp.dao.NavigationDAO;
import com.primasolutions.idp.dao.TokenDAO;
import com.primasolutions.idp.domain.AppAspAttr;
import com.primasolutions.idp.domain.Application;
import com.primasolutions.idp.domain.Navigation;
import com.primasolutions.idp.domain.NavigationAttribute;
import com.primasolutions.idp.domain.Token;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static com.primasolutions.idp.constants.AspectAttribute.SP_SAML_SSO_URL;
import static com.primasolutions.idp.constants.AspectType.SP_SAML;
import static com.primasolutions.idp.constants.Direction.OUTBOUND;
import static com.primasolutions.idp.constants.TokenNorm.SAML;
import static com.primasolutions.idp.constants.TokenPurpose.NAVIGATION;
import static com.primasolutions.idp.constants.TokenStatus.CHECKED;
import static com.primasolutions.idp.constants.TokenType.ACCESS;
import static com.primasolutions.idp.token.Claims.TARGET_URL;
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

    @Autowired
    private TokenDAO tokenDAO;

    @Autowired
    private NavigationDAO navigationDAO;

    @Autowired
    private IContext context;

    @Test
    public void when_generate_a_saml_navigation_to_Policy_Dev_then_generate_it() {
        final Application policy = applicationDAO.findByAppname(POLICY_DEV);
        final Map<AspectAttribute, AppAspAttr> attributes = attributesDAO.findAndIndex(policy.getId(), SP_SAML.name());
        final NavigationBean navigation = navigationService.newNavigation(POLICY_DEV);
        assertThat(navigation, notNullValue());
        assertThat(navigation.getTarget(), equalTo(attributes.get(SP_SAML_SSO_URL).getValue()));
        assertThat(navigation.getAttribute("SAMLResponse"), notNullValue());
        assertThat(navigation.getAttribute("SAMLResponse").getValue(), notNullValue());
    }

    @Test
    public void when_generate_a_saml_navigation_then_trace_the_navigation() {
        final NavigationBean navigationBean = navigationService.newNavigation(POLICY_DEV);
        assertThat(navigationBean.getId(), notNullValue());
        final Navigation navigation = navigationDAO.findOne(navigationBean.getId());
        final NavigationAttribute samlResponse = navigation.getAttribute("SAMLResponse");
        assertThat(navigation, notNullValue());
        assertThat(navigation.getCreatedAt(), notNullValue());
        assertThat(navigation.getDirection(), equalTo(OUTBOUND));
        assertThat(navigation.getTargetURL(), equalTo(navigationBean.getTarget()));
        assertThat(navigation.getToken(), notNullValue());
        assertThat(navigation.getToken().getStatus(), equalTo(CHECKED));
        assertThat(navigation.getToken().getChecksum(), equalTo(DigestUtils.sha256Hex(samlResponse.getValue())));
        assertThat(navigation.getSession(), equalTo(context.getAuthenticationSession()));
    }

    @Test
    public void when_generate_a_saml_navigation_then_trace_the_navigation_attribute() {
        final NavigationBean navigationBean = navigationService.newNavigation(POLICY_DEV);
        final Navigation navigation = navigationDAO.findOne(navigationBean.getId());
        final NavigationAttribute samlResponse = navigation.getAttribute("SAMLResponse");
        assertThat(samlResponse, notNullValue());
        assertThat(navigation.getAttribute("SAMLResponse").getValue(), equalTo(samlResponse.getValue()));
    }

    @Test
    public void when_generate_a_saml_navigation_then_trace_the_token() {
        final NavigationBean navigation = navigationService.newNavigation(POLICY_DEV);
        assertThat(navigation.getTokenId(), notNullValue());
        final Token token = tokenDAO.findOne(navigation.getTokenId());
        assertThat(token, notNullValue());
        assertThat(token.getNorm(), equalTo(SAML));
        assertThat(token.getType(), equalTo(ACCESS));
        assertThat(token.getPurpose(), equalTo(NAVIGATION));
        assertThat(token.getIssuanceTime(), notNullValue());
        assertThat(token.getEffectiveTime(), notNullValue());
        assertThat(token.getExpiryTime(), notNullValue());
        assertThat(token.getSession(), equalTo(context.getAuthenticationSession()));
        assertThat(token.getClaimsMap(), notNullValue());
        assertThat(token.getClaimsMap().get(TARGET_URL.getValue()), notNullValue());
        assertThat(token.getClaimsMap().get(TARGET_URL.getValue()).getValue(), equalTo(navigation.getTarget()));
    }
}
