package com.primasolutions.idp.security.sensitive.domain;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.Types;

import static com.primasolutions.idp.tools.RandomForTests.randomString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(DataProviderRunner.class)
public class TokenizedLoginTest {
    private static final String[] COLUMN_NAME = new String[]{randomString()};

    private TokenizedLogin tokenizedLogin;
    private MockPreparedStatement preparedStatement;
    private MockResultSet result;

    @Before
    public void setup() {
        tokenizedLogin = new TokenizedLogin();
        preparedStatement = new MockPreparedStatement();
        result = new MockResultSet();
    }

    @Test
    public void the_sql_type_must_be_VARCHAR() {
        assertThat(tokenizedLogin.sqlTypes(), equalTo(new int[] {Types.VARCHAR}));
    }

    @Test
    public void the_returned_class_must_be_string() {
        assertThat(tokenizedLogin.returnedClass(), equalTo(String.class));
    }

    @DataProvider
    public static Object[][] equalLogin() {
        // @formatter:off
        return new Object[][]{
                {"I3NS8K70O4", "I3NS8K70O4"},
                {"ahmed@elidrissi.fr", "ahmed@elidrissi.fr"},
                {"aei@prima-solutions.com", "aei@prima-solutions.com"},
                {"QAGUHXTJMM", "QAGUHXTJMM"},
                {null, null},
        };
        // @formatter:on
    }

    @Test
    @UseDataProvider("equalLogin")
    public void when_two_objects_are_the_same_string_then_are_equal(final String first, final String second) {
        assertThat(tokenizedLogin.equals(first, second), equalTo(true));
    }

    @DataProvider
    public static Object[][] unequalLogins() {
        // @formatter:off
        return new Object[][]{
                {"QAGUHXTJMM", "ahmed@elidrissi.fr"},
                {null, "ahmed@elidrissi.fr"},
                {randomString(), randomString()},
                {randomString(), randomString()},
                {randomString(), randomString()},
        };
        // @formatter:on
    }

    @Test
    @UseDataProvider("unequalLogins")
    public void when_two_objects_are_not_the_same_then_they_are_not_equal(final String first, final String second) {
        assertThat(tokenizedLogin.equals(first, second), equalTo(false));
    }


    @DataProvider
    public static Object[] logins() {
        // @formatter:off
        return new Object[]{
                randomString(),
                randomString(),
                randomString(),
                randomString(),
                randomString(),
        };
        // @formatter:on
    }

    @Test
    @UseDataProvider("logins")
    public void the_hash_code_is_the_same_the_object_in_param(final Object email) {
        assertThat(tokenizedLogin.hashCode(email), equalTo(email.hashCode()));
    }

    @Test
    public void when_the_object_is_null_then_its_hashcode_is_zero() {
        assertThat(tokenizedLogin.hashCode(null), equalTo(0));
    }
}
