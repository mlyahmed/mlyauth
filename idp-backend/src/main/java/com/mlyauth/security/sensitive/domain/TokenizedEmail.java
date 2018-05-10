package com.mlyauth.security.sensitive.domain;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class TokenizedEmail implements UserType {

    static final int sqlType = Types.VARCHAR;

    @Override
    public int[] sqlTypes() {
        return (new int[]{ sqlType }).clone();
    }

    @Override
    public Class returnedClass() {
        return String.class;
    }

    @Override
    public final boolean equals(final Object x, final Object y) throws HibernateException {
        return x == y || ( x != null && y != null && x.equals( y ) );
    }

    @Override
    public final int hashCode(final Object x) throws HibernateException {
        return x == null ? 0 : x.hashCode();
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner) throws HibernateException, SQLException {
        return rs.getString(names[0]);
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session) throws HibernateException, SQLException {
        if (value == null) {
            st.setNull(index, sqlType);
        } else {
            st.setString(index, EmailTokenizer.newInstance().tokenizeEmailAddress(value.toString()));
        }
    }

    @Override
    public Object deepCopy(Object value) throws HibernateException {
        return value;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public final Serializable disassemble(final Object value) throws HibernateException {
        return (value == null) ? null : (Serializable) deepCopy(value);
    }

    @Override
    public final Object assemble(final Serializable cached, final Object owner) throws HibernateException {
        return (cached == null) ? null : deepCopy(cached);
    }

    @Override
    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return original;
    }

}
