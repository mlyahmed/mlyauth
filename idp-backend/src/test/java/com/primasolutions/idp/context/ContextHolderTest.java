package com.primasolutions.idp.context;

import com.primasolutions.idp.constants.ProfileCode;
import com.primasolutions.idp.dao.AuthenticationSessionDAO;
import com.primasolutions.idp.dao.MockAuthenticationSessionDAO;
import com.primasolutions.idp.domain.Application;
import com.primasolutions.idp.domain.AuthenticationInfo;
import com.primasolutions.idp.domain.AuthenticationSession;
import com.primasolutions.idp.domain.Person;
import com.primasolutions.idp.domain.Profile;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashSet;

import static com.primasolutions.idp.constants.AuthenticationSessionStatus.ACTIVE;
import static com.primasolutions.idp.constants.AuthenticationSessionStatus.CLOSED;
import static com.primasolutions.idp.constants.ProfileCode.MASTER;
import static com.primasolutions.idp.constants.ProfileCode.NAVIGATOR;
import static com.primasolutions.idp.tools.RandomForTests.randomString;
import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(DataProviderRunner.class)
public class ContextHolderTest {
    public static final String RANDOM_LOGIN = RandomStringUtils.random(20, true, true);

    public static final String RANDOM_PASSWORD = RandomStringUtils.random(20, true, true);
    public static final int FIFTY_MILLISECONDS = 50;

    private AuthenticationInfo authenticationInfo;

    private Person person;

    private Application application;

    private MockHttpServletRequest request;

    @Spy
    private AuthenticationSessionDAO sessionDAO = new MockAuthenticationSessionDAO();

    @InjectMocks
    private ContextHolder holder;
    private IContext context;

    @Before
    public void setup() {
        authenticationInfo = new AuthenticationInfo();
        authenticationInfo.setLogin(RANDOM_LOGIN);
        authenticationInfo.setPassword(RANDOM_PASSWORD);
        person = Person.newInstance();
        person.setAuthenticationInfo(authenticationInfo);
        application = Application.newInstance();
        application.setAuthenticationInfo(authenticationInfo);
        request = new MockHttpServletRequest();
        request.setSession(new MockHttpSession());
        ServletRequestAttributes requestAttributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(requestAttributes);
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(holder, "idGenerator", new ContextIdGenerator());
    }

    @Test
    public void when_there_is_no_active_context_then_return_null() {
        the_no_context_is_set();
    }

    @Test
    public void given_there_is_a_session_when_create_a_person_context_then_save_it() {
        context = holder.newPersonContext(person);
        assertThat(context, notNullValue());
        assertThat(holder.getId(), notNullValue());
        assertThat(context.getId(), notNullValue());
        assertThat(holder.getId(), equalTo(context.getId()));
        assertThat(holder.getContext(), equalTo(context));
        assertThat(context.getSession(), equalTo(request.getSession()));
        assertThat(context.getPerson(), equalTo(person));
        assertThat(context.getApplication(), nullValue());
        assertThat(holder.getAuthenticationInfo(), equalTo(authenticationInfo));
        assertThat(context.getAuthenticationInfo(), equalTo(authenticationInfo));
        assertThat(context.getLogin(), equalTo(authenticationInfo.getLogin()));
        assertThat(holder.getLogin(), equalTo(authenticationInfo.getLogin()));
    }

    @Test
    public void given_there_is_a_session_when_create_an_application_context_then_save_it() {
        context = holder.newApplicationContext(application);
        assertThat(context, notNullValue());
        assertThat(holder.getId(), notNullValue());
        assertThat(context.getId(), notNullValue());
        assertThat(holder.getId(), equalTo(context.getId()));
        assertThat(holder.getContext(), equalTo(context));
        assertThat(context.getSession(), equalTo(request.getSession()));
        assertThat(context.getPerson(), nullValue());
        assertThat(context.getApplication(), equalTo(application));
        assertThat(holder.getAuthenticationInfo(), equalTo(authenticationInfo));
        assertThat(context.getAuthenticationInfo(), equalTo(authenticationInfo));
        assertThat(context.getLogin(), equalTo(authenticationInfo.getLogin()));
        assertThat(holder.getLogin(), equalTo(authenticationInfo.getLogin()));
    }

    @Test
    public void when_create_a_new_person_context_then_create_a_new_auth_session() {
        context = holder.newPersonContext(person);
        then_a_new_auth_session_is_created();
    }

    @Test
    public void when_create_a_new_application_context_then_create_a_new_auth_session() {
        context = holder.newApplicationContext(application);
        then_a_new_auth_session_is_created();
    }

    private void then_a_new_auth_session_is_created() {
        final AuthenticationSession holderSession = holder.getAuthenticationSession();
        final AuthenticationSession contextSession = context.getAuthenticationSession();
        assertThat(holderSession, notNullValue());
        assertThat(contextSession, notNullValue());
        assertThat(holderSession, equalTo(contextSession));
        assertThat(contextSession.getId(), notNullValue());
        assertThat(sessionDAO.findOne(contextSession.getId()), notNullValue());
        assertThat(contextSession.getContextId(), equalTo(context.getId()));
        assertThat(contextSession.getStatus(), equalTo(ACTIVE));
        assertThat(contextSession.getCreatedAt(), notNullValue());
        assertTrue(contextSession.getCreatedAt().getTime() - currentTimeMillis() < FIFTY_MILLISECONDS);
        assertThat(contextSession.getAuthenticationInfo(), equalTo(authenticationInfo));
    }


    @Test
    public void given_an_existing_context_when_create_a_new_person_context_then_the_old_session_is_closed() {
        context = holder.newPersonContext(person);
        final AuthenticationSession firstSession = context.getAuthenticationSession();
        context = holder.newPersonContext(person);
        final AuthenticationSession secondSession = context.getAuthenticationSession();
        then_the_first_session_is_closed_and_new_one_is_created(firstSession, secondSession);
    }

    @Test
    public void given_an_existing_context_when_create_a_new_application_context_then_the_old_session_is_closed() {
        context = holder.newApplicationContext(application);
        final AuthenticationSession firstSession = context.getAuthenticationSession();
        context = holder.newApplicationContext(application);
        final AuthenticationSession secondSession = context.getAuthenticationSession();
        then_the_first_session_is_closed_and_new_one_is_created(firstSession, secondSession);
    }

    @Test
    public void given_an_existing_person_context_when_reset_it_then_close_the_session() {
        context = holder.newPersonContext(person);
        final AuthenticationSession authSession = context.getAuthenticationSession();
        holder.reset();
        then_the_auth_session_is_closed(authSession);
    }

    @Test
    public void given_an_existing_application_context_when_reset_it_then_close_the_session() {
        context = holder.newApplicationContext(application);
        final AuthenticationSession authSession = context.getAuthenticationSession();
        holder.reset();
        then_the_auth_session_is_closed(authSession);
    }

    @Test
    public void given_many_sessions_when_create_a_person_context_then_it_must_be_bound_to_the_right_session() {
        MockHttpSession session1 = new MockHttpSession();
        MockHttpSession session2 = new MockHttpSession();
        request.setSession(session1);
        holder.newPersonContext(person);
        request.setSession(session2);
        assertThat(holder.getContext(), nullValue());
    }

    @Test
    public void given_many_sessions_when_create_an_application_context_then_it_must_be_bound_to_the_right_session() {
        MockHttpSession session1 = new MockHttpSession();
        MockHttpSession session2 = new MockHttpSession();
        request.setSession(session1);
        holder.newApplicationContext(application);
        request.setSession(session2);
        assertThat(holder.getContext(), nullValue());
    }

    @Test
    public void bind_attribute_to_the_current_person_context() {
        context = holder.newPersonContext(person);
        context.putAttribute("bp", "NBHG5487NJ");
        context.putAttribute("clientProfile", "CL");
        context.putAttribute("prestationId", "BA0000215487985");
        assertThat(holder.getAttribute("bp"), equalTo("NBHG5487NJ"));
        assertThat(holder.getAttribute("clientProfile"), equalTo("CL"));
        assertThat(holder.getAttribute("prestationId"), equalTo("BA0000215487985"));
    }

    @Test
    public void bind_attribute_to_the_current_application_context() {
        context = holder.newApplicationContext(application);
        context.putAttribute("audience", "NCBHDY5485DS");
        context.putAttribute("profile", "GS");
        context.putAttribute("prestation", "BA002154548s");
        assertThat(holder.getAttribute("audience"), equalTo("NCBHDY5485DS"));
        assertThat(holder.getAttribute("profile"), equalTo("GS"));
        assertThat(holder.getAttribute("prestation"), equalTo("BA002154548s"));
    }

    @Test
    public void bind_attribute_to_the_right_person_context() {
        request.setSession(new MockHttpSession());
        context = holder.newPersonContext(person);
        context.putAttribute("target", randomString());
        context.putAttribute("clientProfile", randomString());
        context.putAttribute("prestationId", randomString());
        request.setSession(new MockHttpSession());
        assertThat(holder.getAttribute("target"), nullValue());
        assertThat(holder.getAttribute("clientProfile"), nullValue());
        assertThat(holder.getAttribute("prestationId"), nullValue());
    }

    @Test
    public void bind_attribute_to_the_right_application_context() {
        request.setSession(new MockHttpSession());
        context = holder.newApplicationContext(application);
        context.putAttribute("verdict", randomString());
        context.putAttribute("delegator", randomString());
        context.putAttribute("delegate", randomString());
        request.setSession(new MockHttpSession());
        assertThat(holder.getAttribute("verdict"), nullValue());
        assertThat(holder.getAttribute("delegator"), nullValue());
        assertThat(holder.getAttribute("delegate"), nullValue());
    }

    @Test
    public void bind_holder_attribute_to_the_right_person_context() {
        holder.newPersonContext(person);
        holder.putAttribute("clientId", "NBHG5487NJ");
        holder.putAttribute("clientProfile", "CL");
        holder.putAttribute("prestationId", "BA0000215487985");
        assertThat(holder.getAttribute("clientId"), equalTo("NBHG5487NJ"));
        assertThat(holder.getAttribute("clientProfile"), equalTo("CL"));
        assertThat(holder.getAttribute("prestationId"), equalTo("BA0000215487985"));
    }

    @Test
    public void bind_holder_attribute_to_the_right_application_context() {
        holder.newApplicationContext(application);
        holder.putAttribute("state", "NBHGS2514548SSS");
        holder.putAttribute("client", "XT");
        holder.putAttribute("stamp", "BA000021SADSD2323");
        assertThat(holder.getAttribute("state"), equalTo("NBHGS2514548SSS"));
        assertThat(holder.getAttribute("client"), equalTo("XT"));
        assertThat(holder.getAttribute("stamp"), equalTo("BA000021SADSD2323"));
    }

    @Test
    public void bind_the_context_attributes_map_to_the_current_person_context() {
        context = holder.newPersonContext(person);
        context.putAttribute("subject", "BVSGFSGFS");
        context.putAttribute("issuer", "CXFST");
        context.putAttribute("audience", "BA00002154215487");
        assertThat(context.getAttributes(), notNullValue());
        assertThat(context.getAttributes().get("subject"), equalTo("BVSGFSGFS"));
        assertThat(context.getAttributes().get("issuer"), equalTo("CXFST"));
        assertThat(context.getAttributes().get("audience"), equalTo("BA00002154215487"));
        assertThat(holder.getAttributes(), equalTo(context.getAttributes()));
    }

    @Test
    public void bind_the_context_attributes_map_to_the_current_application_context() {
        context = holder.newApplicationContext(application);
        context.putAttribute("BP", "JHSJHSJS");
        context.putAttribute("STATE", "SAD54545454D");
        context.putAttribute("AUD", "TA21212121DEFT");
        assertThat(context.getAttributes(), notNullValue());
        assertThat(context.getAttributes().get("BP"), equalTo("JHSJHSJS"));
        assertThat(context.getAttributes().get("STATE"), equalTo("SAD54545454D"));
        assertThat(context.getAttributes().get("AUD"), equalTo("TA21212121DEFT"));
        assertThat(holder.getAttributes(), equalTo(context.getAttributes()));
    }

    @Test
    public void when_reset_then_remove_the_person_context() {
        holder.newPersonContext(person);
        holder.reset();
        the_no_context_is_set();
    }

    @Test
    public void when_reset_then_remove_the_application_context() {
        holder.newApplicationContext(application);
        holder.reset();
        the_no_context_is_set();
    }

    @Test
    public void when_create_new_person_context_and_same_session_then_keep_the_same_id() {
        final IContext firstContext = holder.newPersonContext(person);
        final IContext secondContext = holder.newPersonContext(person);
        assertThat(firstContext.getId(), equalTo(secondContext.getId()));
    }

    @Test
    public void when_create_new_application_context_and_same_session_then_keep_the_same_id() {
        final IContext firstContext = holder.newApplicationContext(application);
        final IContext secondContext = holder.newApplicationContext(application);
        assertThat(firstContext.getId(), equalTo(secondContext.getId()));
    }

    @Test
    public void when_create_new_person_context_and_different_session_then_new_context_id() {
        request.setSession(new MockHttpSession());
        final IContext firstContext = holder.newPersonContext(person);
        request.setSession(new MockHttpSession());
        final IContext secondContext = holder.newPersonContext(person);
        assertThat(firstContext.getId(), not(equalTo(secondContext.getId())));
    }

    @Test
    public void when_create_new_application_context_and_different_session_then_new_context_id() {
        request.setSession(new MockHttpSession());
        final IContext firstContext = holder.newApplicationContext(application);
        request.setSession(new MockHttpSession());
        final IContext secondContext = holder.newApplicationContext(application);
        assertThat(firstContext.getId(), not(equalTo(secondContext.getId())));
    }

    @Test
    public void when_there_is_no_profile_then_the_person_context_must_return_empty_collection() {
        context = holder.newPersonContext(person);
        assertThat(context.getProfiles(), hasSize(0));
        assertThat(holder.getProfiles(), hasSize(0));
    }

    @Test
    public void when_there_is_no_profile_then_the_application_context_must_return_empty_collection() {
        context = holder.newApplicationContext(application);
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
    public void the_person_context_must_return_the_profiles(final String profile) {
        person.setProfiles(new HashSet<>(asList(Profile.newInstance().setCode(ProfileCode.valueOf(profile)))));
        context = holder.newPersonContext(person);
        assertThat(context.getProfiles(), hasSize(1));
        assertThat(context.getProfiles().toArray(new Profile[]{})[0].getCode(), equalTo(ProfileCode.valueOf(profile)));
        assertThat(holder.getProfiles(), hasSize(1));
        assertThat(holder.getProfiles().toArray(new Profile[]{})[0].getCode(), equalTo(ProfileCode.valueOf(profile)));
    }

    @Test
    @UseDataProvider("profiles")
    public void the_application_context_must_return_the_profiles(final String profile) {
        application.setProfiles(new HashSet<>(asList(Profile.newInstance().setCode(ProfileCode.valueOf(profile)))));
        context = holder.newApplicationContext(application);
        assertThat(context.getProfiles(), hasSize(1));
        assertThat(context.getProfiles().toArray(new Profile[]{})[0].getCode(), equalTo(ProfileCode.valueOf(profile)));
        assertThat(holder.getProfiles(), hasSize(1));
        assertThat(holder.getProfiles().toArray(new Profile[]{})[0].getCode(), equalTo(ProfileCode.valueOf(profile)));
    }

    private void then_the_first_session_is_closed_and_new_one_is_created(final AuthenticationSession firstSession,
                                                                         final AuthenticationSession secondSession) {
        assertThat(firstSession, not(equalTo(secondSession)));
        assertThat(sessionDAO.findOne(firstSession.getId()).getStatus(), equalTo(CLOSED));
        assertThat(sessionDAO.findOne(firstSession.getId()).getClosedAt(), notNullValue());
        assertTrue(sessionDAO.findOne(firstSession.getId()).getClosedAt().getTime() - currentTimeMillis()
                < FIFTY_MILLISECONDS);
    }

    private void then_the_auth_session_is_closed(final AuthenticationSession authSession) {
        assertThat(sessionDAO.findOne(authSession.getId()).getStatus(), equalTo(CLOSED));
        assertThat(sessionDAO.findOne(authSession.getId()).getClosedAt(), notNullValue());
        assertTrue(sessionDAO.findOne(authSession.getId()).getClosedAt().getTime() - currentTimeMillis()
                < FIFTY_MILLISECONDS);
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
}
