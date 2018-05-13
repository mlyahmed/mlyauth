package com.primasolutions.idp.security.sensitive.domain;

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

import static com.primasolutions.idp.tools.RandomForTests.randomString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(DataProviderRunner.class)
public class EncryptedStringTest {
    private static final String[] COLUMN_NAME = new String[]{RandomForTests.randomString()};
    private static final String REGISTERED_NAME = "registerName";

    private StandardPBEStringEncryptor encryptor;
    private Properties typeParams;
    private EncryptedString encryptedString;
    private MockResultSet result;
    private MockPreparedStatement preparedStatement;
    private String plainValue;

    @Before
    public void setup() {
        set_up_the_encryptor();
        set_up_the_encrypted_string_type();
        result = new MockResultSet();
        preparedStatement = new MockPreparedStatement();
        plainValue = RandomForTests.randomString();
    }

    @Test
    public void the_returned_class_type_must_be_string() {
        assertThat(encryptedString.returnedClass(), equalTo(String.class));
    }

    @DataProvider
    public static String[] strings() {
        // @formatter:off
        return new String[]{
                "ahmed.elidrissi.attach@gmail.com",
                randomString(),
                randomString(),
        };
        // @formatter:on
    }

    @Test
    @UseDataProvider("strings")
    public void keep_the_same_instance_when_converted_to_object(final String value) {
        assertThat(encryptedString.convertToObject(value), Matchers.sameInstance(value));
    }

    @Test
    public void when_get_a_message_and_not_wrapped_then_return_it() throws Exception {
        result.setString(COLUMN_NAME[0], RandomForTests.randomString());
        final Object expected = encryptedString.nullSafeGet(result, COLUMN_NAME, null, null);
        assertThat(expected, equalTo(result.getString(COLUMN_NAME[0])));
    }

    @Test
    public void when_get_a_message_and_wrapped_then_return_it_decrypted()  throws Exception {
        final String encrypted = "ENC(" + encryptor.encrypt(plainValue) + ")";
        result.setString(COLUMN_NAME[0], encrypted);
        final Object expected = encryptedString.nullSafeGet(result, COLUMN_NAME, null, null);
        assertThat(expected, equalTo(plainValue));
    }

    @Test
    public void when_set_message_then_encrypt_it_wrapped()  throws Exception {
        encryptedString.nullSafeSet(preparedStatement, plainValue, 0, null);
        assertThat(preparedStatement.getParam(0), notNullValue());
        assertThat(preparedStatement.getParam(0).toString(), Matchers.startsWith("ENC("));
        assertThat(preparedStatement.getParam(0).toString(), Matchers.endsWith(")"));
        assertThat(encryptor.decrypt(unwrap(preparedStatement.getParam(0).toString())), equalTo(plainValue));
    }

    @Test
    public void when_set_null_value_then_set_varchar_type_as_null() throws Exception {
        encryptedString.nullSafeSet(preparedStatement, null, 0, null);
        assertThat(preparedStatement.getNull(0), equalTo(Types.VARCHAR));
    }

    private void set_up_the_encryptor() {
        Security.addProvider(new BouncyCastleProvider());
        encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(RandomForTests.randomString());
        encryptor.setAlgorithm("PBEWITHSHA256AND128BITAES-CBC-BC");
        encryptor.setProviderName(BouncyCastleProvider.PROVIDER_NAME);
        HibernatePBEEncryptorRegistry.getInstance().registerPBEStringEncryptor(REGISTERED_NAME, encryptor);
    }

    private void set_up_the_encrypted_string_type() {
        typeParams = new Properties();
        typeParams.setProperty(ParameterNaming.ENCRYPTOR_NAME, REGISTERED_NAME);
        encryptedString = new EncryptedString();
        encryptedString.setParameterValues(typeParams);
    }


    private String unwrap(final String message) {
        return message.substring(0, message.length() - 1).replace("ENC(", "");
    }
}
