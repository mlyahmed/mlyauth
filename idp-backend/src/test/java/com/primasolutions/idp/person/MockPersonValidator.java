package com.primasolutions.idp.person;

import com.primasolutions.idp.beans.PersonBean;

public class MockPersonValidator extends PersonValidator {

    private RuntimeException forcedError;

    public void setForcedError(final RuntimeException forcedError) {
        this.forcedError = forcedError;
    }

    @Override
    public void validateNew(final PersonBean bean) {
        if (forcedError != null) throw forcedError;
        super.validateNew(bean);
    }
}
