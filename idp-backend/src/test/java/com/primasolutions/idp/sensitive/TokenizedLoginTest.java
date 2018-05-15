package com.primasolutions.idp.sensitive;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.Types;

import static com.primasolutions.idp.tools.RandomForTests.randomString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
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
    public void the_hash_code_is_the_same_the_object_in_param(final Object login) {
        assertThat(tokenizedLogin.hashCode(login), equalTo(login.hashCode()));
    }

    @Test
    public void when_the_object_is_null_then_its_hashcode_is_zero() {
        assertThat(tokenizedLogin.hashCode(null), equalTo(0));
    }

    @Test
    @UseDataProvider("logins")
    public void when_get_value_then_get_it_as_it_is(final String login) throws Exception {
        result.setString(COLUMN_NAME[0], login);
        final Object expected = tokenizedLogin.nullSafeGet(result, COLUMN_NAME, null, null);
        assertThat(expected, equalTo(login));
    }


    @DataProvider
    public static Object[][] loginAndTokenized() {
        // @formatter:off
        return new Object[][]{
                {"ahmed@elidrissi.ma", "a****@elidrissi.ma"},
                {"aei@prima-solutions.com", "a**@prima-solutions.com"},
                {"I7LGCQI9ZI", "I**G*****I"},
                {"RLN7OPVL1D", "R**7*****D"},
                {"RLN7OPVL1D", "R**7*****D"},
                {"RLN7OPVL1DXBYTS", "R****P********S"},
                {"RLN7OPVL1DXBYTS", "R****P********S"},
        };
        // @formatter:on
    }

    @Test
    @UseDataProvider("loginAndTokenized")
    public void when_set_value_then_tokenize_it(final String login, final String tokenized) throws Exception {
        tokenizedLogin.nullSafeSet(preparedStatement, login, 0, null);
        assertThat(preparedStatement.getParam(0).toString(), equalTo(tokenized));
    }

    @Test
    public void when_set_null_value_then_set_varchar_type_as_null() throws Exception {
        tokenizedLogin.nullSafeSet(preparedStatement, null, 0, null);
        assertThat(preparedStatement.getNull(0), equalTo(Types.VARCHAR));
    }

    @Test
    @UseDataProvider("logins")
    public void when_deep_copy_and_login_then_return_the_same_object(final String login) {
        assertThat(tokenizedLogin.deepCopy(login), sameInstance(login));
    }

    @Test
    public void the_tokenized_login_type_is_immutable() {
        assertThat(tokenizedLogin.isMutable(), equalTo(false));
    }

    @Test
    @UseDataProvider("logins")
    public void when_disassemble_an_login_return_the_same_value(final String login) {
        assertThat(tokenizedLogin.disassemble(login), equalTo(login));
    }

    @Test
    public void when_disassemble_a_null_value_then_return_null() {
        assertThat(tokenizedLogin.disassemble(null), nullValue());
    }

    @Test
    @UseDataProvider("logins")
    public void when_assemble_from_a_cached_login_then_return_the_cached_value(final String login) {
        assertThat(tokenizedLogin.assemble(login, null), equalTo(login));
    }

    @Test
    public void when_assemble_from_a_null_value_then_return_null() {
        assertThat(tokenizedLogin.assemble(null, null), nullValue());
    }

    @Test
    @UseDataProvider("logins")
    public void when_replace_then_return_the_origin_value(final String login) {
        assertThat(tokenizedLogin.replace(login, null, null), equalTo(login));
    }
}
