package com.primasolutions.idp.sensitive;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class EncryptedString extends AbstractEncryptedType {

    @Override
    public Object nullSafeGet(final ResultSet rs, final String[] names,
                              final SharedSessionContractImplementor session,
                              final Object owner) throws HibernateException, SQLException {
        return doNullSafeGet(rs, rs.getString(names[0]));
    }

    @Override
    public Object nullSafeGet(final ResultSet rs, final String[] names, final SessionImplementor session,
                              final Object owner) throws HibernateException, SQLException {
        return doNullSafeGet(rs, rs.getString(names[0]));
    }

    private Object doNullSafeGet(final ResultSet rs, final String string) throws SQLException {
        checkInitialization();
        final String value = string;
        return rs.wasNull() ? null : convertToObject(!isWrapped(value) ? value : encryptor.decrypt(unwrap(value)));
    }

    @Override
    public void nullSafeSet(final PreparedStatement st, final Object value, final int index,
                            final SharedSessionContractImplementor session) throws HibernateException, SQLException {
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

}
