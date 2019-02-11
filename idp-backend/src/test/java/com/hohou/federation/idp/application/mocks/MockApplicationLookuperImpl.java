package com.hohou.federation.idp.application.mocks;

import com.hohou.federation.idp.application.ApplicationLookuperImpl;
import com.hohou.federation.idp.tools.MockReseter;
import com.hohou.federation.idp.tools.ResettableMock;

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
