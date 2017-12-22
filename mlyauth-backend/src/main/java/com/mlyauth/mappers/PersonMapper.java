package com.mlyauth.mappers;

import com.google.common.collect.Sets;
import com.mlyauth.beans.PersonBean;
import com.mlyauth.domain.Application;
import com.mlyauth.domain.Person;

import java.util.HashSet;
import java.util.stream.Collectors;

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
        return Sets.newHashSet(person.getApplications().stream()
                .filter(app -> app.getAppname() != null)
                .map(Application::getAppname)
                .collect(Collectors.toSet()));
    }

    @Override
    public Person toEntity(PersonBean bean) {
        return null;
    }

}
