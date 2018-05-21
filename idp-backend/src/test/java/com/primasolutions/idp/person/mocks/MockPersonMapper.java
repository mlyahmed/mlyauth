package com.primasolutions.idp.person.mocks;

import com.primasolutions.idp.authentication.mocks.MockRoleDAO;
import com.primasolutions.idp.person.mapper.PersonMapperImpl;
import com.primasolutions.idp.tools.MockReseter;
import com.primasolutions.idp.tools.ResettableMock;

public final class MockPersonMapper extends PersonMapperImpl implements ResettableMock {

    private static volatile MockPersonMapper instance;

    public static MockPersonMapper getInstance() {
        if (instance == null) {
            synchronized (MockPersonMapper.class) {
                if (instance == null)
                    instance = new MockPersonMapper();
            }
        }
        return instance;
    }

    private MockPersonMapper() {
        MockReseter.register(this);
        this.personDAO = MockPersonDAO.getInstance();
        this.roleDAO = MockRoleDAO.getInstance();
    }

    @Override
    public void reset() {
        instance = null;
    }
}
