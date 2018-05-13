package com.primasolutions.idp.security.sensitive.domain;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class TokenizedLogin implements UserType {
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
    public final boolean equals(final Object x, final Object y) throws HibernateException {
        return x == y || (x != null && y != null && x.equals(y));
    }

    @Override
    public final int hashCode(final Object x) throws HibernateException {
        return x == null ? 0 : x.hashCode();
    }

    @Override
    public Object nullSafeGet(final ResultSet rs, final String[] names, final SessionImplementor session,
                              final Object owner) throws HibernateException, SQLException {
        return null;
    }

    @Override
    public void nullSafeSet(final PreparedStatement st, final Object value, final int index,
                            final SessionImplementor session) throws HibernateException, SQLException {

    }

    @Override
    public Object deepCopy(final Object value) throws HibernateException {
        return null;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(final Object value) throws HibernateException {
        return null;
    }

    @Override
    public Object assemble(final Serializable cached, final Object owner) throws HibernateException {
        return null;
    }

    @Override
    public Object replace(final Object original, final Object target, final Object owner) throws HibernateException {
        return null;
    }
}
