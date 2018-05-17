package com.primasolutions.idp.person.mocks;

import com.primasolutions.idp.authentication.mocks.MockRoleDAO;
import com.primasolutions.idp.person.PersonBuilder;
import com.primasolutions.idp.tools.MockReseter;
import com.primasolutions.idp.tools.ResettableMock;

public final class MockPersonBuilder extends PersonBuilder implements ResettableMock {

    private static MockPersonBuilder instance;

    public static MockPersonBuilder getInstance() {
        if (instance == null) {
            synchronized (MockPersonBuilder.class) {
                if (instance == null)
                    instance = new MockPersonBuilder();
            }
        }
        return instance;
    }

    private MockPersonBuilder() {
        MockReseter.register(this);
        this.personDAO = MockPersonDAO.getInstance();
        this.roleDAO = MockRoleDAO.getInstance();
    }

    @Override
    public void reset() {
        instance = null;
    }
}
