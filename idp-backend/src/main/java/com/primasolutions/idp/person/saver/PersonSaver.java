package com.primasolutions.idp.person.saver;

import com.primasolutions.idp.person.model.Person;

public interface PersonSaver {
    void create(Person p);

    void update(Person p);
}
