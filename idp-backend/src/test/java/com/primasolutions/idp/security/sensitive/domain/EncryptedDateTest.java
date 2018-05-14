package com.primasolutions.idp.security.sensitive.domain;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.hibernate4.encryptor.HibernatePBEEncryptorRegistry;
import org.jasypt.hibernate4.type.ParameterNaming;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.Security;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import static com.primasolutions.idp.tools.RandomForTests.randomString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

@RunWith(DataProviderRunner.class)
public class EncryptedDateTest {
    private static final String[] COLUMN_NAME = new String[]{randomString()};
    private static final String REGISTERED_NAME = "registerName";

    private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
    private StandardPBEStringEncryptor encryptor;
    private Properties typeParams;
    private EncryptedDate encryptedDate;
    private MockResultSet result;
    private MockPreparedStatement preparedStatement;

    @Before
    public void setup() {
        set_up_the_encryptor();
        set_up_the_encrypted_date_type();
        result = new MockResultSet();
        preparedStatement = new MockPreparedStatement();
    }

    @Test
    public void the_returned_class_type_must_be_date() {
        assertThat(encryptedDate.returnedClass(), equalTo(Date.class));
    }

    @DataProvider
    public static String[] dateAsString() {
        // @formatter:off
        return new String[]{
                "1984-09-18",
                "1995-10-01",
                "1979-12-13",
        };
        // @formatter:on
    }

    @Test
    @UseDataProvider("dateAsString")
    public void convert_the_object_to_date(final String value) throws ParseException {
        assertThat(encryptedDate.convertToObject(value), equalTo(dateFormatter.parse(value)));
    }

    @Test
    public void when_convert_a_null_to_object_then_return_null() {
        assertThat(encryptedDate.convertToObject(null), nullValue());
    }

    @DataProvider
    public static String[] badFormattedDates() {
        // @formatter:off
        return new String[]{
                randomString(),
                "199512OK",
                "989898",
        };
        // @formatter:on
    }

    @Test(expected = IllegalArgumentException.class)
    @UseDataProvider("badFormattedDates")
    public void when_conver_a_bad_formatted_date_to_object_then_error(final String value) {
        encryptedDate.convertToObject(value);
    }

    private void set_up_the_encryptor() {
        Security.addProvider(new BouncyCastleProvider());
        encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(randomString());
        encryptor.setAlgorithm("ss");
        encryptor.setProviderName(BouncyCastleProvider.PROVIDER_NAME);
        HibernatePBEEncryptorRegistry.getInstance().registerPBEStringEncryptor(REGISTERED_NAME, encryptor);
    }

    private void set_up_the_encrypted_date_type() {
        typeParams = new Properties();
        typeParams.setProperty(ParameterNaming.ENCRYPTOR_NAME, REGISTERED_NAME);
        encryptedDate = new EncryptedDate();
        encryptedDate.setParameterValues(typeParams);
    }


    private String unwrap(final String message) {
        return message.substring(0, message.length() - 1).replace("ENC(", "");
    }
}
