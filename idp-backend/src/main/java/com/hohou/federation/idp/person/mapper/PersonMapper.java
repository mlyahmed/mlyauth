package com.hohou.federation.idp.person.mapper;

import com.hohou.federation.idp.person.model.Person;
import com.hohou.federation.idp.person.model.PersonBean;

public interface PersonMapper {

    PersonBean toBean(Person person);

    Person toEntity(PersonBean bean);

}
