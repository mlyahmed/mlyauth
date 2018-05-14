package com.primasolutions.idp.security.sensitive.domain;

import org.apache.commons.lang.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EncryptedDate extends AbstractEncryptedType {

    private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public Object nullSafeGet(final ResultSet rs, final String[] names,
                              final SessionImplementor session, final Object owner)
            throws HibernateException, SQLException {
        return null;
    }

    @Override
    public void nullSafeSet(final PreparedStatement st, final Object value,
                            final int index, final SessionImplementor session)
            throws HibernateException, SQLException {

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



}
