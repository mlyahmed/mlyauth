package com.primasolutions.idp.authentication.mocks;

import com.primasolutions.idp.authentication.AuthSession;
import com.primasolutions.idp.authentication.AuthSessionDAO;
import com.primasolutions.idp.tools.MockReseter;
import com.primasolutions.idp.tools.ResettableMock;

import java.util.HashMap;
import java.util.Map;

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
        return entity.clone(); //Keep it to keep the tests valid
    }


    @Override
    public <S extends AuthSession> Iterable<S> save(final Iterable<S> entities) {
        return null;
    }

    @Override
    public AuthSession findOne(final Long id) {
        return sessions.get(id).clone();  //Keep it to keep the tests valid
    }

    @Override
    public boolean exists(final Long aLong) {
        return false;
    }

    @Override
    public Iterable<AuthSession> findAll() {
        return null;
    }

    @Override
    public Iterable<AuthSession> findAll(final Iterable<Long> longs) {
        return null;
    }

    @Override
    public long count() {
        return sessions.size();
    }

    @Override
    public void delete(final Long aLong) {

    }

    @Override
    public void delete(final AuthSession entity) {

    }

    @Override
    public void delete(final Iterable<? extends AuthSession> entities) {

    }

    @Override
    public void deleteAll() {

    }
}
