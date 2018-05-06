package com.mlyauth.security.sensitive.domain;

import com.mlyauth.tools.RandomForTests;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.hamcrest.Matchers;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.hibernate4.encryptor.HibernatePBEEncryptorRegistry;
import org.jasypt.hibernate4.type.ParameterNaming;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.security.Security;
import java.sql.ResultSet;
import java.util.Properties;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class EncryptedStringTest {

    public static final String REGISTERED_NAME = "registerName";
    private StandardPBEStringEncryptor encryptor;
    private EncryptedString encryptedString;
    private Properties typeParams;

    @Before
    public void setup(){
        set_up_the_encryptor();
        set_up_the_encrypted_type();
    }

    private void set_up_the_encryptor() {
        Security.addProvider(new BouncyCastleProvider());
        encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(RandomForTests.randomString());
        encryptor.setAlgorithm("PBEWITHSHA256AND128BITAES-CBC-BC");
        encryptor.setProviderName(BouncyCastleProvider.PROVIDER_NAME);
        HibernatePBEEncryptorRegistry.getInstance().registerPBEStringEncryptor(REGISTERED_NAME, encryptor);
    }

    private void set_up_the_encrypted_type() {
        typeParams = new Properties();
        typeParams.setProperty(ParameterNaming.ENCRYPTOR_NAME, REGISTERED_NAME);
        encryptedString = new EncryptedString();
        encryptedString.setParameterValues(typeParams);
    }

    @Test
    public void when_get_a_message_and_not_wrapped_then_return_it() throws Exception{
        ResultSet result = Mockito.mock(ResultSet.class);
        String[] names = {RandomForTests.randomString()};
        final String message = RandomForTests.randomString();
        Mockito.when(result.getString(names[0])).thenReturn(message);
        final Object expected = encryptedString.nullSafeGet(result, names, null, null);
        assertThat(expected, Matchers.equalTo(message));
    }

    @Test
    public void when_get_a_message_and_wrapped_then_return_it_decrypted()  throws Exception {
        ResultSet result = Mockito.mock(ResultSet.class);
        String[] names = {RandomForTests.randomString()};
        final String message = RandomForTests.randomString();
        final String encrypted = "ENC(" + encryptor.encrypt(message) + ")";
        Mockito.when(result.getString(names[0])).thenReturn(encrypted);
        final Object expected = encryptedString.nullSafeGet(result, names, null, null);
        assertThat(expected, Matchers.equalTo(message));
    }

    @Test
    public void when_set_message_then_encrypt_it_wrapped()  throws Exception {
        MockPreparedStatement statement = new MockPreparedStatement();
        String value = RandomForTests.randomString();
        encryptedString.nullSafeSet(statement, value, 0, null);

        assertThat(statement.getParam(0), notNullValue());
        assertThat(statement.getParam(0).toString(), Matchers.startsWith("ENC("));
        assertThat(statement.getParam(0).toString(), Matchers.endsWith(")"));
        assertThat(encryptor.decrypt(unwrap(statement.getParam(0).toString())), equalTo(value));
    }


    private String unwrap(String message){
        return message.substring(0, message.length() -1).replace("ENC(", "");
    }
}