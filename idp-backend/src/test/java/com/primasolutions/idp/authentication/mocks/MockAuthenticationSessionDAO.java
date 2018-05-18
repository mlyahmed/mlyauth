package com.primasolutions.idp.authentication.mocks;

import com.primasolutions.idp.authentication.AuthenticationSession;
import com.primasolutions.idp.authentication.AuthenticationSessionDAO;
import com.primasolutions.idp.tools.MockReseter;
import com.primasolutions.idp.tools.ResettableMock;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public final class MockAuthenticationSessionDAO implements AuthenticationSessionDAO, ResettableMock {

    private static final long INITIAL_VALUE = 7665L;
    private static Long currentId = INITIAL_VALUE;

    private static volatile MockAuthenticationSessionDAO instance;

    private Map<Long, AuthenticationSession> sessions;

    public static MockAuthenticationSessionDAO getInstance() {
        if (instance == null) {
            synchronized (MockAuthenticationSessionDAO.class) {
                if (instance == null)
                    instance = new MockAuthenticationSessionDAO();
            }
        }
        return instance;
    }


    private MockAuthenticationSessionDAO() {
        sessions = new HashMap<>();
        MockReseter.register(this);
    }

    public void reset() {
        sessions.clear();
    }

    @Override
    public AuthenticationSession save(final AuthenticationSession entity) {
        entity.setId(++currentId);
        sessions.put(currentId, entity);
        return entity.clone(); //Keep it to keep the tests valid
    }


    @Override
    public <S extends AuthenticationSession> Iterable<S> save(final Iterable<S> entities) {
        return null;
    }

    @Override
    public AuthenticationSession findOne(final Long id) {
        return sessions.get(id).clone();  //Keep it to keep the tests valid
    }

    @Override
    public boolean exists(final Long aLong) {
        return false;
    }

    @Override
    public Iterable<AuthenticationSession> findAll() {
        return null;
    }

    @Override
    public Iterable<AuthenticationSession> findAll(final Iterable<Long> longs) {
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
    public void delete(final AuthenticationSession entity) {

    }

    @Override
    public void delete(final Iterable<? extends AuthenticationSession> entities) {

    }

    @Override
    public void deleteAll() {

    }
}
