package com.mlyauth.utests.security.context;

import com.mlyauth.security.context.ContextIdGenerator;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class ContextIdGeneratorTest {

    private ContextIdGenerator generator;

    @Before
    public void setup() {

        generator = new ContextIdGenerator();
    }

    @Test
    public void when_generate_an_id_then_generate_it() {
        final String id = generator.generateId();
        assertThat(id, notNullValue());
        assertThat(id.length(), equalTo(32));
    }


    @Test
    public void the_generated_id_must_be_unique() {
        List<String> previousIds = new ArrayList<>(20000);
        IntStream.range(0, 10000).forEach(i -> previousIds.add(generator.generateId()));
        assertThat(previousIds, not(contains(generator.generateId())));
    }
}
