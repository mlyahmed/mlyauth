package com.mlyauth.utests.security.context;

import com.mlyauth.constants.AuthenticationInfoStatus;
import com.mlyauth.domain.AuthenticationInfo;
import com.mlyauth.domain.Person;
import com.mlyauth.domain.Profile;
import com.mlyauth.security.context.IContext;
import com.mlyauth.security.context.IContextHolder;
import com.mlyauth.security.context.IDPUser;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

import static com.mlyauth.constants.ProfileCode.MASTER;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class IDPUserTest {

    public static final Date PASSED_TIME = new Date(System.currentTimeMillis() - (1000 * 60));
    public static final Date FUTURE_TIME = new Date(System.currentTimeMillis() + (1000 * 60));
    public static final String RANDOM_LOGIN = RandomStringUtils.random(20, true, true) + "@gmail.com";
    public static final String RANDOM_PASSWORD = RandomStringUtils.random(30, true, true);

    private IContextHolder contextHolder;
    private Person person;
    private AuthenticationInfo authInfo;
    private IContext context;

    @Before
    public void setup() {
        contextHolder = new MockContextHolder();
        person = new Person();
        authInfo = new AuthenticationInfo();
        authInfo.setLogin(RANDOM_LOGIN);
        authInfo.setPassword(RANDOM_PASSWORD);
        person.setAuthenticationInfo(authInfo);
        context = contextHolder.newContext(person);
    }

    @Test
    public void when_create_a_user_then_must_be_set() {
        authInfo.setExpireAt(FUTURE_TIME);
        IDPUser user = new IDPUser(context);
        assertThat(user.getContext(), equalTo(context));
        assertThat(user.getPerson(), equalTo(person));
        assertThat(user.getUsername(), equalTo(authInfo.getLogin()));
        assertThat(user.getPassword(), equalTo(authInfo.getPassword()));
        assertThat(user.isAccountNonExpired(), equalTo(true));
        assertThat(user.isAccountNonLocked(), equalTo(true));
    }

    @Test
    public void when_the_expiring_time_is_passed_then_the_accound_is_expired() {
        authInfo.setExpireAt(PASSED_TIME);
        IDPUser user = new IDPUser(context);
        assertThat(user.isAccountNonExpired(), equalTo(false));
    }

    @Test
    public void when_the_account_status_is_not_active_then_is_locked() {
        authInfo.setExpireAt(FUTURE_TIME);
        authInfo.setStatus(AuthenticationInfoStatus.LOCKED);
        IDPUser user = new IDPUser(context);
        assertThat(user.isAccountNonLocked(), equalTo(false));
    }

    @Test
    public void the_profiles_must_be_loaded() {
        authInfo.setExpireAt(FUTURE_TIME);
        person.setProfiles(new HashSet<>(Arrays.asList(Profile.newInstance().setCode(MASTER))));
        IDPUser user = new IDPUser(context);
        assertThat(user.getAuthorities(), notNullValue());
        assertThat(user.getAuthorities(), hasSize(1));
        assertThat(user.getAuthorities().toArray(new GrantedAuthority[]{})[0].getAuthority(), equalTo(MASTER.name()));
    }
}
