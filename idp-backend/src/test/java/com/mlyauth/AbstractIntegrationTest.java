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
        System.setProperty("startup.passphrase", "UD`jS47)Gf976wT+>75TA'cQ,65Bjh(L");
    }

    public final static String MASTER_EMAIL = "ahmed.elidrissi.attach@gmail.com";
    public final static String MASTER_PASSWORD = "root";

    @ClassRule
    public static final SpringClassRule springClassRule = new SpringClassRule();


    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

}
