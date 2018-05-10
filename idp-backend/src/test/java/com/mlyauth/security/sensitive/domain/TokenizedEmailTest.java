package com.mlyauth.security.sensitive.domain;

import com.mlyauth.tools.RandomForTests;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.Types;

import static com.mlyauth.tools.RandomForTests.randomFrenchEmail;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(DataProviderRunner.class)
public class TokenizedEmailTest {
    private static final String[] COLUMN_NAME = new String[]{RandomForTests.randomString()};

    private TokenizedEmail tokenizedEmail;

    @Before
    public void setup(){
        tokenizedEmail = new TokenizedEmail();
    }

    @Test
    public void the_sql_type_must_be_VARCHAR(){
        assertThat(tokenizedEmail.sqlTypes(), equalTo(new int[]{ Types.VARCHAR }));
    }

    @Test
    public void the_returned_class_must_be_string(){
        assertThat(tokenizedEmail.returnedClass(), equalTo(String.class));
    }


    @DataProvider
    public static Object[][] equalEmails() {
        // @formatter:off
        return new Object[][]{
                {"ahmed@elidrissi.ma", "ahmed@elidrissi.ma"},
                {"ahmed@elidrissi.attach.ma", "ahmed@elidrissi.attach.ma"},
                {"ahmed.elidrissi@prima-solutions.com", "ahmed.elidrissi@prima-solutions.com"},
                {"aei@prima-solutions.com", "aei@prima-solutions.com"},
                {null, null},
        };
        // @formatter:on
    }

    @Test
    @UseDataProvider("equalEmails")
    public void when_two_objects_are_the_same_string_then_are_equal(String first, String second){
        assertThat(tokenizedEmail.equals(first, second), equalTo(true));
    }

    @DataProvider
    public static Object[][] unequalEmails() {
        // @formatter:off
        return new Object[][]{
                {"ahmed@elidrissi.ma", "ahmed@elidrissi.fr"},
                {null, "ahmed@elidrissi.fr"},
                {randomFrenchEmail(), randomFrenchEmail()},
                {randomFrenchEmail(), randomFrenchEmail()},
                {randomFrenchEmail(), randomFrenchEmail()},
        };
        // @formatter:on
    }

    @Test
    @UseDataProvider("unequalEmails")
    public void when_two_objects_are_not_the_same_then_they_are_not_equal(String first, String second){
        assertThat(tokenizedEmail.equals(first, second), equalTo(false));
    }

    @DataProvider
    public static Object[] emails() {
        // @formatter:off
        return new Object[]{
                randomFrenchEmail(),
                randomFrenchEmail(),
                randomFrenchEmail(),
                randomFrenchEmail(),
        };
        // @formatter:on
    }

    @Test
    @UseDataProvider("emails")
    public void the_hash_code_is_the_same_the_object_in_param(Object email){
        assertThat(tokenizedEmail.hashCode(email), equalTo(email.hashCode()));
    }

    @Test
    public void when_the_object_is_null_then_its_hashcode_is_zero(){
        assertThat(tokenizedEmail.hashCode(null), equalTo(0));
    }

    @Test
    @UseDataProvider("emails")
    public void when_get_value_then_get_it_as_it_is(String email) throws Exception {
        MockResultSet result = new MockResultSet();
        result.setString(COLUMN_NAME[0], email);
        final Object expected = tokenizedEmail.nullSafeGet(result, COLUMN_NAME, null, null);
        assertThat(expected, equalTo(email));
    }
}