package com.mlyauth.security.sensitive.domain;

import org.apache.commons.lang3.StringUtils;
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
    static final int[] sqlTypes = new int[]{ sqlType };

    @Override
    public int[] sqlTypes() {
        return sqlTypes.clone();
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
        return x.hashCode();
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner) throws HibernateException, SQLException {
        return rs.getString(names[0]);
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session) throws HibernateException, SQLException {
        if (value == null) {
            st.setNull(index, Types.VARCHAR);
        } else {
            final String[] parts = value.toString().split("@");
            final int length = parts[0].length();
            final String plain = parts[0].substring(0, parts[0].length() / 2);
            String token = plain + StringUtils.repeat("*", length - plain.length()) + "@" + parts[1];
            st.setString(index, token);
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
