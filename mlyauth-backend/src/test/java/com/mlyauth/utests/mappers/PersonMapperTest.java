package com.mlyauth.utests.mappers;

import com.google.common.collect.Sets;
import com.mlyauth.beans.PersonBean;
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

import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@RunWith(DataProviderRunner.class)
public class PersonMapperTest {

    private PersonMapper mapper;
    private Person person;

    @Before
    public void setup(){
        mapper = new PersonMapper();
        person = new Person();
    }

    @Test
    public void when_map_to_bean_and_null_then_return_null(){
        final PersonBean bean = mapper.toBean(null);
        assertThat(bean, Matchers.nullValue());
    }


    @DataProvider
    public static Object[] properties() {
        // @formatter:off
        return new Object[][] {
                {1, "Ahmed", "EL IDRISSI", "ahmed.elidrissi.attach@gmail.com"},
                {2, "Moulay", "ATTACH", "mlyahmed1@gmail.com"},
                {3232, "Fatima-Ezzahrae", "EL IDRISSI", "fatima.elidrissi@yahoo.fr"},
        };
        // @formatter:on
    }

    @Test
    @UseDataProvider("properties")
    public void when_map_to_bean_then_map_properties(long id, String firstname, String lastname, String email){
        person.setId(id);
        person.setFirstname(firstname);
        person.setLastname(lastname);
        person.setEmail(email);
        final PersonBean bean = mapper.toBean(person);
        assertThat(bean, notNullValue());
        assertThat(bean.getId(), equalTo(id));
        assertThat(bean.getFirstname(), equalTo(firstname));
        assertThat(bean.getLastname(), equalTo(lastname));
        assertThat(bean.getEmail(), equalTo(email));
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

    @Test
    @UseDataProvider("passwords")
    public void when_map_to_bean_then_do_not_map_password(String password){
        person.setPassword(password);
        final PersonBean bean = mapper.toBean(person);
        assertThat(bean, notNullValue());
        assertThat(bean.getPassword(), nullValue());
    }

    @Test
    public void when_map_to_bean_and_applications_is_null_then_map_applications_to_empty(){
        person.setApplications(null);
        final PersonBean bean = mapper.toBean(person);
        assertThat(bean, notNullValue());
        assertThat(bean.getApplications(), equalTo(Collections.emptySet()));
    }

    @Test
    public void when_map_to_bean_and_one_application_then_map_application_to_code() {
        person.setApplications(Sets.newHashSet(Application.newInstance().setAppname("Policy")));
        final PersonBean bean = mapper.toBean(person);
        assertThat(bean, notNullValue());
        assertThat(bean.getApplications(), equalTo(Sets.newHashSet("Policy")));
    }

    @Test
    public void when_map_to_bean_and_application_code_then_map_application_to_empty() {
        person.setApplications(Sets.newHashSet(Application.newInstance().setAppname(null)));
        final PersonBean bean = mapper.toBean(person);
        assertThat(bean, notNullValue());
        assertThat(bean.getApplications(), equalTo(Sets.newHashSet()));
    }

    @Test
    public void when_map_to_bean_then_map_applications_to_codes() {
        final Application app1 = Application.newInstance().setAppname("Policy");
        final Application app2 = Application.newInstance().setAppname("Claims");
        person.setApplications(Sets.newHashSet(app1, app2));
        final PersonBean bean = mapper.toBean(person);
        assertThat(bean, notNullValue());
        assertThat(bean.getApplications(), equalTo(Sets.newHashSet("Policy", "Claims")));
    }
}