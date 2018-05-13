package com.primasolutions.idp.security.sensitive.domain;

import com.primasolutions.idp.tools.RandomForTests;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.Types;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(DataProviderRunner.class)
public class TokenizedLoginTest {
    private static final String[] COLUMN_NAME = new String[]{RandomForTests.randomString()};

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
}
