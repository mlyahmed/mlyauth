package com.mlyauth.security.context;

import com.mlyauth.domain.Person;

public interface IContextHolder extends IContext {

    IContext getContext();

    IContext newContext(Person person);

    void reset();

}
