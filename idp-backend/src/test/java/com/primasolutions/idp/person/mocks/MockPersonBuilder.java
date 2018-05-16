package com.primasolutions.idp.person.mocks;

import com.primasolutions.idp.authentication.mocks.MockRoleDAO;
import com.primasolutions.idp.person.PersonBuilder;

public class MockPersonBuilder extends PersonBuilder {

    public MockPersonBuilder() {
        this.personDAO = MockPersonDAO.getInstance();
        this.roleDAO = MockRoleDAO.getInstance();
    }

}
