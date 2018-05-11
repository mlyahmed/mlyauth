package com.mlyauth.security.sensitive.domain;

import org.apache.commons.lang.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.jasypt.hibernate4.type.AbstractEncryptedAsStringType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class EncryptedString extends AbstractEncryptedAsStringType {

    public static final String WRAP_PREFIX = "ENC(";
    public static final String WRAP_SUFFIX = ")";

    @Override
    public Object nullSafeGet(final ResultSet rs, final String[] names,
                              final SessionImplementor session, final Object owner)
            throws HibernateException, SQLException {

        checkInitialization();
        final String value = rs.getString(names[0]);
        return rs.wasNull() ? null : convertToObject(!isWrapped(value) ? value : encryptor.decrypt(unwrap(value)));
    }

    @Override
    public void nullSafeSet(final PreparedStatement st, final Object value,
                            final int index, final SessionImplementor session)
            throws HibernateException, SQLException {
        checkInitialization();
        if (value == null) {
            st.setNull(index, Types.VARCHAR);
        } else {
            st.setString(index, wrap(encryptor.encrypt(convertToString(value))));
        }
    }

    @Override
    protected Object convertToObject(final String stringValue) {
        return stringValue;
    }

    @Override
    public Class returnedClass() {
        return String.class;
    }


    private boolean isWrapped(final String message) {
        return StringUtils.isNotBlank(message) && message.startsWith(WRAP_PREFIX) && message.endsWith(WRAP_SUFFIX);
    }

    private String unwrap(final String message) {
        return message.substring(0, message.length() - 1).replace(WRAP_PREFIX, "");
    }

    private String wrap(final String message) {
        return WRAP_PREFIX + message + WRAP_SUFFIX;
    }
}
