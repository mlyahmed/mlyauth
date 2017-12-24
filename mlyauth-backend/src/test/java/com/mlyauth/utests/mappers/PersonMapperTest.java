package com.mlyauth.utests.mappers;

import com.google.common.collect.Sets;
import com.mlyauth.beans.PersonBean;
import com.mlyauth.dao.ApplicationDAO;
import com.mlyauth.domain.Application;
import com.mlyauth.domain.Person;
import com.mlyauth.mappers.PersonMapper;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(DataProviderRunner.class)
public class PersonMapperTest {


    private Person person;

    private PersonBean bean;

    @Mock
    private ApplicationDAO applicationDAO;

    @InjectMocks
    private PersonMapper mapper;

    @DataProvider
    public static Object[] properties() {
        // @formatter:off
        return new Object[][]{
                {1l, "Ahmed", "EL IDRISSI", "ahmed.elidrissi.attach", "ahmed.elidrissi.attach@gmail.com"},
                {2l, "Moulay", "ATTACH", "mlyahmed1", "mlyahmed1@gmail.com"},
                {3232l, "Fatima-Ezzahrae", "EL IDRISSI", "fatima.elidrissi", "fatima.elidrissi@yahoo.fr"},
        };
        // @formatter:on
    }


    /* Person to bean */

    @Test
    public void when_map_to_bean_and_null_then_return_null(){
        final PersonBean bean = mapper.toBean(null);
        assertThat(bean, Matchers.nullValue());
    }

    @DataProvider
    public static Object[] applications() {
        // @formatter:off
        return new Object[][] {
                {Application.newInstance().setAppname("Policy"), Application.newInstance().setAppname("Claims"), Application.newInstance().setAppname("Okta")},
                {Application.newInstance().setAppname("Google"), Application.newInstance().setAppname("Facebook"), Application.newInstance().setAppname("Twitter")},
                {Application.newInstance().setAppname("Yahoo"), Application.newInstance().setAppname("Amazon"), Application.newInstance().setAppname("Instagram")},
        };
        // @formatter:on
    }

    @Before
    public void setup() {
        initMocks(this);
        person = Person.newInstance();
        bean = PersonBean.newInstance();
    }

    @Test
    @UseDataProvider("properties")
    public void when_map_to_bean_then_map_properties(Object... properties) {
        given_person(properties);
        when_map_person_to_bean();
        then_person_properties_are_mapped_to_bean(properties);
    }

    private void given_person(Object[] properties) {
        person.setId((Long) properties[0])
                .setFirstname(String.valueOf(properties[1]))
                .setLastname(String.valueOf(properties[2]))
                .setUsername(String.valueOf(properties[3]))
                .setEmail(String.valueOf(properties[4]));
    }

    private void when_map_person_to_bean() {
        bean = mapper.toBean(person);
    }

    @DataProvider
    public static Object[] passwords() {
        // @formatter:off
        return new Object[] {
                "password",
                "pwd",
                "drowssap",
        };
        // @formatter:on
    }

    @SuppressWarnings("Duplicates")
    private void then_person_properties_are_mapped_to_bean(Object[] properties) {
        assertThat(bean, notNullValue());
        assertThat(bean.getId(), equalTo((Long) properties[0]));
        assertThat(bean.getFirstname(), equalTo(String.valueOf(properties[1])));
        assertThat(bean.getLastname(), equalTo(String.valueOf(properties[2])));
        assertThat(bean.getUsername(), equalTo(String.valueOf(properties[3])));
        assertThat(bean.getEmail(), equalTo(String.valueOf(properties[4])));
    }

    @Test
    @UseDataProvider("passwords")
    public void when_map_to_bean_then_do_not_map_password(String password){
        person.setPassword(password);
        when_map_person_to_bean();
        then_the_person_password_is_not_mapped();
    }

    private void then_the_person_password_is_not_mapped() {
        assertThat(bean, notNullValue());
        assertThat(bean.getPassword(), nullValue());
    }

    @Test
    public void when_map_to_bean_and_applications_is_null_then_map_applications_to_empty(){
        person.setApplications(null);
        when_map_person_to_bean();
        the_the_bean_applications_is_empty();
    }

    private void the_the_bean_applications_is_empty() {
        assertThat(bean, notNullValue());
        assertThat(bean.getApplications(), equalTo(Sets.newHashSet()));
    }

    @Test
    public void when_map_to_bean_and_application_code_is_null_then_map_application_to_empty() {
        person.setApplications(Sets.newHashSet(Application.newInstance().setAppname(null)));
        when_map_person_to_bean();
        the_the_bean_applications_is_empty();
    }

    @Test
    public void when_map_to_bean_and_one_application_then_map_application_to_code() {
        person.setApplications(Sets.newHashSet(Application.newInstance().setAppname("Policy")));
        bean = mapper.toBean(person);
        assertThat(bean, notNullValue());
        assertThat(bean.getApplications(), equalTo(Sets.newHashSet("Policy")));
    }

    @Test
    @UseDataProvider("applications")
    public void when_map_to_bean_then_map_applications_to_appnames(Application... apps) {
        person.setApplications(Sets.newHashSet(apps));
        when_map_person_to_bean();
        then_applications_are_mapped_to_appnames(apps);
    }

    private void then_applications_are_mapped_to_appnames(Application[] apps) {
        assertThat(bean, notNullValue());
        assertThat(bean.getApplications(), equalTo(Arrays.stream(apps).map(Application::getAppname).collect(Collectors.toSet())));
    }

    /* Bean to Person */

    @Test
    public void when_map_bean_to_person_and_null_then_return_null() {
        person = mapper.toEntity(null);
        assertThat(person, nullValue());
    }


    @Test
    @UseDataProvider("properties")
    public void when_map_bean_to_person_then_map_properties(Object... properties) {
        given_bean(properties);
        when_map_bean_to_person();
        then_bean_properties_are_mapped_to_person(properties);
    }

    @SuppressWarnings("Duplicates")
    private void then_bean_properties_are_mapped_to_person(Object... properties) {
        assertThat(person, notNullValue());
        assertThat(person.getId(), equalTo((Long) properties[0]));
        assertThat(person.getFirstname(), equalTo(String.valueOf(properties[1])));
        assertThat(person.getLastname(), equalTo(String.valueOf(properties[2])));
        assertThat(person.getUsername(), equalTo(String.valueOf(properties[3])));
        assertThat(person.getEmail(), equalTo(String.valueOf(properties[4])));
    }

    private void when_map_bean_to_person() {
        person = mapper.toEntity(bean);
    }

    private void given_bean(Object... properties) {
        bean.setId((Long) properties[0])
                .setFirstname(String.valueOf(properties[1]))
                .setLastname(String.valueOf(properties[2]))
                .setUsername(String.valueOf(properties[3]))
                .setEmail(String.valueOf(properties[4]));
    }


    @Test
    @UseDataProvider("applications")
    public void when_map_bean_to_person_and_assigned_to_policy_then_map_policy_to_application(Application... apps) {
        bean.setApplications(Arrays.stream(apps).map(Application::getAppname).collect(Collectors.toSet()));
        Arrays.stream(apps).forEach(app -> when(applicationDAO.findByAppname(app.getAppname())).thenReturn(app));
        when_map_bean_to_person();
        assertThat(person, notNullValue());
        assertThat(person.getApplications(), notNullValue());
        assertThat(person.getApplications().size(), Matchers.equalTo(apps.length));
        assertThat(person.getApplications(), equalTo(Sets.newHashSet(apps)));
    }

}