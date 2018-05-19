package com.primasolutions.idp.authentication.mocks;

import com.primasolutions.idp.authentication.AuthInfoByLogin;
import com.primasolutions.idp.authentication.AuthInfoByLoginDAO;
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

public final class MockAuthInfoByLoginDAO implements AuthInfoByLoginDAO, ResettableMock {

    private static final int INITIAL_VALUE = 908;
    private static long currentID = INITIAL_VALUE;

    private Map<Long, AuthenticationInfoByLogin> index;

    private static class LazyHolder {
        static final MockAuthenticationInfoByLoginDAO INSTANCE = new MockAuthenticationInfoByLoginDAO();
    }

    public static MockAuthenticationInfoByLoginDAO getInstance() {
        return LazyHolder.INSTANCE;
    }

    private MockAuthInfoByLoginDAO() {
        MockReseter.register(this);
        index = new LinkedHashMap<>();
    }

    @Override
    public void reset() {
        index.clear();
    }

    @Override
    public Set<AuthInfoByLogin> findByLogin(final String login) {
        final String token = StringTokenizer.newInstance().tokenize(login);
        return index.values().stream().filter(a -> a.getLogin().equals(token)).collect(Collectors.toSet());
    }

    @Override
    public List<AuthInfoByLogin> findAll() {
        return null;
    }

    @Override
    public List<AuthInfoByLogin> findAll(final Sort sort) {
        return null;
    }

    @Override
    public Page<AuthInfoByLogin> findAll(final Pageable pageable) {
        return null;
    }

    @Override
    public List<AuthInfoByLogin> findAll(final Iterable<Long> longs) {
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
    public void delete(final AuthInfoByLogin entity) {

    }

    @Override
    public void delete(final Iterable<? extends AuthInfoByLogin> entities) {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public <S extends AuthInfoByLogin> S save(final S entity) {
        if (entity.getId() < 1) entity.setId(currentID++);
        entity.setLogin(StringTokenizer.newInstance().tokenize(entity.getLogin()));
        index.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public <S extends AuthInfoByLogin> List<S> save(final Iterable<S> entities) {
        return null;
    }

    @Override
    public AuthInfoByLogin findOne(final Long aLong) {
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
    public <S extends AuthInfoByLogin> S saveAndFlush(final S entity) {
        return save(entity);
    }

    @Override
    public void deleteInBatch(final Iterable<AuthInfoByLogin> entities) {

    }

    @Override
    public void deleteAllInBatch() {

    }

    @Override
    public AuthInfoByLogin getOne(final Long aLong) {
        return null;
    }

    @Override
    public <S extends AuthInfoByLogin> S findOne(final Example<S> example) {
        return null;
    }

    @Override
    public <S extends AuthInfoByLogin> List<S> findAll(final Example<S> example) {
        return null;
    }

    @Override
    public <S extends AuthInfoByLogin> List<S> findAll(final Example<S> example, final Sort sort) {
        return null;
    }

    @Override
    public <S extends AuthInfoByLogin> Page<S> findAll(final Example<S> example, final Pageable pageable) {
        return null;
    }

    @Override
    public <S extends AuthInfoByLogin> long count(final Example<S> example) {
        return 0;
    }

    @Override
    public <S extends AuthInfoByLogin> boolean exists(final Example<S> example) {
        return false;
    }
}
