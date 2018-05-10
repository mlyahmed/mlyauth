package com.mlyauth.security.sensitive.domain;

import org.junit.Before;
import org.junit.Test;

import java.sql.Types;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class TokenizedEmailTest {

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

}