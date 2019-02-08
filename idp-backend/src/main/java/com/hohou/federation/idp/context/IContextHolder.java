package com.hohou.federation.idp.context;

import com.hohou.federation.idp.application.Application;
import com.hohou.federation.idp.person.model.Person;

public interface IContextHolder extends IContext {

    IContext getContext();

    IContext newPersonContext(Person person);

    IContext newApplicationContext(Application application);

    void reset();

}
