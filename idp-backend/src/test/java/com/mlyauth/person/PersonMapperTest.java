package com.mlyauth.person;

import com.google.common.collect.Sets;
import com.mlyauth.beans.PersonBean;
import com.mlyauth.constants.RoleCode;
import com.mlyauth.dao.PersonDAO;
import com.mlyauth.dao.RoleDAO;
import com.mlyauth.domain.Application;
import com.mlyauth.domain.AuthenticationInfo;
import com.mlyauth.domain.Person;
import com.mlyauth.domain.Role;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(DataProviderRunner.class)
public class PersonMapperTest {

    private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

    private Role role;

    private Person person;

    private PersonBean bean;

    @Mock
    private PersonDAO personDAO;

    @Mock
    private RoleDAO roleDAO;

    @InjectMocks
    private PersonMapper mapper;

    @Before
    public void setup() {
        initMocks(this);
        role = Role.newInstance().setCode(RoleCode.CLIENT);
        person = Person.newInstance().setRole(role);
        bean = PersonBean.newInstance();
        when(roleDAO.findOne(Mockito.any(RoleCode.class))).thenReturn(role);
    }


    @DataProvider
    public static Object[] properties() {
        // @formatter:off
        return new Object[][]{
                {1l, "1", "Ahmed", "EL IDRISSI", "1990-10-01", "ahmed.elidrissi.attach@gmail.com"},
                {2l, "2", "Moulay", "ATTACH", "1993-11-01", "mlyahmed1@gmail.com"},
                {3232l, "3232", "Fatima-Ezzahrae", "EL IDRISSI", "1997-12-01", "fatima.elidrissi@yahoo.fr"},
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

    @Test
    @UseDataProvider("properties")
    public void when_map_to_bean_then_map_properties(Object... properties) throws Exception{
        given_person(properties);
        when_map_person_to_bean();
        then_person_properties_are_mapped_to_bean(properties);
    }

    private void given_person(Object[] properties) throws ParseException {
        person.setId((Long) properties[0])
                .setExternalId(String.valueOf(properties[1]))
                .setFirstname(String.valueOf(properties[2]))
                .setLastname(String.valueOf(properties[3]))
                .setBirthdate(dateFormatter.parse(String.valueOf(properties[4])))
                .setEmail(String.valueOf(properties[5]));
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
        assertThat(bean.getExternalId(), equalTo(String.valueOf(properties[1])));
        assertThat(bean.getFirstname(), equalTo(String.valueOf(properties[2])));
        assertThat(bean.getLastname(), equalTo(String.valueOf(properties[3])));
        assertThat(bean.getBirthdate(), equalTo(String.valueOf(properties[4])));
        assertThat(bean.getEmail(), equalTo(String.valueOf(properties[5])));
    }

    @Test
    @UseDataProvider("passwords")
    public void when_map_to_bean_then_do_not_map_password(String password){
        person.setAuthenticationInfo(AuthenticationInfo.newInstance().setPassword(password));
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
    public void when_map_bean_to_person_then_map_properties(Object... properties) throws Exception{
        given_bean(properties);
        when_map_bean_to_person();
        then_bean_properties_are_mapped_to_person(properties);
    }

    @SuppressWarnings("Duplicates")
    private void then_bean_properties_are_mapped_to_person(Object... properties) throws ParseException {
        assertThat(person, notNullValue());
        assertThat(person.getId(), equalTo((Long) properties[0]));
        assertThat(person.getExternalId(), equalTo(String.valueOf(properties[1])));
        assertThat(person.getFirstname(), equalTo(String.valueOf(properties[2])));
        assertThat(person.getLastname(), equalTo(String.valueOf(properties[3])));
        assertThat(person.getBirthdate(), equalTo(dateFormatter.parse(String.valueOf(properties[4]))));
        assertThat(person.getEmail(), equalTo(String.valueOf(properties[5])));
    }

    private void when_map_bean_to_person() {
        person = mapper.toEntity(bean);
    }

    private void given_bean(Object... properties) {
        bean.setId((Long) properties[0])
                .setExternalId(String.valueOf(properties[1]))
                .setFirstname(String.valueOf(properties[2]))
                .setLastname(String.valueOf(properties[3]))
                .setBirthdate(String.valueOf(properties[4]))
                .setEmail(String.valueOf(properties[5]));
    }

}