package com.primasolutions.idp.application.mocks;

import com.primasolutions.idp.application.Application;
import com.primasolutions.idp.application.ApplicationDAO;
import com.primasolutions.idp.tools.MockReseter;
import com.primasolutions.idp.tools.ResettableMock;

import java.util.HashMap;
import java.util.LinkedHashMap;

public final class MockApplicationDAO implements ApplicationDAO, ResettableMock {

    private static final int INITIAL_VALUE = 9865;
    private static long currentID = INITIAL_VALUE;

    private static volatile MockApplicationDAO instance;

    private HashMap<Long, Application> applications;

    public static MockApplicationDAO getInstance() {
        if (instance == null) {
            synchronized (MockApplicationDAO.class) {
                if (instance == null)
                    instance = new MockApplicationDAO();
            }
        }
        return instance;
    }

    private MockApplicationDAO() {
        applications = new LinkedHashMap<>();
        MockReseter.register(this);
    }

    @Override
    public void reset() {
        applications.clear();
    }


    @Override
    public Application findByAppname(final String name) {
        return applications.values().stream().filter(a -> a.getAppname().equals(name)).findFirst().orElse(null);
    }

    @Override
    public <S extends Application> S save(final S entity) {
        if (entity.getId() < 1) entity.setId(++currentID);
        applications.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public <S extends Application> Iterable<S> save(final Iterable<S> entities) {
        return null;
    }

    @Override
    public Application findOne(final Long id) {
        return applications.get(id);
    }

    @Override
    public boolean exists(final Long aLong) {
        return false;
    }

    @Override
    public Iterable<Application> findAll() {
        return null;
    }

    @Override
    public Iterable<Application> findAll(final Iterable<Long> longs) {
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
    public void delete(final Application entity) {

    }

    @Override
    public void delete(final Iterable<? extends Application> entities) {

    }

    @Override
    public void deleteAll() {

    }
}
