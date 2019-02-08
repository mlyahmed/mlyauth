package com.hohou.federation.idp.person.mocks;

import com.hohou.federation.idp.person.model.PersonBean;
import com.hohou.federation.idp.person.validator.PersonValidator;
import com.hohou.federation.idp.tools.MockReseter;
import com.hohou.federation.idp.tools.ResettableMock;

public final class MockPersonValidator extends PersonValidator implements ResettableMock {

    private static volatile MockPersonValidator instance;

    private RuntimeException forcedError;

    public static MockPersonValidator getInstance() {
        if (instance == null) {
            synchronized (MockPersonValidator.class) {
                if (instance == null)
                    instance = new MockPersonValidator();
            }
        }
        return instance;
    }

    private MockPersonValidator() {
        MockReseter.register(this);
        personLookuper = MockPersonLookuper.getInstance();
    }

    public void setForcedError(final RuntimeException forcedError) {
        this.forcedError = forcedError;
    }

    @Override
    public void validateNew(final PersonBean bean) {
        if (forcedError != null) throw forcedError;
        super.validateNew(bean);
    }

    @Override
    public void reset() {
        instance = null;
    }
}
