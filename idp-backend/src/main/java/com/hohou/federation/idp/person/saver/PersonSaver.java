package com.hohou.federation.idp.person.saver;

import com.hohou.federation.idp.person.model.Person;

public interface PersonSaver {
    void create(Person p);

    void update(Person p);
}
