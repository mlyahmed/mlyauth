package com.primasolutions.idp.authentication.mocks;

import com.primasolutions.idp.authentication.AuthInfo;
import com.primasolutions.idp.authentication.AuthInfoDAO;
import com.primasolutions.idp.tools.MockReseter;
import com.primasolutions.idp.tools.ResettableMock;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public final class MockAuthInfoDAO implements AuthInfoDAO, ResettableMock {


    private static final int INITIAL_VALUE = 98;
    private static long lastID = INITIAL_VALUE;

    private static volatile MockAuthInfoDAO instance;

    private HashMap<Long, AuthInfo> authentications;

    public static MockAuthInfoDAO getInstance() {
        if (instance == null) {
            synchronized (MockAuthInfoDAO.class) {
                if (instance == null)
                    instance = new MockAuthInfoDAO();
            }
        }
        return instance;
    }

    private MockAuthInfoDAO() {
        authentications = new LinkedHashMap<>();
        MockReseter.register(this);
    }

    @Override
    public void reset() {
        authentications.clear();
    }

    @Override
    public List<AuthInfo> findAll() {
        return null;
    }

    @Override
    public List<AuthInfo> findAll(final Sort sort) {
        return null;
    }

    @Override
    public Page<AuthInfo> findAll(final Pageable pageable) {
        return null;
    }

    @Override
    public List<AuthInfo> findAll(final Iterable<Long> longs) {
        return null;
    }

    @Override
    public long count() {
        return authentications.size();
    }

    @Override
    public void delete(final Long aLong) {

    }

    @Override
    public void delete(final AuthInfo entity) {

    }

    @Override
    public void delete(final Iterable<? extends AuthInfo> entities) {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public <S extends AuthInfo> S save(final S entity) {
        if (entity.getId() < 1) entity.setId(++lastID);
        authentications.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public <S extends AuthInfo> List<S> save(final Iterable<S> entities) {
        return null;
    }

    @Override
    public AuthInfo findOne(final Long aLong) {
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
    public <S extends AuthInfo> S saveAndFlush(final S entity) {
        return save(entity);
    }

    @Override
    public void deleteInBatch(final Iterable<AuthInfo> entities) {

    }

    @Override
    public void deleteAllInBatch() {

    }

    @Override
    public AuthInfo getOne(final Long id) {
        return authentications.get(id);
    }

    @Override
    public <S extends AuthInfo> S findOne(final Example<S> example) {
        return null;
    }

    @Override
    public <S extends AuthInfo> List<S> findAll(final Example<S> example) {
        return null;
    }

    @Override
    public <S extends AuthInfo> List<S> findAll(final Example<S> example, final Sort sort) {
        return null;
    }

    @Override
    public <S extends AuthInfo> Page<S> findAll(final Example<S> example, final Pageable pageable) {
        return null;
    }

    @Override
    public <S extends AuthInfo> long count(final Example<S> example) {
        return 0;
    }

    @Override
    public <S extends AuthInfo> boolean exists(final Example<S> example) {
        return false;
    }
}
