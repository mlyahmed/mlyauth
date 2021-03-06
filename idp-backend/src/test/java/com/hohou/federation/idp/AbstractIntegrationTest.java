package com.hohou.federation.idp;

import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.transaction.annotation.Transactional;

@RunWith(DataProviderRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@ContextConfiguration(initializers = { TestConfig.Initializer.class })
@Import(TestConfig.class)
public abstract class AbstractIntegrationTest {

    public static final String MASTER_EMAIL = "ahmed.elidrissi.attach@gmail.com";
    protected static final String MASTER_PASSWORD = "root";

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();


    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

}
