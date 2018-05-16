package com.primasolutions.idp.authentication.mocks;

import com.primasolutions.idp.authentication.Role;
import com.primasolutions.idp.authentication.RoleDAO;
import com.primasolutions.idp.constants.RoleCode;
import com.primasolutions.idp.tools.MockReseter;
import com.primasolutions.idp.tools.ResettableMock;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public final class MockRoleDAO implements RoleDAO, ResettableMock {

    private static MockRoleDAO instance;

    public static MockRoleDAO getInstance() {
        if (instance == null) {
            synchronized (MockRoleDAO.class) {
                if (instance == null)
                    instance = new MockRoleDAO();
            }
        }
        return instance;
    }

    private MockRoleDAO() {
        MockReseter.register(this);
    }

    @Override
    public void reset() {
        instance = null;
    }

    @Override
    public List<Role> findAll() {
        return null;
    }

    @Override
    public List<Role> findAll(final Sort sort) {
        return null;
    }

    @Override
    public Page<Role> findAll(final Pageable pageable) {
        return null;
    }

    @Override
    public List<Role> findAll(final Iterable<RoleCode> roleCodes) {
        return null;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void delete(final RoleCode roleCode) {

    }

    @Override
    public void delete(final Role entity) {

    }

    @Override
    public void delete(final Iterable<? extends Role> entities) {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public <S extends Role> S save(final S entity) {
        return null;
    }

    @Override
    public <S extends Role> List<S> save(final Iterable<S> entities) {
        return null;
    }

    @Override
    public Role findOne(final RoleCode roleCode) {
        return roleCode == null ? null : Role.newInstance().setCode(roleCode);
    }

    @Override
    public boolean exists(final RoleCode roleCode) {
        return false;
    }

    @Override
    public void flush() {

    }

    @Override
    public <S extends Role> S saveAndFlush(final S entity) {
        return null;
    }

    @Override
    public void deleteInBatch(final Iterable<Role> entities) {

    }

    @Override
    public void deleteAllInBatch() {

    }

    @Override
    public Role getOne(final RoleCode roleCode) {
        return roleCode == null ? null :  Role.newInstance().setCode(roleCode);
    }

    @Override
    public <S extends Role> S findOne(final Example<S> example) {
        return null;
    }

    @Override
    public <S extends Role> List<S> findAll(final Example<S> example) {
        return null;
    }

    @Override
    public <S extends Role> List<S> findAll(final Example<S> example, final Sort sort) {
        return null;
    }

    @Override
    public <S extends Role> Page<S> findAll(final Example<S> example, final Pageable pageable) {
        return null;
    }

    @Override
    public <S extends Role> long count(final Example<S> example) {
        return 0;
    }

    @Override
    public <S extends Role> boolean exists(final Example<S> example) {
        return false;
    }
}
