package com.primasolutions.idp.authentication.mocks;

import com.primasolutions.idp.authentication.AuthenticationInfo;
import com.primasolutions.idp.authentication.AuthenticationInfoDAO;
import com.primasolutions.idp.tools.MockReseter;
import com.primasolutions.idp.tools.ResettableMock;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public final class MockAuthenticationInfoDAO implements AuthenticationInfoDAO, ResettableMock {


    private static final int INITIAL_VALUE = 98;
    private static long lastID = INITIAL_VALUE;

    private HashMap<Long, AuthenticationInfo> authentications;

    private static class LazyHolder {
        static final MockAuthenticationInfoDAO INSTANCE = new MockAuthenticationInfoDAO();
    }

    public static MockAuthenticationInfoDAO getInstance() {
        return LazyHolder.INSTANCE;
    }

    private MockAuthenticationInfoDAO() {
        MockReseter.register(this);
        authentications = new LinkedHashMap<>();
    }

    @Override
    public void reset() {
        authentications.clear();
    }

    @Override
    public List<AuthenticationInfo> findAll() {
        return null;
    }

    @Override
    public List<AuthenticationInfo> findAll(final Sort sort) {
        return null;
    }

    @Override
    public Page<AuthenticationInfo> findAll(final Pageable pageable) {
        return null;
    }

    @Override
    public List<AuthenticationInfo> findAll(final Iterable<Long> longs) {
        return null;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void delete(final Long aLong) {

    }

    @Override
    public void delete(final AuthenticationInfo entity) {

    }

    @Override
    public void delete(final Iterable<? extends AuthenticationInfo> entities) {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public <S extends AuthenticationInfo> S save(final S entity) {
        if (entity.getId() < 1) entity.setId(++lastID);
        authentications.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public <S extends AuthenticationInfo> List<S> save(final Iterable<S> entities) {
        return null;
    }

    @Override
    public AuthenticationInfo findOne(final Long aLong) {
        return authentications.get(aLong);
    }

    @Override
    public boolean exists(final Long aLong) {
        return false;
    }

    @Override
    public void flush() {

    }

    @Override
    public <S extends AuthenticationInfo> S saveAndFlush(final S entity) {
        return save(entity);
    }

    @Override
    public void deleteInBatch(final Iterable<AuthenticationInfo> entities) {

    }

    @Override
    public void deleteAllInBatch() {

    }

    @Override
    public AuthenticationInfo getOne(final Long aLong) {
        return null;
    }

    @Override
    public <S extends AuthenticationInfo> S findOne(final Example<S> example) {
        return null;
    }

    @Override
    public <S extends AuthenticationInfo> List<S> findAll(final Example<S> example) {
        return null;
    }

    @Override
    public <S extends AuthenticationInfo> List<S> findAll(final Example<S> example, final Sort sort) {
        return null;
    }

    @Override
    public <S extends AuthenticationInfo> Page<S> findAll(final Example<S> example, final Pageable pageable) {
        return null;
    }

    @Override
    public <S extends AuthenticationInfo> long count(final Example<S> example) {
        return 0;
    }

    @Override
    public <S extends AuthenticationInfo> boolean exists(final Example<S> example) {
        return false;
    }
}
