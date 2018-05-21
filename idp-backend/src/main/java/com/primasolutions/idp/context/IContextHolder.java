package com.primasolutions.idp.context;

import com.primasolutions.idp.application.Application;
import com.primasolutions.idp.person.model.Person;

public interface IContextHolder extends IContext {

    IContext getContext();

    IContext newPersonContext(Person person);

    IContext newApplicationContext(Application application);

    void reset();

}
