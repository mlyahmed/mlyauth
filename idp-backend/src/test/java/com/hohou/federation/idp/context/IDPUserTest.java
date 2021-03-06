package com.hohou.federation.idp.context;

import com.hohou.federation.idp.application.Application;
import com.hohou.federation.idp.authentication.AuthInfo;
import com.hohou.federation.idp.authentication.Profile;
import com.hohou.federation.idp.constants.AuthInfoStatus;
import com.hohou.federation.idp.constants.ProfileCode;
import com.hohou.federation.idp.context.mocks.MockContextHolder;
import com.hohou.federation.idp.person.model.Person;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.core.GrantedAuthority;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

@RunWith(DataProviderRunner.class)
public class IDPUserTest {

    private static final String RANDOM_LOGIN = RandomStringUtils.random(20, true, true) + "@gmail.com";
    private static final String RANDOM_PASSWORD = RandomStringUtils.random(30, true, true);
    private static final int SIXTY_SECONDS = 1000 * 60;
    private static final int TEN_MINUTES = 1000 * 600;

    private IContextHolder contextHolder;
    private Person person;
    private Application application;
    private AuthInfo authInfo;
    private IContext personContext;
    private IContext context;

    @Before
    public void setup() {
        contextHolder = new MockContextHolder();
        person = Person.newInstance();
        application = Application.newInstance();
        authInfo = AuthInfo.newInstance();
        authInfo.setLogin(RANDOM_LOGIN);
        authInfo.setPassword(RANDOM_PASSWORD);
        person.setAuthenticationInfo(authInfo);
        application.setAuthenticationInfo(authInfo);
        personContext = contextHolder.newPersonContext(person);
        context = contextHolder.newApplicationContext(application);
    }

    @Test
    public void when_create_a_person_user_then_must_be_set() {
        authInfo.setExpireAt(futureTime());
        IDPUser user = new IDPUser(personContext);
        assertThat(user.getContext(), equalTo(personContext));
        assertThat(user.getPerson(), equalTo(person));
        assertThat(user.isAPerson(), equalTo(true));
        assertThat(user.getApplication(), nullValue());
        assertThat(user.isAnApplication(), equalTo(false));
        assertThat(user.getUsername(), equalTo(authInfo.getLogin()));
        assertThat(user.getPassword(), equalTo(authInfo.getPassword()));
        assertThat(user.isAccountNonExpired(), equalTo(true));
        assertThat(user.isAccountNonLocked(), equalTo(true));
    }

    @Test
    public void when_create_an_application_user_then_must_be_set() {
        authInfo.setExpireAt(futureTime());
        IDPUser user = new IDPUser(context);
        assertThat(user.getContext(), equalTo(context));
        assertThat(user.getPerson(), nullValue());
        assertThat(user.isAPerson(), equalTo(false));
        assertThat(user.getApplication(), equalTo(application));
        assertThat(user.isAnApplication(), equalTo(true));
        assertThat(user.getUsername(), equalTo(authInfo.getLogin()));
        assertThat(user.getPassword(), equalTo(authInfo.getPassword()));
        assertThat(user.isAccountNonExpired(), equalTo(true));
        assertThat(user.isAccountNonLocked(), equalTo(true));
    }

    @Test
    public void when_the_expiring_time_is_passed_then_the_person_accound_is_expired() {
        authInfo.setExpireAt(passedTime());
        IDPUser user = new IDPUser(personContext);
        assertThat(user.isAccountNonExpired(), equalTo(false));
    }

    @Test
    public void when_the_expiring_time_is_passed_then_the_application_accound_is_expired() {
        authInfo.setExpireAt(passedTime());
        IDPUser user = new IDPUser(context);
        assertThat(user.isAccountNonExpired(), equalTo(false));
    }

    @Test
    public void when_the_person_account_status_is_not_active_then_is_locked() {
        authInfo.setExpireAt(futureTime());
        authInfo.setStatus(AuthInfoStatus.LOCKED);
        IDPUser user = new IDPUser(personContext);
        assertThat(user.isAccountNonLocked(), equalTo(false));
    }

    @Test
    public void when_the_application_account_status_is_not_active_then_is_locked() {
        authInfo.setExpireAt(futureTime());
        authInfo.setStatus(AuthInfoStatus.LOCKED);
        IDPUser user = new IDPUser(context);
        assertThat(user.isAccountNonLocked(), equalTo(false));
    }

    @DataProvider
    public static Object[] profiles() {
        // @formatter:off
        return new String[]{
                ProfileCode.MASTER.name(),
                ProfileCode.NAVIGATOR.name()
        };
        // @formatter:on
    }

    @Test
    @UseDataProvider("profiles")
    public void the_profiles_must_be_loaded(final String profile) {
        authInfo.setExpireAt(futureTime());
        person.setProfiles(new HashSet<>(Arrays.asList(Profile.newInstance().setCode(ProfileCode.valueOf(profile)))));
        IDPUser user = new IDPUser(personContext);
        assertThat(user.getAuthorities(), notNullValue());
        assertThat(user.getAuthorities(), hasSize(1));
        assertThat(user.getAuthorities().toArray(new GrantedAuthority[]{})[0].getAuthority(),
                equalTo(ProfileCode.valueOf(profile).name()));
    }

    private Date futureTime() {
        return new Date(System.currentTimeMillis() + TEN_MINUTES);
    }

    private Date passedTime() {
        return new Date(System.currentTimeMillis() - SIXTY_SECONDS);
    }
}
