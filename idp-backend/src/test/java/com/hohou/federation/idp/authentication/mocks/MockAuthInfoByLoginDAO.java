package com.hohou.federation.idp.authentication.mocks;

import com.hohou.federation.idp.authentication.AuthInfoByLogin;
import com.hohou.federation.idp.authentication.AuthInfoByLoginDAO;
import com.hohou.federation.idp.sensitive.StringTokenizer;
import com.hohou.federation.idp.tools.MockReseter;
import com.hohou.federation.idp.tools.ResettableMock;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class MockAuthInfoByLoginDAO implements AuthInfoByLoginDAO, ResettableMock {

    private static final int INITIAL_VALUE = 908;
    private static long currentID = INITIAL_VALUE;

    private static volatile MockAuthInfoByLoginDAO instance;

    private Map<Long, AuthInfoByLogin> index;

    public static MockAuthInfoByLoginDAO getInstance() {
        if (instance == null) {
            synchronized (MockAuthInfoByLoginDAO.class) {
                if (instance == null)
                    instance = new MockAuthInfoByLoginDAO();
            }
        }
        return instance;
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
    public List<AuthInfoByLogin> findAllById(final Iterable<Long> longs) {
        return null;
    }

    @Override
    public <S extends AuthInfoByLogin> List<S> saveAll(final Iterable<S> entities) {
        return null;
    }

    @Override
    public Optional<AuthInfoByLogin> findById(final Long aLong) {
        return Optional.of(index.get(aLong));
    }

    @Override
    public boolean existsById(final Long aLong) {
        return false;
    }

    @Override
    public Page<AuthInfoByLogin> findAll(final Pageable pageable) {
        return null;
    }

    @Override
    public long count() {
        return index.size();
    }

    @Override
    public void deleteById(final Long id) {
        index.remove(id);
    }

    @Override
    public void delete(final AuthInfoByLogin entity) {

    }

    @Override
    public void deleteAll(final Iterable<? extends AuthInfoByLogin> entities) {

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
    public <S extends AuthInfoByLogin> Optional<S> findOne(final Example<S> example) {
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
