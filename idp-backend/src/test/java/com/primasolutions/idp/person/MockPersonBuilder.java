package com.primasolutions.idp.person;

import com.primasolutions.idp.authentication.mocks.MockRoleDAO;

public class MockPersonBuilder extends PersonBuilder {

    public MockPersonBuilder() {
        this.personDAO = MockPersonDAO.getInstance();
        this.roleDAO = MockRoleDAO.getInstance();
    }

}
