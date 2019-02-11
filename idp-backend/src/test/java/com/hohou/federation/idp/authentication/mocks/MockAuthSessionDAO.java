package com.hohou.federation.idp.authentication.mocks;

import com.hohou.federation.idp.authentication.AuthSession;
import com.hohou.federation.idp.authentication.AuthSessionDAO;
import com.hohou.federation.idp.tools.MockReseter;
import com.hohou.federation.idp.tools.ResettableMock;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("unchecked")
public final class MockAuthSessionDAO implements AuthSessionDAO, ResettableMock {

    private static final long INITIAL_VALUE = 7665L;
    private static Long currentId = INITIAL_VALUE;

    private static volatile MockAuthSessionDAO instance;

    private Map<Long, AuthSession> sessions;

    public static MockAuthSessionDAO getInstance() {
        if (instance == null) {
            synchronized (MockAuthSessionDAO.class) {
                if (instance == null)
                    instance = new MockAuthSessionDAO();
            }
        }
        return instance;
    }


    private MockAuthSessionDAO() {
        sessions = new HashMap<>();
        MockReseter.register(this);
    }

    public void reset() {
        sessions.clear();
    }

    @Override
    public AuthSession save(final AuthSession entity) {
        entity.setId(++currentId);
        sessions.put(currentId, entity);
        return entity;
    }

    @Override
    public <S extends AuthSession> Iterable<S> saveAll(final Iterable<S> entities) {
        return null;
    }

    @Override
    public Optional<AuthSession> findById(final Long sessionsId) {
        return Optional.of(sessions.get(sessionsId));
    }

    @Override
    public boolean existsById(final Long aLong) {
        return false;
    }

    @Override
    public Iterable<AuthSession> findAll() {
        return null;
    }

    @Override
    public Iterable<AuthSession> findAllById(final Iterable<Long> longs) {
        return null;
    }

    @Override
    public long count() {
        return sessions.size();
    }

    @Override
    public void deleteById(final Long aLong) {

    }

    @Override
    public void delete(final AuthSession entity) {

    }

    @Override
    public void deleteAll(final Iterable<? extends AuthSession> entities) {

    }

    @Override
    public void deleteAll() {

    }
}
