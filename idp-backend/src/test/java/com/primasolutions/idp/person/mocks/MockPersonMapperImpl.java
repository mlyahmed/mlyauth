package com.primasolutions.idp.person.mocks;

import com.primasolutions.idp.authentication.mocks.MockRoleDAO;
import com.primasolutions.idp.person.PersonMapperImpl;
import com.primasolutions.idp.tools.MockReseter;
import com.primasolutions.idp.tools.ResettableMock;

public final class MockPersonMapperImpl extends PersonMapperImpl implements ResettableMock {

    private static volatile MockPersonMapperImpl instance;

    public static MockPersonMapperImpl getInstance() {
        if (instance == null) {
            synchronized (MockPersonMapperImpl.class) {
                if (instance == null)
                    instance = new MockPersonMapperImpl();
            }
        }
        return instance;
    }

    private MockPersonMapperImpl() {
        MockReseter.register(this);
        this.personDAO = MockPersonDAO.getInstance();
        this.roleDAO = MockRoleDAO.getInstance();
    }

    @Override
    public void reset() {
        instance = null;
    }
}
