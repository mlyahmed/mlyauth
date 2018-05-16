package com.primasolutions.idp.person.mocks;

import com.primasolutions.idp.person.PersonBean;
import com.primasolutions.idp.person.PersonValidator;

public final class MockPersonValidator extends PersonValidator {

    private static MockPersonValidator instance;

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
}
