package com.mlyauth.mappers;

import com.mlyauth.beans.PersonBean;
import com.mlyauth.domain.Person;

public class PersonMapper implements IDomainMapper<Person, PersonBean>{

    @Override
    public PersonBean toBean(Person person) {
        if(person == null) return null;
        return PersonBean.newInstance()
                .setId(person.getId())
                .setFirstname(person.getFirstname())
                .setLastname(person.getLastname())
                .setEmail(person.getEmail());
    }

    @Override
    public Person toEntity(PersonBean bean) {
        return null;
    }

}
