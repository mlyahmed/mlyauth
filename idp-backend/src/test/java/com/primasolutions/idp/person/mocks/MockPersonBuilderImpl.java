package com.primasolutions.idp.person.mocks;

import com.primasolutions.idp.authentication.mocks.MockRoleDAO;
import com.primasolutions.idp.person.PersonBuilderImpl;
import com.primasolutions.idp.tools.MockReseter;
import com.primasolutions.idp.tools.ResettableMock;

public final class MockPersonBuilderImpl extends PersonBuilderImpl implements ResettableMock {

    private static volatile MockPersonBuilderImpl instance;

    public static MockPersonBuilderImpl getInstance() {
        if (instance == null) {
            synchronized (MockPersonBuilderImpl.class) {
                if (instance == null)
                    instance = new MockPersonBuilderImpl();
            }
        }
        return instance;
    }

    private MockPersonBuilderImpl() {
        MockReseter.register(this);
        this.personDAO = MockPersonDAO.getInstance();
        this.roleDAO = MockRoleDAO.getInstance();
    }

    @Override
    public void reset() {
        instance = null;
    }
}
