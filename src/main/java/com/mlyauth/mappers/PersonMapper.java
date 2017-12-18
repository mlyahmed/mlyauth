package com.mlyauth.mappers;

import com.google.common.collect.Sets;
import com.mlyauth.beans.PersonBean;
import com.mlyauth.domain.Person;

import java.util.HashSet;

public class PersonMapper implements IDomainMapper<Person, PersonBean>{

    @Override
    public PersonBean toBean(Person person) {
        if(person == null) return null;
        return PersonBean.newInstance()
                .setId(person.getId())
                .setFirstname(person.getFirstname())
                .setLastname(person.getLastname())
                .setEmail(person.getEmail())
                .setApplications(applicationsToCodes(person));
    }

    private HashSet<String> applicationsToCodes(Person person) {
        return person.getApplications() == null ? new HashSet<>() : Sets.newHashSet("Policy");
    }

    @Override
    public Person toEntity(PersonBean bean) {
        return null;
    }

}
