package com.mlyauth.utests.security.context;

import com.mlyauth.domain.AuthenticationInfo;
import com.mlyauth.domain.Person;
import com.mlyauth.security.context.ContextHolder;
import com.mlyauth.security.context.IContext;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class ContextHolderTest {

    private AuthenticationInfo authenticationInfo;
    private Person person;
    private ContextHolder holder;
    private MockHttpServletRequest request;

    @Before
    public void setup() {
        authenticationInfo = new AuthenticationInfo();
        person = new Person();
        person.setAuthenticationInfo(authenticationInfo);
        holder = new ContextHolder();
        request = new MockHttpServletRequest();
        ServletRequestAttributes requestAttributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(requestAttributes);
    }

    @Test
    public void when_there_is_no_active_context_then_return_null() {
        assertThat(holder.getContext(), nullValue());
        assertThat(holder.getAuthenticationInfo(), nullValue());
        assertThat(holder.getPerson(), nullValue());
        assertThat(holder.getAttributes(), notNullValue());
        assertThat(holder.getAttributes().size(), equalTo(0));
        assertThat(holder.putAttribute("key", "value"), equalTo(false));
    }

    @Test
    public void given_there_is_a_session_when_create_a_context_then_save_it() {
        MockHttpSession session = new MockHttpSession();
        request.setSession(session);
        final IContext context = holder.newContext(person);
        assertThat(context, Matchers.notNullValue());
        assertThat(holder.getContext(), equalTo(context));
        assertThat(context.getSession(), equalTo(session));
        assertThat(context.getPerson(), equalTo(person));
        assertThat(holder.getAuthenticationInfo(), equalTo(authenticationInfo));
        assertThat(context.getAuthenticationInfo(), equalTo(authenticationInfo));
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
    public void bind_the_context_to_the_current_session() {
        IContext context = new MockContext();
        MockHttpSession session = new MockHttpSession();
        request.setSession(session);
        holder.setContext(context);
        assertThat(holder.getContext(), equalTo(context));
    }


    @Test
    public void bind_the_context_to_the_right_session() {
        MockHttpSession session1 = new MockHttpSession();
        MockHttpSession session2 = new MockHttpSession();
        request.setSession(session1);
        IContext context = new MockContext();
        holder.setContext(context);
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

}
