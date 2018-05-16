package com.primasolutions.idp.person.mocks;

import com.primasolutions.idp.person.PersonBean;
import com.primasolutions.idp.person.PersonValidator;

public class MockPersonValidator extends PersonValidator {

    private RuntimeException forcedError;

    public MockPersonValidator() {
        personLookuper = new MockPersonLookuper();
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
