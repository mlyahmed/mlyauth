package com.hohou.federation.idp.sensitive.mocks;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;

public final class MockPreparedStatement implements PreparedStatement {

    private HashMap<Integer, Object> params = new HashMap<>();
    private HashMap<Integer, Integer> nulls = new HashMap<>();

    public Object getParam(final int index) {
        return this.params.get(index);
    }

    public int getNull(final int index) {
        return nulls.get(index);
    }



    @Override
    public ResultSet executeQuery() {
        return null;
    }

    @Override
    public int executeUpdate() {
        return 0;
    }

    @Override
    public void setNull(final int parameterIndex, final int sqlType) {
        nulls.put(parameterIndex, sqlType);
    }

    @Override
    public void setBoolean(final int parameterIndex, final boolean x) {

    }

    @Override
    public void setByte(final int parameterIndex, final byte x) {

    }

    @Override
    public void setShort(final int parameterIndex, final short x) {

    }

    @Override
    public void setInt(final int parameterIndex, final int x) {

    }

    @Override
    public void setLong(final int parameterIndex, final long x) {

    }

    @Override
    public void setFloat(final int parameterIndex, final float x) {

    }

    @Override
    public void setDouble(final int parameterIndex, final double x) {

    }

    @Override
    public void setBigDecimal(final int parameterIndex, final BigDecimal x) {

    }

    @Override
    public void setString(final int parameterIndex, final String x) {
        params.put(parameterIndex, x);
    }

    @Override
    public void setBytes(final int parameterIndex, final byte[] x) {

    }

    @Override
    public void setDate(final int parameterIndex, final Date x) {

    }

    @Override
    public void setTime(final int parameterIndex, final Time x) {

    }

    @Override
    public void setTimestamp(final int parameterIndex, final Timestamp x) {

    }

    @Override
    public void setAsciiStream(final int parameterIndex, final InputStream x, final int length) {

    }

    @Override
    public void setUnicodeStream(final int parameterIndex, final InputStream x, final int length) {

    }

    @Override
    public void setBinaryStream(final int parameterIndex, final InputStream x, final int length) {

    }

    @Override
    public void clearParameters() {

    }

    @Override
    public void setObject(final int parameterIndex, final Object x, final int targetSqlType) {

    }

    @Override
    public void setObject(final int parameterIndex, final Object x) {

    }

    @Override
    public boolean execute() {
        return false;
    }

    @Override
    public void addBatch() {

    }

    @Override
    public void setCharacterStream(final int index, final Reader reader, final int length) {

    }

    @Override
    public void setRef(final int parameterIndex, final Ref x) {

    }

    @Override
    public void setBlob(final int parameterIndex, final Blob x) {

    }

    @Override
    public void setClob(final int parameterIndex, final Clob x) {

    }

    @Override
    public void setArray(final int parameterIndex, final Array x) {

    }

    @Override
    public ResultSetMetaData getMetaData() {
        return null;
    }

    @Override
    public void setDate(final int parameterIndex, final Date x, final Calendar cal) {

    }

    @Override
    public void setTime(final int parameterIndex, final Time x, final Calendar cal) {

    }

    @Override
    public void setTimestamp(final int parameterIndex, final Timestamp x, final Calendar cal) {

    }

    @Override
    public void setNull(final int parameterIndex, final int sqlType, final String typeName) {

    }

    @Override
    public void setURL(final int parameterIndex, final URL x) {

    }

    @Override
    public ParameterMetaData getParameterMetaData() {
        return null;
    }

    @Override
    public void setRowId(final int index, final RowId x) {

    }

    @Override
    public void setNString(final int index, final String value) {

    }

    @Override
    public void setNCharacterStream(final int index, final Reader value, final long length) {

    }

    @Override
    public void setNClob(final int parameterIndex, final NClob value) {

    }

    @Override
    public void setClob(final int parameterIndex, final Reader reader, final long length) {

    }

    @Override
    public void setBlob(final int index, final InputStream inputStream, final long length) {

    }

    @Override
    public void setNClob(final int index, final Reader reader, final long length) {

    }

    @Override
    public void setSQLXML(final int index, final SQLXML xmlObject) {

    }

    @Override
    public void setObject(final int index, final Object x, final int type, final int scaleOLength) {

    }

    @Override
    public void setAsciiStream(final int parameterIndex, final InputStream x, final long length) {

    }

    @Override
    public void setBinaryStream(final int parameterIndex, final InputStream x, final long length) {

    }

    @Override
    public void setCharacterStream(final int index, final Reader reader, final long length) {

    }

    @Override
    public void setAsciiStream(final int parameterIndex, final InputStream x) {

    }

    @Override
    public void setBinaryStream(final int parameterIndex, final InputStream x) {

    }

    @Override
    public void setCharacterStream(final int parameterIndex, final Reader reader) {

    }

    @Override
    public void setNCharacterStream(final int parameterIndex, final Reader value) {

    }

    @Override
    public void setClob(final int parameterIndex, final Reader reader) {

    }

    @Override
    public void setBlob(final int parameterIndex, final InputStream inputStream) {

    }

    @Override
    public void setNClob(final int parameterIndex, final Reader reader) {

    }

    @Override
    public ResultSet executeQuery(final String sql) {
        return null;
    }

    @Override
    public int executeUpdate(final String sql) {
        return 0;
    }

    @Override
    public void close() {

    }

    @Override
    public int getMaxFieldSize() {
        return 0;
    }

    @Override
    public void setMaxFieldSize(final int max) {

    }

    @Override
    public int getMaxRows() {
        return 0;
    }

    @Override
    public void setMaxRows(final int max) {

    }

    @Override
    public void setEscapeProcessing(final boolean enable) {

    }

    @Override
    public int getQueryTimeout() {
        return 0;
    }

    @Override
    public void setQueryTimeout(final int seconds) {

    }

    @Override
    public void cancel() {

    }

    @Override
    public SQLWarning getWarnings() {
        return null;
    }

    @Override
    public void clearWarnings() {

    }

    @Override
    public void setCursorName(final String name) {

    }

    @Override
    public boolean execute(final String sql) {
        return false;
    }

    @Override
    public ResultSet getResultSet() {
        return null;
    }

    @Override
    public int getUpdateCount() {
        return 0;
    }

    @Override
    public boolean getMoreResults() {
        return false;
    }

    @Override
    public void setFetchDirection(final int direction) {

    }

    @Override
    public int getFetchDirection() {
        return ResultSet.FETCH_FORWARD;
    }

    @Override
    public void setFetchSize(final int rows) {

    }

    @Override
    public int getFetchSize() {
        return 0;
    }

    @Override
    public int getResultSetConcurrency() {
        return ResultSet.CONCUR_UPDATABLE;
    }

    @Override
    public int getResultSetType() {
        return ResultSet.TYPE_FORWARD_ONLY;
    }

    @Override
    public void addBatch(final String sql) {

    }

    @Override
    public void clearBatch() {

    }

    @Override
    public int[] executeBatch() {
        return new int[0];
    }

    @Override
    public Connection getConnection() {
        return null;
    }

    @Override
    public boolean getMoreResults(final int current) {
        return false;
    }

    @Override
    public ResultSet getGeneratedKeys() {
        return null;
    }

    @Override
    public int executeUpdate(final String sql, final int autoGeneratedKeys) {
        return 0;
    }

    @Override
    public int executeUpdate(final String sql, final int[] columnIndexes) {
        return 0;
    }

    @Override
    public int executeUpdate(final String sql, final String[] columnNames) {
        return 0;
    }

    @Override
    public boolean execute(final String sql, final int autoGeneratedKeys) {
        return false;
    }

    @Override
    public boolean execute(final String sql, final int[] columnIndexes) {
        return false;
    }

    @Override
    public boolean execute(final String sql, final String[] columnNames) {
        return false;
    }

    @Override
    public int getResultSetHoldability() {
        return 0;
    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public void setPoolable(final boolean poolable) {

    }

    @Override
    public boolean isPoolable() {
        return false;
    }

    @Override
    public void closeOnCompletion() {

    }

    @Override
    public boolean isCloseOnCompletion() {
        return false;
    }

    @Override
    public <T> T unwrap(final Class<T> iface) {
        return null;
    }

    @Override
    public boolean isWrapperFor(final Class<?> iface) {
        return false;
    }
}
