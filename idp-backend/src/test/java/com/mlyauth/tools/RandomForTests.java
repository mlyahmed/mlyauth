package com.mlyauth.tools;

import org.apache.commons.lang.RandomStringUtils;

import java.util.Random;

public class RandomForTests {

    public static String randomString() {
        final int length = (new Random()).nextInt(50);
        return RandomStringUtils.random(length > 0 ? length : 50, true, true);
    }


    public static String randomFrenchEmail() {
        return randomString() + "@" + randomString() + ".fr";
    }

}
