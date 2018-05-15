package com.primasolutions.idp.context;

import com.primasolutions.idp.application.Application;
import com.primasolutions.idp.authentication.AuthenticationInfo;
import com.primasolutions.idp.authentication.Profile;
import com.primasolutions.idp.constants.AuthenticationInfoStatus;
import com.primasolutions.idp.constants.ProfileCode;
import com.primasolutions.idp.person.Person;
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

import static com.primasolutions.idp.constants.ProfileCode.MASTER;
import static com.primasolutions.idp.constants.ProfileCode.NAVIGATOR;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

@RunWith(DataProviderRunner.class)
public class IDPUserTest {

    public static final Date PASSED_TIME = new Date(System.currentTimeMillis() - (1000 * 60));
    public static final Date FUTURE_TIME = new Date(System.currentTimeMillis() + (1000 * 60));
    public static final String RANDOM_LOGIN = RandomStringUtils.random(20, true, true) + "@gmail.com";
    public static final String RANDOM_PASSWORD = RandomStringUtils.random(30, true, true);

    private IContextHolder contextHolder;
    private Person person;
    private Application application;
    private AuthenticationInfo authInfo;
    private IContext personContext;
    private IContext applicationContext;

    @Before
    public void setup() {
        contextHolder = new MockContextHolder();
        person = Person.newInstance();
        application = Application.newInstance();
        authInfo = AuthenticationInfo.newInstance();
        authInfo.setLogin(RANDOM_LOGIN);
        authInfo.setPassword(RANDOM_PASSWORD);
        person.setAuthenticationInfo(authInfo);
        application.setAuthenticationInfo(authInfo);
        personContext = contextHolder.newPersonContext(person);
        applicationContext = contextHolder.newApplicationContext(application);
    }

    @Test
    public void when_create_a_person_user_then_must_be_set() {
        authInfo.setExpireAt(FUTURE_TIME);
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
        authInfo.setExpireAt(FUTURE_TIME);
        IDPUser user = new IDPUser(applicationContext);
        assertThat(user.getContext(), equalTo(applicationContext));
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
        authInfo.setExpireAt(PASSED_TIME);
        IDPUser user = new IDPUser(personContext);
        assertThat(user.isAccountNonExpired(), equalTo(false));
    }

    @Test
    public void when_the_expiring_time_is_passed_then_the_application_accound_is_expired() {
        authInfo.setExpireAt(PASSED_TIME);
        IDPUser user = new IDPUser(applicationContext);
        assertThat(user.isAccountNonExpired(), equalTo(false));
    }

    @Test
    public void when_the_person_account_status_is_not_active_then_is_locked() {
        authInfo.setExpireAt(FUTURE_TIME);
        authInfo.setStatus(AuthenticationInfoStatus.LOCKED);
        IDPUser user = new IDPUser(personContext);
        assertThat(user.isAccountNonLocked(), equalTo(false));
    }

    @Test
    public void when_the_application_account_status_is_not_active_then_is_locked() {
        authInfo.setExpireAt(FUTURE_TIME);
        authInfo.setStatus(AuthenticationInfoStatus.LOCKED);
        IDPUser user = new IDPUser(applicationContext);
        assertThat(user.isAccountNonLocked(), equalTo(false));
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
    public void the_profiles_must_be_loaded(final String profile) {
        authInfo.setExpireAt(FUTURE_TIME);
        person.setProfiles(new HashSet<>(Arrays.asList(Profile.newInstance().setCode(ProfileCode.valueOf(profile)))));
        IDPUser user = new IDPUser(personContext);
        assertThat(user.getAuthorities(), notNullValue());
        assertThat(user.getAuthorities(), hasSize(1));
        assertThat(user.getAuthorities().toArray(new GrantedAuthority[]{})[0].getAuthority(),
                equalTo(ProfileCode.valueOf(profile).name()));
    }
}
