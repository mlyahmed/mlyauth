package com.primasolutions.idp.person.mocks;

import com.primasolutions.idp.person.Person;
import com.primasolutions.idp.person.PersonDAO;
import com.primasolutions.idp.tools.MockReseter;
import com.primasolutions.idp.tools.ResettableMock;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public final class MockPersonDAO implements PersonDAO, ResettableMock {

    private static final int INITIAL_VALUE = 9429;
    private static long currentID = INITIAL_VALUE;

    private static MockPersonDAO instance;

    private HashMap<Long, Person> persons;

    public static MockPersonDAO getInstance() {
        if (instance == null) {
            synchronized (MockPersonDAO.class) {
                if (instance == null)
                    instance = new MockPersonDAO();
            }
        }
        return instance;
    }

    private MockPersonDAO() {
        persons = new LinkedHashMap<>();
        MockReseter.register(this);
    }

    @Override
    public void reset() {
        instance = null;
    }

    @Override
    public Person findByExternalId(final String extId) {
        return persons.values().stream().filter(p -> p.getExternalId().equals(extId)).findFirst().orElse(null);
    }

    @Override
    public List<Person> findAll() {
        return null;
    }

    @Override
    public List<Person> findAll(final Sort sort) {
        return null;
    }

    @Override
    public Page<Person> findAll(final Pageable pageable) {
        return null;
    }

    @Override
    public List<Person> findAll(final Iterable<Long> longs) {
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
    public void delete(final Person entity) {

    }

    @Override
    public void delete(final Iterable<? extends Person> entities) {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public <S extends Person> S save(final S entity) {
        if (entity.getId() < 1) entity.setId(currentID++);
        persons.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public <S extends Person> List<S> save(final Iterable<S> entities) {
        return null;
    }

    @Override
    public Person findOne(final Long aLong) {
        return persons.get(aLong);
    }

    @Override
    public boolean exists(final Long aLong) {
        return persons.get(aLong) != null;
    }

    @Override
    public void flush() {

    }

    @Override
    public <S extends Person> S saveAndFlush(final S entity) {
        return save(entity);
    }

    @Override
    public void deleteInBatch(final Iterable<Person> entities) {

    }

    @Override
    public void deleteAllInBatch() {

    }

    @Override
    public Person getOne(final Long aLong) {
        return null;
    }

    @Override
    public <S extends Person> S findOne(final Example<S> example) {
        return null;
    }

    @Override
    public <S extends Person> List<S> findAll(final Example<S> example) {
        return null;
    }

    @Override
    public <S extends Person> List<S> findAll(final Example<S> example, final Sort sort) {
        return null;
    }

    @Override
    public <S extends Person> Page<S> findAll(final Example<S> example, final Pageable pageable) {
        return null;
    }

    @Override
    public <S extends Person> long count(final Example<S> example) {
        return 0;
    }

    @Override
    public <S extends Person> boolean exists(final Example<S> example) {
        return false;
    }
}
