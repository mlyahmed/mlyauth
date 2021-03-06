package com.hohou.federation.idp.person.mocks;

import com.hohou.federation.idp.person.model.PersonByEmail;
import com.hohou.federation.idp.person.model.PersonByEmailDAO;
import com.hohou.federation.idp.sensitive.EmailTokenizer;
import com.hohou.federation.idp.tools.MockReseter;
import com.hohou.federation.idp.tools.ResettableMock;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
        return index.values().stream().filter(p -> p.getEmail().equals(token)).collect(Collectors.toSet());
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
    public List<PersonByEmail> findAllById(final Iterable<Long> longs) {
        return null;
    }

    @Override
    public <S extends PersonByEmail> List<S> saveAll(final Iterable<S> entities) {
        return null;
    }

    @Override
    public Optional<PersonByEmail> findById(final Long aLong) {
        return Optional.of(index.get(aLong));
    }

    @Override
    public boolean existsById(final Long aLong) {
        return false;
    }

    @Override
    public Page<PersonByEmail> findAll(final Pageable pageable) {
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
    public void delete(final PersonByEmail entity) {

    }

    @Override
    public void deleteAll(final Iterable<? extends PersonByEmail> entities) {

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
    public <S extends PersonByEmail> Optional<S> findOne(final Example<S> example) {
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
