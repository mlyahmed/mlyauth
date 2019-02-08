package com.hohou.federation.idp.navigation;

import com.hohou.federation.idp.AbstractIntegrationTest;
import com.hohou.federation.idp.application.AppAspAttr;
import com.hohou.federation.idp.application.Application;
import com.hohou.federation.idp.application.ApplicationAspectAttributeDAO;
import com.hohou.federation.idp.application.ApplicationDAO;
import com.hohou.federation.idp.constants.AspectAttribute;
import com.hohou.federation.idp.constants.AspectType;
import com.hohou.federation.idp.constants.Direction;
import com.hohou.federation.idp.constants.TokenNorm;
import com.hohou.federation.idp.constants.TokenPurpose;
import com.hohou.federation.idp.constants.TokenStatus;
import com.hohou.federation.idp.constants.TokenType;
import com.hohou.federation.idp.context.IContext;
import com.hohou.federation.idp.token.Claims;
import com.hohou.federation.idp.token.Token;
import com.hohou.federation.idp.token.TokenDAO;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static com.hohou.federation.idp.constants.AspectAttribute.SP_SAML_SSO_URL;
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
        final Map<AspectAttribute, AppAspAttr> attributes = attributesDAO.findAndIndex(policy.getId(), AspectType.SP_SAML.name());
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
        final Navigation navigation = navigationDAO.findById(navigationBean.getId()).get();
        final NavigationAttribute samlResponse = navigation.getAttribute("SAMLResponse");
        assertThat(navigation, notNullValue());
        assertThat(navigation.getCreatedAt(), notNullValue());
        assertThat(navigation.getDirection(), equalTo(Direction.OUTBOUND));
        assertThat(navigation.getTargetURL(), equalTo(navigationBean.getTarget()));
        assertThat(navigation.getToken(), notNullValue());
        assertThat(navigation.getToken().getStatus(), equalTo(TokenStatus.CHECKED));
        assertThat(navigation.getToken().getChecksum(), equalTo(DigestUtils.sha256Hex(samlResponse.getValue())));
        assertThat(navigation.getSession(), equalTo(context.getAuthenticationSession()));
    }

    @Test
    public void when_generate_a_saml_navigation_then_trace_the_navigation_attribute() {
        final NavigationBean navigationBean = navigationService.newNavigation(POLICY_DEV);
        final Navigation navigation = navigationDAO.findById(navigationBean.getId()).get();
        final NavigationAttribute samlResponse = navigation.getAttribute("SAMLResponse");
        assertThat(samlResponse, notNullValue());
        assertThat(navigation.getAttribute("SAMLResponse").getValue(), equalTo(samlResponse.getValue()));
    }

    @Test
    public void when_generate_a_saml_navigation_then_trace_the_token() {
        final NavigationBean navigation = navigationService.newNavigation(POLICY_DEV);
        assertThat(navigation.getTokenId(), notNullValue());
        final Token token = tokenDAO.findById(navigation.getTokenId()).get();
        assertThat(token, notNullValue());
        assertThat(token.getNorm(), equalTo(TokenNorm.SAML));
        assertThat(token.getType(), equalTo(TokenType.ACCESS));
        assertThat(token.getPurpose(), equalTo(TokenPurpose.NAVIGATION));
        assertThat(token.getIssuanceTime(), notNullValue());
        assertThat(token.getEffectiveTime(), notNullValue());
        assertThat(token.getExpiryTime(), notNullValue());
        assertThat(token.getSession(), equalTo(context.getAuthenticationSession()));
        assertThat(token.getClaimsMap(), notNullValue());
        assertThat(token.getClaimsMap().get(Claims.TARGET_URL.getValue()), notNullValue());
        assertThat(token.getClaimsMap().get(Claims.TARGET_URL.getValue()).getValue(), equalTo(navigation.getTarget()));
    }
}
