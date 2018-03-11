package com.mlyauth;

import org.h2.engine.Mode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class TestConfig {

    @Value("${spring.datasource.driver-class-name}")
    private String driverClass;

    @PostConstruct
    public void tweakH2CompatibilityMode() {
        if ("org.h2.Driver".equals(this.driverClass)) {
            Mode mode = Mode.getInstance("MySQL");
            mode.convertInsertNullToZero = false;
        }
    }


}
