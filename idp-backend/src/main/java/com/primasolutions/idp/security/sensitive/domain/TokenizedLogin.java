package com.primasolutions.idp.security.sensitive.domain;

import org.apache.commons.validator.routines.EmailValidator;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public final class TokenizedLogin implements UserType {
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
        return x == y || (x != null && x.equals(y));
    }

    @Override
    public int hashCode(final Object x) throws HibernateException {
        return x == null ? 0 : x.hashCode();
    }

    @Override
    public Object nullSafeGet(final ResultSet rs, final String[] names, final SessionImplementor session,
                              final Object owner) throws HibernateException, SQLException {
        return rs.getString(names[0]);
    }

    @Override
    public void nullSafeSet(final PreparedStatement st, final Object value, final int index,
                            final SessionImplementor session) throws HibernateException, SQLException {
        if (value == null) {
            st.setNull(index, SQL_TYPE);
        } else {
            final boolean isEmail = EmailValidator.getInstance().isValid(value.toString());
            if (isEmail) {
                st.setString(index, EmailTokenizer.newInstance().tokenizeEmailAddress(value.toString()));
            } else {
                st.setString(index, StringTokenizer.newInstance().tokenize(value.toString()));
            }
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
