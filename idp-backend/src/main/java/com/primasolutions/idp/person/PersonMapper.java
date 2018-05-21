package com.primasolutions.idp.person;

import com.primasolutions.idp.person.model.Person;
import com.primasolutions.idp.person.model.PersonBean;

public interface PersonMapper {

    PersonBean toBean(Person person);

    Person toEntity(PersonBean bean);

}
