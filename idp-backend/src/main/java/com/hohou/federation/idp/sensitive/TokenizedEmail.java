package com.hohou.federation.idp.sensitive;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Objects;

public final class TokenizedEmail implements UserType {

    private static final int SQL_TYPE = Types.VARCHAR;

    @Override
    public int[] sqlTypes() {
        return (new int[]{SQL_TYPE}).clone();
    }

    @Override
    public Class returnedClass() {
        return String.class;
    }

    @Override
    public boolean equals(final Object x, final Object y) throws HibernateException {
        return Objects.equals(x, y);
    }

    @Override
    public int hashCode(final Object x) throws HibernateException {
        return x == null ? 0 : x.hashCode();
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner)
            throws HibernateException, SQLException {
        return rs.getString(names[0]);
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session)
            throws HibernateException, SQLException {
        if (value == null) {
            st.setNull(index, SQL_TYPE);
        } else {
            st.setString(index, EmailTokenizer.newInstance().tokenizeEmailAddress(value.toString()));
        }
    }

    @Override
    public Object deepCopy(final Object value) throws HibernateException {
        return value;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(final Object value) throws HibernateException {
        return (value == null) ? null : (Serializable) deepCopy(value);
    }

    @Override
    public Object assemble(final Serializable cached, final Object owner) throws HibernateException {
        return (cached == null) ? null : deepCopy(cached);
    }

    @Override
    public Object replace(final Object original, final Object target, final Object owner) throws HibernateException {
        return original;
    }

}
