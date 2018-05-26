package com.primasolutions.idp.application.mocks;

import com.primasolutions.idp.application.ApplicationLookuper;
import com.primasolutions.idp.tools.MockReseter;
import com.primasolutions.idp.tools.ResettableMock;

public final class MockApplicationLookuper extends ApplicationLookuper implements ResettableMock {

    private static volatile MockApplicationLookuper instance;

    public static MockApplicationLookuper getInstance() {
        if (instance == null) {
            synchronized (MockApplicationLookuper.class) {
                if (instance == null)
                    instance = new MockApplicationLookuper();
            }
        }
        return instance;
    }

    private MockApplicationLookuper() {
        MockReseter.register(this);
        applicationDAO = MockApplicationDAO.getInstance();
    }

    @Override
    public void reset() {
        instance = null;
    }


}
