package com.primasolutions.idp.context;

import com.primasolutions.idp.domain.Application;
import com.primasolutions.idp.domain.Person;

public interface IContextHolder extends IContext {

    IContext getContext();

    IContext newPersonContext(Person person);

    IContext newApplicationContext(Application application);

    void reset();

}
