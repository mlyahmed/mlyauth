package com.hohou.federation.idp.person.mocks;

import com.hohou.federation.idp.authentication.mocks.MockRoleDAO;
import com.hohou.federation.idp.person.mapper.PersonMapperImpl;
import com.hohou.federation.idp.tools.MockReseter;
import com.hohou.federation.idp.tools.ResettableMock;

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
