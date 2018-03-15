package com.mlyauth.context;

import com.mlyauth.domain.Application;
import com.mlyauth.domain.Person;

public interface IContextHolder extends IContext {

    IContext getContext();

    IContext newPersonContext(Person person);

    IContext newApplicationContext(Application application);

    void reset();

}
