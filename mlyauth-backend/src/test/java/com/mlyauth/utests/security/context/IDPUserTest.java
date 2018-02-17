package com.mlyauth.utests.security.context;

import com.mlyauth.domain.AuthenticationInfo;
import com.mlyauth.domain.Person;
import com.mlyauth.security.context.IContext;
import com.mlyauth.security.context.IContextHolder;
import com.mlyauth.security.context.IDPUser;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

public class IDPUserTest {

    @Test
    public void when_create_a_user_then_must_be_set() {
        IContextHolder contextHolder = new MockContextHolder();
        Person person = new Person();
        AuthenticationInfo authInfo = new AuthenticationInfo();
        authInfo.setLogin("ahmed.elidrissi.attach@gmail.com");
        authInfo.setPassword("secret");
        person.setAuthenticationInfo(authInfo);
        final IContext context = contextHolder.newContext(person);
        IDPUser user = new IDPUser(context);
        Assert.assertThat(user.getContext(), Matchers.equalTo(context));
    }
}
