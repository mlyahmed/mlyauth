package com.mlyauth.utests.security.context;

import com.mlyauth.domain.AuthenticationInfo;
import com.mlyauth.domain.Person;
import com.mlyauth.security.context.IContext;
import com.mlyauth.security.context.IContextHolder;
import com.mlyauth.security.context.IDPUser;
import org.junit.Test;

import java.util.Date;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class IDPUserTest {

    @Test
    public void when_create_a_user_then_must_be_set() {

        IContextHolder contextHolder = new MockContextHolder();
        Person person = new Person();
        AuthenticationInfo authInfo = new AuthenticationInfo();
        authInfo.setLogin("ahmed.elidrissi.attach@gmail.com");
        authInfo.setPassword("secret");
        authInfo.setExpireOn(new Date(System.currentTimeMillis() - (1000 * 60)));

        person.setAuthenticationInfo(authInfo);
        final IContext context = contextHolder.newContext(person);
        IDPUser user = new IDPUser(context);
        assertThat(user.getContext(), equalTo(context));
        assertThat(user.getPerson(), equalTo(person));
        assertThat(user.getUsername(), equalTo("ahmed.elidrissi.attach@gmail.com"));
        assertThat(user.getPassword(), equalTo("secret"));
    }

    @Test
    public void when_the_account_is_expired_then_it_is() {
        IContextHolder contextHolder = new MockContextHolder();
        Person person = new Person();
        AuthenticationInfo authInfo = new AuthenticationInfo();
        authInfo.setLogin("ahmed.elidrissi.attach@gmail.com");
        authInfo.setPassword("secret");
        authInfo.setExpireOn(new Date(System.currentTimeMillis() - (1000 * 60)));

        person.setAuthenticationInfo(authInfo);
        final IContext context = contextHolder.newContext(person);


        IDPUser user = new IDPUser(context);
        assertThat(user.isAccountNonExpired(), equalTo(false));
    }
}
