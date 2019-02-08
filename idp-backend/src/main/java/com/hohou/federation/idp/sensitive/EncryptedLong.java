package com.hohou.federation.idp.sensitive;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.UUID;

import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;

public class EncryptedLong extends AbstractEncryptedType {

    @Override
    public Object nullSafeGet(final ResultSet rs, final String[] names,
                              final SharedSessionContractImplementor session, final Object owner)
            throws HibernateException, SQLException {
        return doNullSafeGet(rs, rs.getString(names[0]));
    }

    @Override
    public Object nullSafeGet(final ResultSet rs, final String[] names, final SessionImplementor session,
                              final Object owner) throws HibernateException, SQLException {
        return doNullSafeGet(rs, rs.getString(names[0]));
    }

    private Object doNullSafeGet(final ResultSet rs, final String string) throws SQLException {
        checkInitialization();
        final String v = string;
        return rs.wasNull() ? null : convertToObject(!isWrapped(v) ? v : unNoise(encryptor.decrypt(unwrap(v))));
    }

    @Override
    public void nullSafeSet(final PreparedStatement st, final Object value,
                            final int index, final SharedSessionContractImplementor session)
            throws HibernateException, SQLException {
        doNullSafeSet(st, value, index);
    }

    @Override
    public void nullSafeSet(final PreparedStatement st, final Object value, final int index,
                            final SessionImplementor session) throws HibernateException, SQLException {
        doNullSafeSet(st, value, index);
    }

    private void doNullSafeSet(final PreparedStatement st, final Object value, final int index) throws SQLException {
        checkInitialization();
        if (value == null) {
            st.setNull(index, Types.VARCHAR);
        } else {
            st.setString(index, wrap(encryptor.encrypt(noise(convertToString(value)))));
        }
    }

    @Override
    protected Object convertToObject(final String string) {
        return new Long(string);
    }

    @Override
    public Class returnedClass() {
        return Long.class;
    }

    private String noise(final String value) {
        return sha256Hex(UUID.randomUUID().toString()) + "::" + value;
    }

    private String unNoise(final String noised) {
        final String[] parts = noised.split("::");
        if (parts.length != 2) throw new IllegalArgumentException("Corrupted");
        return parts[1];
    }
}
