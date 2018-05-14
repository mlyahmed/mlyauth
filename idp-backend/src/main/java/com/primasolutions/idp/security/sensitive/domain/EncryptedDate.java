package com.primasolutions.idp.security.sensitive.domain;

import org.apache.commons.lang.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;

public class EncryptedDate extends AbstractEncryptedType {

    private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public Object nullSafeGet(final ResultSet rs, final String[] names,
                              final SessionImplementor session, final Object owner)
            throws HibernateException, SQLException {
        checkInitialization();
        final String v = rs.getString(names[0]);
        return rs.wasNull() ? null : convertToObject(!isWrapped(v) ? v : unNoise(encryptor.decrypt(unwrap(v))));
    }

    @Override
    public void nullSafeSet(final PreparedStatement st, final Object value,
                            final int index, final SessionImplementor session)
            throws HibernateException, SQLException {
        checkInitialization();
        if (value == null) {
            st.setNull(index, Types.VARCHAR);
        } else {
            st.setString(index, wrap(encryptor.encrypt(noise(convertToString(value)))));
        }
    }

    @Override
    protected Object convertToObject(final String string) {
        return StringUtils.isBlank(string) ? null : toDate(string);
    }

    private Date toDate(final String date) {
        try {
            return dateFormatter.parse(date);
        } catch (Exception e) {
            throw new IllegalArgumentException("Bad formatted date");
        }
    }

    @Override
    public Class returnedClass() {
        return Date.class;
    }

    private String noise(final String value) {
        return sha256Hex(UUID.randomUUID().toString()) + ":::" + value;
    }

    private String unNoise(final String noised) {
        final String[] parts = noised.split(":::");
        if (parts.length != 2) throw new IllegalArgumentException("Corrupted");
        return parts[1];
    }

}
