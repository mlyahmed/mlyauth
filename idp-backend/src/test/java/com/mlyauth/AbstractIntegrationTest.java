package com.mlyauth;

import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.transaction.annotation.Transactional;

@RunWith(DataProviderRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class AbstractIntegrationTest {

    static {
        System.setProperty("startup.passphrase", "cM7g+:S*DY7m>c.D3{8jHtr6tH%^L~3t");
    }

    public final static String MASTER_EMAIL = "ahmed.elidrissi.attach@gmail.com";
    public final static String MASTER_PASSWORD = "root";
    public final static String MASTER_EXTERNAL_ID = "gestF";

    @ClassRule
    public static final SpringClassRule springClassRule = new SpringClassRule();


    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

}
