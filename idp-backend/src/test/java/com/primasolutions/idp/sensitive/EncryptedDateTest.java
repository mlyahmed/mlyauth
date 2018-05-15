package com.primasolutions.idp.sensitive;

import com.primasolutions.idp.tools.RandomForTests;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.hamcrest.Matchers;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.hibernate4.encryptor.HibernatePBEEncryptorRegistry;
import org.jasypt.hibernate4.type.ParameterNaming;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.Security;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;

import static com.primasolutions.idp.tools.RandomForTests.randomString;
import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
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

    @Test
    @UseDataProvider("dateAsString")
    public void when_get_value_and_not_wrapped_then_return_it(final String date) throws Exception {
        result.setString(COLUMN_NAME[0], date);
        final Object expected = encryptedDate.nullSafeGet(result, COLUMN_NAME, null, null);
        assertThat(expected, notNullValue());
        assertThat(dateFormatter.format(expected), equalTo(result.getString(COLUMN_NAME[0])));
    }

    @Test
    @UseDataProvider("dateAsString")
    @SuppressWarnings("Duplicates")
    public void when_get_value_and_wrapped_then_return_it_decrypted(final String value)  throws Exception {
        final String noisedValue = sha256Hex(UUID.randomUUID().toString()) + ":::" + value;
        result.setString(COLUMN_NAME[0], "ENC(" + encryptor.encrypt(noisedValue) + ")");
        final Object expected = encryptedDate.nullSafeGet(result, COLUMN_NAME, null, null);
        assertThat(expected, notNullValue());
        assertThat(expected, equalTo(dateFormatter.parse(value)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_get_value_and_wrapped_but_corrupted_then_error()  throws Exception {
        final String encrypted = "ENC(" + encryptor.encrypt(RandomForTests.randomLong().toString()) + ")";
        result.setString(COLUMN_NAME[0], encrypted);
        encryptedDate.nullSafeGet(result, COLUMN_NAME, null, null);
    }

    @Test
    @UseDataProvider("dateAsString")
    public void when_set_value_then_encrypt_it_wrapped(final String value)  throws Exception {
        encryptedDate.nullSafeSet(preparedStatement, value, 0, null);
        assertThat(preparedStatement.getParam(0), notNullValue());
        assertThat(preparedStatement.getParam(0).toString(), Matchers.startsWith("ENC("));
        assertThat(preparedStatement.getParam(0).toString(), Matchers.endsWith(")"));

        final String decrypted = encryptor.decrypt(unwrap(preparedStatement.getParam(0).toString()));
        assertThat(decrypted.split(":::"), arrayWithSize(2));
        assertThat(decrypted.split(":::")[1], equalTo(value));
    }

    @Test
    public void when_set_null_value_then_set_varchar_type_as_null() throws Exception {
        encryptedDate.nullSafeSet(preparedStatement, null, 0, null);
        assertThat(preparedStatement.getNull(0), equalTo(Types.VARCHAR));
    }

    private void set_up_the_encryptor() {
        Security.addProvider(new BouncyCastleProvider());
        encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(randomString());
        encryptor.setAlgorithm("PBEWithSHAAnd2-KeyTripleDES-CBC");
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
