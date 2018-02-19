package com.mlyauth.mocks.dao;

import com.mlyauth.dao.AuthenticationSessionDAO;
import com.mlyauth.domain.AuthenticationSession;

import java.util.HashMap;
import java.util.Map;

public class MockAuthenticationSessionDAO implements AuthenticationSessionDAO {

    private static Long IDS = 0l;
    Map<Long, AuthenticationSession> sessions = new HashMap<>();

    @Override
    public AuthenticationSession save(AuthenticationSession entity) {
        entity.setId(++IDS);
        sessions.put(IDS, entity);
        return entity.clone(); //Keep it to keep the tests valid
    }


    @Override
    public <S extends AuthenticationSession> Iterable<S> save(Iterable<S> entities) {
        return null;
    }

    @Override
    public AuthenticationSession findOne(Long id) {
        return sessions.get(id).clone();  //Keep it to keep the tests valid
    }

    @Override
    public boolean exists(Long aLong) {
        return false;
    }

    @Override
    public Iterable<AuthenticationSession> findAll() {
        return null;
    }

    @Override
    public Iterable<AuthenticationSession> findAll(Iterable<Long> longs) {
        return null;
    }

    @Override
    public long count() {
        return sessions.size();
    }

    @Override
    public void delete(Long aLong) {

    }

    @Override
    public void delete(AuthenticationSession entity) {

    }

    @Override
    public void delete(Iterable<? extends AuthenticationSession> entities) {

    }

    @Override
    public void deleteAll() {

    }
}
