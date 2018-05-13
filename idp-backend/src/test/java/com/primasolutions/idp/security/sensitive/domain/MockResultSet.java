package com.primasolutions.idp.security.sensitive.domain;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MockResultSet implements ResultSet {

    private HashMap<String, Object> columns = new HashMap<>();

    public void setString(final String columnName, final String value) {
        columns.put(columnName, value);
    }

    @Override
    public boolean next() throws SQLException {
        return false;
    }

    @Override
    public void close() throws SQLException {

    }

    @Override
    public boolean wasNull() throws SQLException {
        return false;
    }

    @Override
    public String getString(final int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public boolean getBoolean(final int columnIndex) throws SQLException {
        return false;
    }

    @Override
    public byte getByte(final int columnIndex) throws SQLException {
        return 0;
    }

    @Override
    public short getShort(final int columnIndex) throws SQLException {
        return 0;
    }

    @Override
    public int getInt(final int columnIndex) throws SQLException {
        return 0;
    }

    @Override
    public long getLong(final int columnIndex) throws SQLException {
        return 0;
    }

    @Override
    public float getFloat(final int columnIndex) throws SQLException {
        return 0;
    }

    @Override
    public double getDouble(final int columnIndex) throws SQLException {
        return 0;
    }

    @Override
    public BigDecimal getBigDecimal(final int columnIndex, final int scale) throws SQLException {
        return null;
    }

    @Override
    public byte[] getBytes(final int columnIndex) throws SQLException {
        return new byte[0];
    }

    @Override
    public Date getDate(final int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Time getTime(final int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Timestamp getTimestamp(final int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public InputStream getAsciiStream(final int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public InputStream getUnicodeStream(final int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public InputStream getBinaryStream(final int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public String getString(final String label) throws SQLException {
        return (String) columns.get(label);
    }

    @Override
    public boolean getBoolean(final String label) throws SQLException {
        return false;
    }

    @Override
    public byte getByte(final String label) throws SQLException {
        return 0;
    }

    @Override
    public short getShort(final String label) throws SQLException {
        return 0;
    }

    @Override
    public int getInt(final String label) throws SQLException {
        return 0;
    }

    @Override
    public long getLong(final String label) throws SQLException {
        return 0;
    }

    @Override
    public float getFloat(final String label) throws SQLException {
        return 0;
    }

    @Override
    public double getDouble(final String label) throws SQLException {
        return 0;
    }

    @Override
    public BigDecimal getBigDecimal(final String label, final int scale) throws SQLException {
        return null;
    }

    @Override
    public byte[] getBytes(final String label) throws SQLException {
        return new byte[0];
    }

    @Override
    public Date getDate(final String label) throws SQLException {
        return null;
    }

    @Override
    public Time getTime(final String label) throws SQLException {
        return null;
    }

    @Override
    public Timestamp getTimestamp(final String label) throws SQLException {
        return null;
    }

    @Override
    public InputStream getAsciiStream(final String label) throws SQLException {
        return null;
    }

    @Override
    public InputStream getUnicodeStream(final String label) throws SQLException {
        return null;
    }

    @Override
    public InputStream getBinaryStream(final String label) throws SQLException {
        return null;
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return null;
    }

    @Override
    public void clearWarnings() throws SQLException {

    }

    @Override
    public String getCursorName() throws SQLException {
        return null;
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return null;
    }

    @Override
    public Object getObject(final int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Object getObject(final String label) throws SQLException {
        return null;
    }

    @Override
    public int findColumn(final String label) throws SQLException {
        return 0;
    }

    @Override
    public Reader getCharacterStream(final int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Reader getCharacterStream(final String label) throws SQLException {
        return null;
    }

    @Override
    public BigDecimal getBigDecimal(final int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public BigDecimal getBigDecimal(final String label) throws SQLException {
        return null;
    }

    @Override
    public boolean isBeforeFirst() throws SQLException {
        return false;
    }

    @Override
    public boolean isAfterLast() throws SQLException {
        return false;
    }

    @Override
    public boolean isFirst() throws SQLException {
        return false;
    }

    @Override
    public boolean isLast() throws SQLException {
        return false;
    }

    @Override
    public void beforeFirst() throws SQLException {

    }

    @Override
    public void afterLast() throws SQLException {

    }

    @Override
    public boolean first() throws SQLException {
        return false;
    }

    @Override
    public boolean last() throws SQLException {
        return false;
    }

    @Override
    public int getRow() throws SQLException {
        return 0;
    }

    @Override
    public boolean absolute(final int row) throws SQLException {
        return false;
    }

    @Override
    public boolean relative(final int rows) throws SQLException {
        return false;
    }

    @Override
    public boolean previous() throws SQLException {
        return false;
    }

    @Override
    public void setFetchDirection(final int direction) throws SQLException {

    }

    @Override
    public int getFetchDirection() throws SQLException {
        return 0;
    }

    @Override
    public void setFetchSize(final int rows) throws SQLException {

    }

    @Override
    public int getFetchSize() throws SQLException {
        return 0;
    }

    @Override
    public int getType() throws SQLException {
        return 0;
    }

    @Override
    public int getConcurrency() throws SQLException {
        return 0;
    }

    @Override
    public boolean rowUpdated() throws SQLException {
        return false;
    }

    @Override
    public boolean rowInserted() throws SQLException {
        return false;
    }

    @Override
    public boolean rowDeleted() throws SQLException {
        return false;
    }

    @Override
    public void updateNull(final int columnIndex) throws SQLException {

    }

    @Override
    public void updateBoolean(final int columnIndex, final boolean x) throws SQLException {

    }

    @Override
    public void updateByte(final int columnIndex, final byte x) throws SQLException {

    }

    @Override
    public void updateShort(final int columnIndex, final short x) throws SQLException {

    }

    @Override
    public void updateInt(final int columnIndex, final int x) throws SQLException {

    }

    @Override
    public void updateLong(final int columnIndex, final long x) throws SQLException {

    }

    @Override
    public void updateFloat(final int columnIndex, final float x) throws SQLException {

    }

    @Override
    public void updateDouble(final int columnIndex, final double x) throws SQLException {

    }

    @Override
    public void updateBigDecimal(final int columnIndex, final BigDecimal x) throws SQLException {

    }

    @Override
    public void updateString(final int columnIndex, final String x) throws SQLException {

    }

    @Override
    public void updateBytes(final int columnIndex, final byte[] x) throws SQLException {

    }

    @Override
    public void updateDate(final int columnIndex, final Date x) throws SQLException {

    }

    @Override
    public void updateTime(final int columnIndex, final Time x) throws SQLException {

    }

    @Override
    public void updateTimestamp(final int columnIndex, final Timestamp x) throws SQLException {

    }

    @Override
    public void updateAsciiStream(final int columnIndex, final InputStream x, final int length) throws SQLException {

    }

    @Override
    public void updateBinaryStream(final int columnIndex, final InputStream x, final int length) throws SQLException {

    }

    @Override
    public void updateCharacterStream(final int columnIndex, final Reader x, final int length) throws SQLException {

    }

    @Override
    public void updateObject(final int columnIndex, final Object x, final int scaleOrLength) throws SQLException {

    }

    @Override
    public void updateObject(final int columnIndex, final Object x) throws SQLException {

    }

    @Override
    public void updateNull(final String label) throws SQLException {

    }

    @Override
    public void updateBoolean(final String label, final boolean x) throws SQLException {

    }

    @Override
    public void updateByte(final String label, final byte x) throws SQLException {

    }

    @Override
    public void updateShort(final String label, final short x) throws SQLException {

    }

    @Override
    public void updateInt(final String label, final int x) throws SQLException {

    }

    @Override
    public void updateLong(final String label, final long x) throws SQLException {

    }

    @Override
    public void updateFloat(final String label, final float x) throws SQLException {

    }

    @Override
    public void updateDouble(final String label, final double x) throws SQLException {

    }

    @Override
    public void updateBigDecimal(final String label, final BigDecimal x) throws SQLException {

    }

    @Override
    public void updateString(final String label, final String x) throws SQLException {

    }

    @Override
    public void updateBytes(final String label, final byte[] x) throws SQLException {

    }

    @Override
    public void updateDate(final String label, final Date x) throws SQLException {

    }

    @Override
    public void updateTime(final String label, final Time x) throws SQLException {

    }

    @Override
    public void updateTimestamp(final String label, final Timestamp x) throws SQLException {

    }

    @Override
    public void updateAsciiStream(final String label, final InputStream x, final int length) throws SQLException {

    }

    @Override
    public void updateBinaryStream(final String label, final InputStream x, final int length) throws SQLException {

    }

    @Override
    public void updateCharacterStream(final String label, final Reader reader, final int length) throws SQLException {

    }

    @Override
    public void updateObject(final String label, final Object x, final int scaleOrLength) throws SQLException {

    }

    @Override
    public void updateObject(final String label, final Object x) throws SQLException {

    }

    @Override
    public void insertRow() throws SQLException {

    }

    @Override
    public void updateRow() throws SQLException {

    }

    @Override
    public void deleteRow() throws SQLException {

    }

    @Override
    public void refreshRow() throws SQLException {

    }

    @Override
    public void cancelRowUpdates() throws SQLException {

    }

    @Override
    public void moveToInsertRow() throws SQLException {

    }

    @Override
    public void moveToCurrentRow() throws SQLException {

    }

    @Override
    public Statement getStatement() throws SQLException {
        return null;
    }

    @Override
    public Object getObject(final int columnIndex, final Map<String, Class<?>> map) throws SQLException {
        return null;
    }

    @Override
    public Ref getRef(final int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Blob getBlob(final int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Clob getClob(final int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Array getArray(final int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Object getObject(final String label, final Map<String, Class<?>> map) throws SQLException {
        return null;
    }

    @Override
    public Ref getRef(final String label) throws SQLException {
        return null;
    }

    @Override
    public Blob getBlob(final String label) throws SQLException {
        return null;
    }

    @Override
    public Clob getClob(final String label) throws SQLException {
        return null;
    }

    @Override
    public Array getArray(final String label) throws SQLException {
        return null;
    }

    @Override
    public Date getDate(final int columnIndex, final Calendar cal) throws SQLException {
        return null;
    }

    @Override
    public Date getDate(final String label, final Calendar cal) throws SQLException {
        return null;
    }

    @Override
    public Time getTime(final int columnIndex, final Calendar cal) throws SQLException {
        return null;
    }

    @Override
    public Time getTime(final String label, final Calendar cal) throws SQLException {
        return null;
    }

    @Override
    public Timestamp getTimestamp(final int columnIndex, final Calendar cal) throws SQLException {
        return null;
    }

    @Override
    public Timestamp getTimestamp(final String label, final Calendar cal) throws SQLException {
        return null;
    }

    @Override
    public URL getURL(final int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public URL getURL(final String label) throws SQLException {
        return null;
    }

    @Override
    public void updateRef(final int columnIndex, final Ref x) throws SQLException {

    }

    @Override
    public void updateRef(final String label, final Ref x) throws SQLException {

    }

    @Override
    public void updateBlob(final int columnIndex, final Blob x) throws SQLException {

    }

    @Override
    public void updateBlob(final String label, final Blob x) throws SQLException {

    }

    @Override
    public void updateClob(final int columnIndex, final Clob x) throws SQLException {

    }

    @Override
    public void updateClob(final String label, final Clob x) throws SQLException {

    }

    @Override
    public void updateArray(final int columnIndex, final Array x) throws SQLException {

    }

    @Override
    public void updateArray(final String label, final Array x) throws SQLException {

    }

    @Override
    public RowId getRowId(final int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public RowId getRowId(final String label) throws SQLException {
        return null;
    }

    @Override
    public void updateRowId(final int columnIndex, final RowId x) throws SQLException {

    }

    @Override
    public void updateRowId(final String label, final RowId x) throws SQLException {

    }

    @Override
    public int getHoldability() throws SQLException {
        return 0;
    }

    @Override
    public boolean isClosed() throws SQLException {
        return false;
    }

    @Override
    public void updateNString(final int columnIndex, final String nString) throws SQLException {

    }

    @Override
    public void updateNString(final String label, final String nString) throws SQLException {

    }

    @Override
    public void updateNClob(final int columnIndex, final NClob nClob) throws SQLException {

    }

    @Override
    public void updateNClob(final String label, final NClob nClob) throws SQLException {

    }

    @Override
    public NClob getNClob(final int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public NClob getNClob(final String label) throws SQLException {
        return null;
    }

    @Override
    public SQLXML getSQLXML(final int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public SQLXML getSQLXML(final String label) throws SQLException {
        return null;
    }

    @Override
    public void updateSQLXML(final int columnIndex, final SQLXML xmlObject) throws SQLException {

    }

    @Override
    public void updateSQLXML(final String label, final SQLXML xmlObject) throws SQLException {

    }

    @Override
    public String getNString(final int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public String getNString(final String label) throws SQLException {
        return null;
    }

    @Override
    public Reader getNCharacterStream(final int columnIndex) throws SQLException {
        return null;
    }

    @Override
    public Reader getNCharacterStream(final String label) throws SQLException {
        return null;
    }

    @Override
    public void updateNCharacterStream(final int columnIndex, final Reader x, final long length) throws SQLException {

    }

    @Override
    public void updateNCharacterStream(final String label, final Reader reader, final long length) throws SQLException {

    }

    @Override
    public void updateAsciiStream(final int columnIndex, final InputStream x, final long length) throws SQLException {

    }

    @Override
    public void updateBinaryStream(final int columnIndex, final InputStream x, final long length) throws SQLException {

    }

    @Override
    public void updateCharacterStream(final int columnIndex, final Reader x, final long length) throws SQLException {

    }

    @Override
    public void updateAsciiStream(final String label, final InputStream x, final long length) throws SQLException {

    }

    @Override
    public void updateBinaryStream(final String label, final InputStream x, final long length) throws SQLException {

    }

    @Override
    public void updateCharacterStream(final String label, final Reader reader, final long length) throws SQLException {

    }

    @Override
    public void updateBlob(final int index, final InputStream inputStream, final long length) throws SQLException {

    }

    @Override
    public void updateBlob(final String label, final InputStream inputStream, final long length) throws SQLException {

    }

    @Override
    public void updateClob(final int columnIndex, final Reader reader, final long length) throws SQLException {

    }

    @Override
    public void updateClob(final String label, final Reader reader, final long length) throws SQLException {

    }

    @Override
    public void updateNClob(final int columnIndex, final Reader reader, final long length) throws SQLException {

    }

    @Override
    public void updateNClob(final String label, final Reader reader, final long length) throws SQLException {

    }

    @Override
    public void updateNCharacterStream(final int columnIndex, final Reader x) throws SQLException {

    }

    @Override
    public void updateNCharacterStream(final String label, final Reader reader) throws SQLException {

    }

    @Override
    public void updateAsciiStream(final int columnIndex, final InputStream x) throws SQLException {

    }

    @Override
    public void updateBinaryStream(final int columnIndex, final InputStream x) throws SQLException {

    }

    @Override
    public void updateCharacterStream(final int columnIndex, final Reader x) throws SQLException {

    }

    @Override
    public void updateAsciiStream(final String label, final InputStream x) throws SQLException {

    }

    @Override
    public void updateBinaryStream(final String label, final InputStream x) throws SQLException {

    }

    @Override
    public void updateCharacterStream(final String label, final Reader reader) throws SQLException {

    }

    @Override
    public void updateBlob(final int columnIndex, final InputStream inputStream) throws SQLException {

    }

    @Override
    public void updateBlob(final String label, final InputStream inputStream) throws SQLException {

    }

    @Override
    public void updateClob(final int columnIndex, final Reader reader) throws SQLException {

    }

    @Override
    public void updateClob(final String label, final Reader reader) throws SQLException {

    }

    @Override
    public void updateNClob(final int columnIndex, final Reader reader) throws SQLException {

    }

    @Override
    public void updateNClob(final String label, final Reader reader) throws SQLException {

    }

    @Override
    public <T> T getObject(final int columnIndex, final Class<T> type) throws SQLException {
        return null;
    }

    @Override
    public <T> T getObject(final String label, final Class<T> type) throws SQLException {
        return null;
    }

    @Override
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(final Class<?> iface) throws SQLException {
        return false;
    }
}
