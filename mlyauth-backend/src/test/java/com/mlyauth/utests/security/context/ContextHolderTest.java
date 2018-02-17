package com.mlyauth.utests.security.context;

import com.mlyauth.domain.AuthenticationInfo;
import com.mlyauth.domain.Person;
import com.mlyauth.exception.ContextAlreadyLoaded;
import com.mlyauth.security.context.ContextHolder;
import com.mlyauth.security.context.ContextIdGenerator;
import com.mlyauth.security.context.IContext;
import com.mlyauth.security.context.IContextIdGenerator;
import org.apache.commons.lang.RandomStringUtils;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class ContextHolderTest {

    private AuthenticationInfo authenticationInfo;
    private Person person;
    private MockHttpServletRequest request;

    @Spy
    private IContextIdGenerator contextIdGenerator = new ContextIdGenerator();

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
        assertThat(context, Matchers.notNullValue());
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
        assertThat(holder.getLogin(), nullValue());
        assertThat(holder.getPassword(), nullValue());
        assertThat(holder.getPerson(), nullValue());
        assertThat(holder.getAttributes(), notNullValue());
        assertThat(holder.getAttributes().size(), equalTo(0));
        assertThat(holder.putAttribute("key", "value"), equalTo(false));
    }

    @Test(expected = ContextAlreadyLoaded.class)
    public void when_create_new_context_and_already_existed_one_then_error() {
        MockHttpSession session = new MockHttpSession();
        request.setSession(session);
        holder.newContext(person);
        holder.newContext(person);
    }

}
