package com.mlyauth.tools;

import org.apache.commons.lang.RandomStringUtils;

import java.util.Random;

public final class RandomForTests {

    public static final int MAX_LENGTH = 50;

    private RandomForTests() {
    }

    public static String randomString() {
        final int length = (new Random()).nextInt(MAX_LENGTH);
        return RandomStringUtils.random(length > 0 ? length : MAX_LENGTH, true, true);
    }


    public static String randomFrenchEmail() {
        return randomString() + "@" + randomString() + ".fr";
    }

}
