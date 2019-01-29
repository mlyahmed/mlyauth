package com.primasolutions.idp.person.mocks;

import com.primasolutions.idp.person.model.Person;
import com.primasolutions.idp.person.model.PersonDAO;
import com.primasolutions.idp.tools.MockReseter;
import com.primasolutions.idp.tools.ResettableMock;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

public final class MockPersonDAO implements PersonDAO, ResettableMock {

    private static final int INITIAL_VALUE = 9429;
    private static long currentID = INITIAL_VALUE;

    private static volatile MockPersonDAO instance;

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
        persons.clear();
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
    public List<Person> findAllById(final Iterable<Long> longs) {
        return null;
    }

    @Override
    public <S extends Person> List<S> saveAll(final Iterable<S> entities) {
        return null;
    }

    @Override
    public Optional<Person> findById(final Long personId) {
        return Optional.of(persons.get(personId));
    }

    @Override
    public boolean existsById(final Long personId) {
        return persons.get(personId) != null;
    }

    @Override
    public Page<Person> findAll(final Pageable pageable) {
        return null;
    }

    @Override
    public long count() {
        return persons.size();
    }

    @Override
    public void deleteById(final Long aLong) {

    }

    @Override
    public void delete(final Person entity) {

    }

    @Override
    public void deleteAll(final Iterable<? extends Person> entities) {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public <S extends Person> S save(final S entity) {
        if (entity.getId() < 1) entity.setId(++currentID);
        persons.put(entity.getId(), entity);
        return entity;
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
    public <S extends Person> Optional<S> findOne(final Example<S> example) {
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
