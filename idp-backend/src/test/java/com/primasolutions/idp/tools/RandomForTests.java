package com.primasolutions.idp.tools;

import com.primasolutions.idp.person.PersonBuilder;
import org.apache.commons.lang.RandomStringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public final class RandomForTests {

    private static final int MAX_LENGTH = 50;
    private static final long MAX_LONG = 10000000000000L;
    private static final int MIN_YEAR = 1950;
    private static final int MAX_YEAR = 1999;

    private RandomForTests() {
    }

    public static String randomString() {
        final int length = (new Random()).nextInt(MAX_LENGTH);
        return RandomStringUtils.random(length > 0 ? length : MAX_LENGTH, true, true);
    }


    public static Long randomLong() {
        return 1 + (long) (Math.random() * (MAX_LONG));
    }


    public static String randomFrenchEmail() {
        return randomString() + "@" + randomString() + ".fr";
    }

    public static String randomBirthdate() {
        Random random = new Random();
        final int minDay = (int) LocalDate.of(MIN_YEAR, 1, 1).toEpochDay();
        final int maxDay = (int) LocalDate.of(MAX_YEAR, 1, 1).toEpochDay();
        LocalDate randomBirthDate = LocalDate.ofEpochDay((long) (minDay + random.nextInt(maxDay - minDay)));
        return randomBirthDate.format(DateTimeFormatter.ofPattern(PersonBuilder.DATE_FORMAT));
    }

}
