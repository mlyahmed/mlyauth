package com.primasolutions.idp.dao;

import com.primasolutions.idp.domain.AuthenticationSession;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class MockAuthenticationSessionDAO implements AuthenticationSessionDAO {

    private static Long currentId = 0L;
    private Map<Long, AuthenticationSession> sessions = new HashMap<>();

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
