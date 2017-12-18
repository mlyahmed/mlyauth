package com.mlyauth.mappers;

import com.mlyauth.beans.PersonBean;
import com.mlyauth.domain.Person;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
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

}