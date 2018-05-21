package com.primasolutions.idp.person.mocks;

import com.primasolutions.idp.person.model.PersonByEmail;
import com.primasolutions.idp.person.model.PersonByEmailDAO;
import com.primasolutions.idp.sensitive.EmailTokenizer;
import com.primasolutions.idp.tools.MockReseter;
import com.primasolutions.idp.tools.ResettableMock;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class MockPersonByEmailDAO implements PersonByEmailDAO, ResettableMock {

    private static final int INITIAL_VALUE = 9519;
    private static long currentID = INITIAL_VALUE;

    private static volatile MockPersonByEmailDAO instance;

    private Map<Long, PersonByEmail> index;

    public static MockPersonByEmailDAO getInstance() {
        if (instance == null) {
            synchronized (MockPersonByEmailDAO.class) {
                if (instance == null)
                    instance = new MockPersonByEmailDAO();
            }
        }
        return instance;
    }

    private MockPersonByEmailDAO() {
        index = new HashMap<>();
        MockReseter.register(this);
    }

    @Override
    public void reset() {
        index.clear();
    }

    @Override
    public Set<PersonByEmail> findByEmail(final String email) {
        final String token = EmailTokenizer.newInstance().tokenizeEmailAddress(email);
        return index.values().stream()
                .filter(p -> p.getEmail().equals(token))
                .map(PersonByEmail::clone)
                .collect(Collectors.toSet());
    }

    @Override
    public List<PersonByEmail> findAll() {
        return null;
    }

    @Override
    public List<PersonByEmail> findAll(final Sort sort) {
        return null;
    }

    @Override
    public Page<PersonByEmail> findAll(final Pageable pageable) {
        return null;
    }

    @Override
    public List<PersonByEmail> findAll(final Iterable<Long> longs) {
        return null;
    }

    @Override
    public long count() {
        return index.size();
    }

    @Override
    public void delete(final Long id) {
        index.remove(id);
    }

    @Override
    public void delete(final PersonByEmail entity) {

    }

    @Override
    public void delete(final Iterable<? extends PersonByEmail> entities) {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public <S extends PersonByEmail> S save(final S entity) {
        if (entity.getId() < 1) entity.setId(currentID++);
        entity.setEmail(EmailTokenizer.newInstance().tokenizeEmailAddress(entity.getEmail()));
        index.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public <S extends PersonByEmail> List<S> save(final Iterable<S> entities) {
        return null;
    }

    @Override
    public PersonByEmail findOne(final Long aLong) {
        return index.get(aLong) == null ? null : index.get(aLong).clone();
    }

    @Override
    public boolean exists(final Long aLong) {
        return false;
    }

    @Override
    public void flush() {

    }

    @Override
    public <S extends PersonByEmail> S saveAndFlush(final S entity) {
        return save(entity);
    }

    @Override
    public void deleteInBatch(final Iterable<PersonByEmail> entities) {

    }

    @Override
    public void deleteAllInBatch() {

    }

    @Override
    public PersonByEmail getOne(final Long aLong) {
        return index.get(aLong);
    }

    @Override
    public <S extends PersonByEmail> S findOne(final Example<S> example) {
        return null;
    }

    @Override
    public <S extends PersonByEmail> List<S> findAll(final Example<S> example) {
        return null;
    }

    @Override
    public <S extends PersonByEmail> List<S> findAll(final Example<S> example, final Sort sort) {
        return null;
    }

    @Override
    public <S extends PersonByEmail> Page<S> findAll(final Example<S> example, final Pageable pageable) {
        return null;
    }

    @Override
    public <S extends PersonByEmail> long count(final Example<S> example) {
        return 0;
    }

    @Override
    public <S extends PersonByEmail> boolean exists(final Example<S> example) {
        return false;
    }
}
