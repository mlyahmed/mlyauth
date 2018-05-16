package com.primasolutions.idp.authentication.mocks;

import com.primasolutions.idp.authentication.AuthenticationInfoByLogin;
import com.primasolutions.idp.authentication.AuthenticationInfoByLoginDAO;
import com.primasolutions.idp.sensitive.StringTokenizer;
import com.primasolutions.idp.tools.MockReseter;
import com.primasolutions.idp.tools.ResettableMock;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class MockAuthenticationInfoByLoginDAO implements AuthenticationInfoByLoginDAO, ResettableMock {

    private static final int INITIAL_VALUE = 908;
    private static long currentID = INITIAL_VALUE;

    private static MockAuthenticationInfoByLoginDAO instance;

    private Map<Long, AuthenticationInfoByLogin> index;

    public static MockAuthenticationInfoByLoginDAO getInstance() {
        if (instance == null) {
            synchronized (MockAuthenticationInfoByLoginDAO.class) {
                if (instance == null)
                    instance = new MockAuthenticationInfoByLoginDAO();
            }
        }
        return instance;
    }

    private MockAuthenticationInfoByLoginDAO() {
        index = new LinkedHashMap<>();
        MockReseter.register(this);
    }

    @Override
    public void reset() {
        instance = null;
    }

    @Override
    public Set<AuthenticationInfoByLogin> findByLogin(final String login) {
        final String token = StringTokenizer.newInstance().tokenize(login);
        return index.values().stream().filter(a -> a.getLogin().equals(token)).collect(Collectors.toSet());
    }

    @Override
    public List<AuthenticationInfoByLogin> findAll() {
        return null;
    }

    @Override
    public List<AuthenticationInfoByLogin> findAll(final Sort sort) {
        return null;
    }

    @Override
    public Page<AuthenticationInfoByLogin> findAll(final Pageable pageable) {
        return null;
    }

    @Override
    public List<AuthenticationInfoByLogin> findAll(final Iterable<Long> longs) {
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
    public void delete(final AuthenticationInfoByLogin entity) {

    }

    @Override
    public void delete(final Iterable<? extends AuthenticationInfoByLogin> entities) {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public <S extends AuthenticationInfoByLogin> S save(final S entity) {
        if (entity.getId() < 1) entity.setId(currentID++);
        entity.setLogin(StringTokenizer.newInstance().tokenize(entity.getLogin()));
        index.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public <S extends AuthenticationInfoByLogin> List<S> save(final Iterable<S> entities) {
        return null;
    }

    @Override
    public AuthenticationInfoByLogin findOne(final Long aLong) {
        return index.get(aLong);
    }

    @Override
    public boolean exists(final Long aLong) {
        return false;
    }

    @Override
    public void flush() {

    }

    @Override
    public <S extends AuthenticationInfoByLogin> S saveAndFlush(final S entity) {
        return save(entity);
    }

    @Override
    public void deleteInBatch(final Iterable<AuthenticationInfoByLogin> entities) {

    }

    @Override
    public void deleteAllInBatch() {

    }

    @Override
    public AuthenticationInfoByLogin getOne(final Long aLong) {
        return null;
    }

    @Override
    public <S extends AuthenticationInfoByLogin> S findOne(final Example<S> example) {
        return null;
    }

    @Override
    public <S extends AuthenticationInfoByLogin> List<S> findAll(final Example<S> example) {
        return null;
    }

    @Override
    public <S extends AuthenticationInfoByLogin> List<S> findAll(final Example<S> example, final Sort sort) {
        return null;
    }

    @Override
    public <S extends AuthenticationInfoByLogin> Page<S> findAll(final Example<S> example, final Pageable pageable) {
        return null;
    }

    @Override
    public <S extends AuthenticationInfoByLogin> long count(final Example<S> example) {
        return 0;
    }

    @Override
    public <S extends AuthenticationInfoByLogin> boolean exists(final Example<S> example) {
        return false;
    }
}
