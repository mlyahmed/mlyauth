package com.primasolutions.idp.application.mocks;

import com.primasolutions.idp.application.ApplicationLookuperImpl;
import com.primasolutions.idp.tools.MockReseter;
import com.primasolutions.idp.tools.ResettableMock;

public final class MockApplicationLookuperImpl extends ApplicationLookuperImpl implements ResettableMock {

    private static volatile MockApplicationLookuperImpl instance;

    public static MockApplicationLookuperImpl getInstance() {
        if (instance == null) {
            synchronized (MockApplicationLookuperImpl.class) {
                if (instance == null)
                    instance = new MockApplicationLookuperImpl();
            }
        }
        return instance;
    }

    private MockApplicationLookuperImpl() {
        MockReseter.register(this);
        applicationDAO = MockApplicationDAO.getInstance();
    }

    @Override
    public void reset() {
        instance = null;
    }


}
