package com.mlyauth.context;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class ContextIdGeneratorTest {

    public static final int ID_LENGTH = 32;
    public static final int BIG_NUMBER = 20000;
    private ContextIdGenerator generator;

    @Before
    public void setup() {

        generator = new ContextIdGenerator();
    }

    @Test
    public void when_generate_an_id_then_generate_it() {
        final String id = generator.generateId();
        assertThat(id, notNullValue());
        assertThat(id.length(), equalTo(ID_LENGTH));
    }


    @Test
    public void the_generated_id_must_be_unique() {
        List<String> previousIds = new ArrayList<>(BIG_NUMBER);
        IntStream.range(0, BIG_NUMBER).forEach(i -> previousIds.add(generator.generateId()));
        assertThat(previousIds, Matchers.not(Matchers.contains(generator.generateId())));
    }
}
