package com.mlyauth.utests.security.context;

import com.mlyauth.constants.ProfileCode;
import com.mlyauth.dao.AuthenticationSessionDAO;
import com.mlyauth.domain.AuthenticationInfo;
import com.mlyauth.domain.AuthenticationSession;
import com.mlyauth.domain.Person;
import com.mlyauth.domain.Profile;
import com.mlyauth.mocks.dao.MockAuthenticationSessionDAO;
import com.mlyauth.security.context.ContextHolder;
import com.mlyauth.security.context.ContextIdGenerator;
import com.mlyauth.security.context.IContext;
import com.mlyauth.security.context.IContextIdGenerator;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.HashSet;

import static com.mlyauth.constants.AuthenticationSessionStatus.ACTIVE;
import static com.mlyauth.constants.AuthenticationSessionStatus.CLOSED;
import static com.mlyauth.constants.ProfileCode.MASTER;
import static com.mlyauth.constants.ProfileCode.NAVIGATOR;
import static java.lang.System.currentTimeMillis;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(DataProviderRunner.class)
public class ContextHolderTest {

    private AuthenticationInfo authenticationInfo;
    private Person person;
    private MockHttpServletRequest request;

    @Spy
    private IContextIdGenerator contextIdGenerator = new ContextIdGenerator();

    @Spy
    private AuthenticationSessionDAO sessionDAO = new MockAuthenticationSessionDAO();

    @InjectMocks
    private ContextHolder holder;

    @Before
    public void setup() {
        authenticationInfo = new AuthenticationInfo();
        authenticationInfo.setLogin(RandomStringUtils.random(20, true, true));
        authenticationInfo.setPassword(RandomStringUtils.random(20, true, true));
        person = new Person();
        person.setAuthenticationInfo(authenticationInfo);
        request = new MockHttpServletRequest();
        ServletRequestAttributes requestAttributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(requestAttributes);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void when_there_is_no_active_context_then_return_null() {
        the_no_context_is_set();
    }

    @Test
    public void given_there_is_a_session_when_create_a_context_then_save_it() {
        MockHttpSession session = new MockHttpSession();
        request.setSession(session);
        final IContext context = holder.newContext(person);
        assertThat(context, notNullValue());
        assertThat(holder.getId(), notNullValue());
        assertThat(context.getId(), notNullValue());
        assertThat(holder.getId(), equalTo(context.getId()));
        assertThat(holder.getContext(), equalTo(context));
        assertThat(context.getSession(), equalTo(session));
        assertThat(context.getPerson(), equalTo(person));
        assertThat(holder.getAuthenticationInfo(), equalTo(authenticationInfo));
        assertThat(context.getAuthenticationInfo(), equalTo(authenticationInfo));
        assertThat(context.getLogin(), equalTo(authenticationInfo.getLogin()));
        assertThat(holder.getLogin(), equalTo(authenticationInfo.getLogin()));
    }

    @Test
    public void when_create_a_new_context_then_create_a_new_auth_session() {
        request.setSession(new MockHttpSession());
        final IContext context = holder.newContext(person);
        assertThat(holder.getAuthenticationSession(), notNullValue());
        assertThat(context.getAuthenticationSession(), notNullValue());
        assertThat(holder.getAuthenticationSession(), equalTo(context.getAuthenticationSession()));
        assertThat(context.getAuthenticationSession().getId(), notNullValue());
        assertThat(sessionDAO.findOne(context.getAuthenticationSession().getId()), notNullValue());
        assertThat(context.getAuthenticationSession().getContextId(), equalTo(context.getId()));
        assertThat(context.getAuthenticationSession().getStatus(), equalTo(ACTIVE));
        assertThat(context.getAuthenticationSession().getCreatedAt(), notNullValue());
        assertTrue(context.getAuthenticationSession().getCreatedAt().getTime() - currentTimeMillis() < 50);
        assertThat(context.getAuthenticationSession().getAuthenticationInfo(), equalTo(authenticationInfo));
    }

    @Test
    public void given_an_existing_context_when_create_a_new_one_then_the_old_session_is_closed() {
        request.setSession(new MockHttpSession());
        IContext context = holder.newContext(person);
        final AuthenticationSession authSession1 = context.getAuthenticationSession();
        context = holder.newContext(person);
        final AuthenticationSession authSession2 = context.getAuthenticationSession();
        assertThat(authSession1, not(equalTo(authSession2)));
        assertThat(sessionDAO.findOne(authSession1.getId()).getStatus(), equalTo(CLOSED));
        assertThat(sessionDAO.findOne(authSession1.getId()).getClosedAt(), notNullValue());
        assertTrue(sessionDAO.findOne(authSession1.getId()).getClosedAt().getTime() - currentTimeMillis() < 50);
    }

    @Test
    public void given_an_existing_context_when_reset_it_then_close_the_session() {
        request.setSession(new MockHttpSession());
        IContext context = holder.newContext(person);
        final AuthenticationSession authSession = context.getAuthenticationSession();
        holder.reset();
        assertThat(sessionDAO.findOne(authSession.getId()).getStatus(), equalTo(CLOSED));
        assertThat(sessionDAO.findOne(authSession.getId()).getClosedAt(), notNullValue());
        assertTrue(sessionDAO.findOne(authSession.getId()).getClosedAt().getTime() - currentTimeMillis() < 50);
    }

    @Test
    public void given_there_are_many_sessions_when_create_a_context_then_it_must_be_bound_to_the_right_session() {
        MockHttpSession session1 = new MockHttpSession();
        MockHttpSession session2 = new MockHttpSession();
        request.setSession(session1);
        holder.newContext(person);
        request.setSession(session2);
        assertThat(holder.getContext(), nullValue());
    }

    @Test
    public void bind_attribute_to_the_current_context() {
        MockHttpSession session = new MockHttpSession();
        request.setSession(session);
        final IContext context = holder.newContext(person);
        context.putAttribute("clientId", "NBHG5487NJ");
        context.putAttribute("clientProfile", "CL");
        context.putAttribute("prestationId", "BA0000215487985");
        assertThat(holder.getAttribute("clientId"), equalTo("NBHG5487NJ"));
        assertThat(holder.getAttribute("clientProfile"), equalTo("CL"));
        assertThat(holder.getAttribute("prestationId"), equalTo("BA0000215487985"));
    }

    @Test
    public void bind_attribute_to_the_right_context() {
        MockHttpSession session1 = new MockHttpSession();
        MockHttpSession session2 = new MockHttpSession();
        request.setSession(session1);
        final IContext context = holder.newContext(person);
        context.putAttribute("clientId", "NBHG5487NJ");
        context.putAttribute("clientProfile", "CL");
        context.putAttribute("prestationId", "BA0000215487985");
        request.setSession(session2);
        assertThat(holder.getAttribute("clientId"), nullValue());
        assertThat(holder.getAttribute("clientProfile"), nullValue());
        assertThat(holder.getAttribute("prestationId"), nullValue());
    }

    @Test
    public void bind_holder_attribute_to_the_right_context() {
        MockHttpSession session = new MockHttpSession();
        request.setSession(session);
        holder.newContext(person);
        holder.putAttribute("clientId", "NBHG5487NJ");
        holder.putAttribute("clientProfile", "CL");
        holder.putAttribute("prestationId", "BA0000215487985");
        assertThat(holder.getAttribute("clientId"), equalTo("NBHG5487NJ"));
        assertThat(holder.getAttribute("clientProfile"), equalTo("CL"));
        assertThat(holder.getAttribute("prestationId"), equalTo("BA0000215487985"));
    }

    @Test
    public void bind_the_context_attributes_map_to_the_corrent_context() {
        MockHttpSession session = new MockHttpSession();
        request.setSession(session);
        final IContext context = holder.newContext(person);
        context.putAttribute("clientId", "NBHG5487NJ");
        context.putAttribute("clientProfile", "CL");
        context.putAttribute("prestationId", "BA0000215487985");
        assertThat(context.getAttributes(), notNullValue());
        assertThat(context.getAttributes().get("clientId"), equalTo("NBHG5487NJ"));
        assertThat(context.getAttributes().get("clientProfile"), equalTo("CL"));
        assertThat(context.getAttributes().get("prestationId"), equalTo("BA0000215487985"));
        assertThat(holder.getAttributes(), equalTo(context.getAttributes()));
    }

    @Test
    public void when_reset_then_remove_the_context() {
        MockHttpSession session = new MockHttpSession();
        request.setSession(session);
        holder.newContext(person);
        holder.reset();
        the_no_context_is_set();
    }

    private void the_no_context_is_set() {
        assertThat(holder.getId(), nullValue());
        assertThat(holder.getContext(), nullValue());
        assertThat(holder.getAuthenticationInfo(), nullValue());
        assertThat(holder.getAuthenticationSession(), nullValue());
        assertThat(holder.getLogin(), nullValue());
        assertThat(holder.getPassword(), nullValue());
        assertThat(holder.getPerson(), nullValue());
        assertThat(holder.getAttributes(), notNullValue());
        assertThat(holder.getAttributes().size(), equalTo(0));
        assertThat(holder.putAttribute("key", "value"), equalTo(false));
    }

    @Test
    public void when_create_new_context_and_same_session_then_keep_the_same_id() {
        request.setSession(new MockHttpSession());
        final IContext context1 = holder.newContext(person);
        final IContext context2 = holder.newContext(person);
        assertThat(context1.getId(), equalTo(context2.getId()));
    }

    @Test
    public void when_create_new_context_and_different_session_then_new_context_id() {
        request.setSession(new MockHttpSession());
        final IContext context1 = holder.newContext(person);
        request.setSession(new MockHttpSession());
        final IContext context2 = holder.newContext(person);
        assertThat(context1.getId(), not(equalTo(context2.getId())));
    }

    @Test
    public void when_there_is_no_profile_then_the_context_must_return_empty_collection() {
        request.setSession(new MockHttpSession());
        final IContext context = holder.newContext(person);
        assertThat(context.getProfiles(), hasSize(0));
        assertThat(holder.getProfiles(), hasSize(0));
    }

    @DataProvider
    public static Object[] profiles() {
        // @formatter:off
        return new String[]{
                MASTER.name(),
                NAVIGATOR.name()
        };
        // @formatter:on
    }

    @Test
    @UseDataProvider("profiles")
    public void the_context_must_return_the_profiles(String profile) {
        request.setSession(new MockHttpSession());
        person.setProfiles(new HashSet<>(Arrays.asList(Profile.newInstance().setCode(ProfileCode.valueOf(profile)))));
        final IContext context = holder.newContext(person);
        assertThat(context.getProfiles(), hasSize(1));
        assertThat(context.getProfiles().toArray(new Profile[]{})[0].getCode(), equalTo(ProfileCode.valueOf(profile)));
        assertThat(holder.getProfiles(), hasSize(1));
        assertThat(holder.getProfiles().toArray(new Profile[]{})[0].getCode(), equalTo(ProfileCode.valueOf(profile)));
    }
}
