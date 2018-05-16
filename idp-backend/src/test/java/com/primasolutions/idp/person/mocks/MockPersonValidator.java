package com.primasolutions.idp.person.mocks;

import com.primasolutions.idp.person.PersonBean;
import com.primasolutions.idp.person.PersonValidator;

public class MockPersonValidator extends PersonValidator {

    private RuntimeException forcedError;
    private boolean forceOK;

    public void setForcedError(final RuntimeException forcedError) {
        this.forcedError = forcedError;
    }

    public void setForceOK() {
        this.forceOK = true;
    }

    @Override
    public void validateNew(final PersonBean bean) {
        if (forcedError != null) throw forcedError;
        if (forceOK) return;

        super.validateNew(bean);
    }
}
