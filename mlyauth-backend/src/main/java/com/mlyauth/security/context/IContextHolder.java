package com.mlyauth.security.context;

import com.mlyauth.domain.Person;

public interface IContextHolder extends IContext {

    IContext getContext();

    void setContext(IContext context);

    IContext newContext(Person person);

    void reset();

}
