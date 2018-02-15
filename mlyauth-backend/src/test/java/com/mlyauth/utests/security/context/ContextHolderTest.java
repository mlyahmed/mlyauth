package com.mlyauth.utests.security.context;

import com.mlyauth.domain.AuthenticationInfo;
import com.mlyauth.domain.Person;
import com.mlyauth.security.context.ContextHolder;
import com.mlyauth.security.context.IContext;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.Assert.assertThat;

public class ContextHolderTest {

    @Test
    public void given_there_is_a_session_when_create_a_context_then_save_it() {
        MockHttpSession session = new MockHttpSession();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(session);
        ServletRequestAttributes requestAttributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(requestAttributes);

        ContextHolder holder = new ContextHolder();

        AuthenticationInfo authenticationInfo = new AuthenticationInfo();
        Person person = new Person();
        person.setAuthenticationInfo(authenticationInfo);

        final IContext context = holder.newContext(person);
        assertThat(context, Matchers.notNullValue());
        assertThat(context.getSession(), Matchers.equalTo(session));
        assertThat(context.getPerson(), Matchers.equalTo(person));
        assertThat(context.getAuthenticationInfo(), Matchers.equalTo(authenticationInfo));
    }
}
