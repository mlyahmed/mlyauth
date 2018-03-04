package com.mlyauth.security.context;

import com.mlyauth.mocks.dao.MockAuthenticationSessionDAO;
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
        sauthSessionDAO = new MockAuthenticationSessionDAO();
    }


    public void resetMock() {
        RequestContextHolder.resetRequestAttributes();
    }

}
