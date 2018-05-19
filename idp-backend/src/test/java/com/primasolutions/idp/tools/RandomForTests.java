package com.primasolutions.idp.tools;

import com.primasolutions.idp.person.PersonBuilder;
import org.ajbrown.namemachine.NameGenerator;
import org.apache.commons.lang.RandomStringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public final class RandomForTests {

    private static final int MAX_LENGTH = 50;
    private static final long MAX_LONG = 10000000000000L;
    private static final int MIN_YEAR = 1950;
    private static final int MAX_YEAR = 1999;

    public static class NamePair {
        private final String firstName;
        private final String lastName;

        NamePair(final String firstName, final String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }
    }

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
        List<String> domains = Arrays.asList("amazon.com", "gmail.com", "yahoo.fr", "hotmail.fr");
        return randomString() + "@" + domains.get(new Random().nextInt(domains.size()));
    }

    public static String randomBirthdate() {
        Random random = new Random();
        final int minDay = (int) LocalDate.of(MIN_YEAR, 1, 1).toEpochDay();
        final int maxDay = (int) LocalDate.of(MAX_YEAR, 1, 1).toEpochDay();
        LocalDate randomBirthDate = LocalDate.ofEpochDay((long) (minDay + random.nextInt(maxDay - minDay)));
        return randomBirthDate.format(DateTimeFormatter.ofPattern(PersonBuilder.DATE_FORMAT));
    }

    public static NamePair randomName() {
        NameGenerator generator = new NameGenerator();
        List<org.ajbrown.namemachine.Name> name = generator.generateNames(1);
        return new NamePair(name.get(0).getFirstName(), name.get(0).getLastName());
    }
}
