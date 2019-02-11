package com.hohou.federation.idp.context.mocks;

import com.hohou.federation.idp.authentication.mocks.MockAuthSessionDAO;
import com.hohou.federation.idp.context.ContextHolder;
import com.hohou.federation.idp.context.ContextIdGenerator;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class MockContextHolder extends ContextHolder {

    public MockContextHolder() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(new MockHttpSession());
        ServletRequestAttributes requestAttributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(requestAttributes);
        idGenerator = new ContextIdGenerator();
        authSessionDAO = MockAuthSessionDAO.getInstance();
    }


    public void resetMock() {
        RequestContextHolder.resetRequestAttributes();
    }

}
