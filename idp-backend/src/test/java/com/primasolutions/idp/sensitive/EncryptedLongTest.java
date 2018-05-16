package com.primasolutions.idp.sensitive;

import com.primasolutions.idp.sensitive.mocks.MockPreparedStatement;
import com.primasolutions.idp.sensitive.mocks.MockResultSet;
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
import java.util.Properties;
import java.util.UUID;

import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(DataProviderRunner.class)
public class EncryptedLongTest {
    private static final String[] COLUMN_NAME = new String[]{RandomForTests.randomString()};
    private static final String REGISTERED_NAME = "registerName";

    private StandardPBEStringEncryptor encryptor;
    private Properties typeParams;
    private EncryptedLong encryptedLong;
    private MockResultSet result;
    private MockPreparedStatement preparedStatement;

    @Before
    public void setup() {
        set_up_the_encryptor();
        set_up_the_encrypted_long_type();
        result = new MockResultSet();
        preparedStatement = new MockPreparedStatement();
    }

    @Test
    public void the_returned_class_type_must_be_long() {
        assertThat(encryptedLong.returnedClass(), equalTo(Long .class));
    }

    @DataProvider
    public static String[] longAsString() {
        // @formatter:off
        return new String[]{
                RandomForTests.randomLong().toString(),
                RandomForTests.randomLong().toString(),
                RandomForTests.randomLong().toString(),
                RandomForTests.randomLong().toString(),
        };
        // @formatter:on
    }

    @Test
    @UseDataProvider("longAsString")
    public void convert_the_object_to_long(final String value) {
        assertThat(encryptedLong.convertToObject(value), equalTo(new Long(value)));
    }

    @Test
    public void when_get_value_and_not_wrapped_then_return_it() throws Exception {
        result.setString(COLUMN_NAME[0], RandomForTests.randomLong().toString());
        final Object expected = encryptedLong.nullSafeGet(result, COLUMN_NAME, null, null);
        assertThat(expected, notNullValue());
        assertThat(expected.toString(), equalTo(result.getString(COLUMN_NAME[0])));
    }

    @Test
    @UseDataProvider("longAsString")
    @SuppressWarnings("Duplicates")
    public void when_get_value_and_wrapped_then_return_it_decrypted(final String value)  throws Exception {
        final String noisedValue = sha256Hex(UUID.randomUUID().toString()) + "::" + value;
        result.setString(COLUMN_NAME[0], "ENC(" + encryptor.encrypt(noisedValue) + ")");
        final Object expected = encryptedLong.nullSafeGet(result, COLUMN_NAME, null, null);
        assertThat(expected, notNullValue());
        assertThat(expected.toString(), equalTo(value));
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_get_value_and_wrapped_but_corrupted_then_error()  throws Exception {
        final String encrypted = "ENC(" + encryptor.encrypt(RandomForTests.randomLong().toString()) + ")";
        result.setString(COLUMN_NAME[0], encrypted);
        encryptedLong.nullSafeGet(result, COLUMN_NAME, null, null);
    }

    @Test
    @UseDataProvider("longAsString")
    public void when_set_value_then_encrypt_it_wrapped(final String value)  throws Exception {
        encryptedLong.nullSafeSet(preparedStatement, value, 0, null);
        assertThat(preparedStatement.getParam(0), notNullValue());
        assertThat(preparedStatement.getParam(0).toString(), Matchers.startsWith("ENC("));
        assertThat(preparedStatement.getParam(0).toString(), Matchers.endsWith(")"));

        final String decrypted = encryptor.decrypt(unwrap(preparedStatement.getParam(0).toString()));
        assertThat(decrypted.split("::"), arrayWithSize(2));
        assertThat(decrypted.split("::")[1], equalTo(value));
    }

    @Test
    public void when_set_null_value_then_set_varchar_type_as_null() throws Exception {
        encryptedLong.nullSafeSet(preparedStatement, null, 0, null);
        assertThat(preparedStatement.getNull(0), equalTo(Types.VARCHAR));
    }


    private void set_up_the_encryptor() {
        Security.addProvider(new BouncyCastleProvider());
        encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(RandomForTests.randomString());
        encryptor.setAlgorithm("PBEWithMD5AndDES");
        encryptor.setProviderName(BouncyCastleProvider.PROVIDER_NAME);
        HibernatePBEEncryptorRegistry.getInstance().registerPBEStringEncryptor(REGISTERED_NAME, encryptor);
    }

    private void set_up_the_encrypted_long_type() {
        typeParams = new Properties();
        typeParams.setProperty(ParameterNaming.ENCRYPTOR_NAME, REGISTERED_NAME);
        encryptedLong = new EncryptedLong();
        encryptedLong.setParameterValues(typeParams);
    }


    private String unwrap(final String message) {
        return message.substring(0, message.length() - 1).replace("ENC(", "");
    }
}
