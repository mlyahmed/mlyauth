package com.mlyauth.mappers;

import com.google.common.collect.Sets;
import com.mlyauth.beans.PersonBean;
import com.mlyauth.dao.ApplicationDAO;
import com.mlyauth.domain.Application;
import com.mlyauth.domain.AuthenticationInfo;
import com.mlyauth.domain.Person;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Named
public class PersonMapper implements IDomainMapper<Person, PersonBean>{


    @Inject
    private ApplicationDAO applicationDAO;

    @Override
    public PersonBean toBean(Person person) {
        if(person == null) return null;
        return PersonBean.newInstance()
                .setId(person.getId())
                .setExternalId(person.getExternalId())
                .setFirstname(person.getFirstname())
                .setLastname(person.getLastname())
                .setEmail(person.getEmail())
                .setApplications(applicationsToAppnames(person));
    }

    private HashSet<String> applicationsToAppnames(Person person) {
        return Sets.newHashSet(person.getApplications().stream()
                .filter(app -> app.getAppname() != null)
                .map(Application::getAppname)
                .collect(Collectors.toSet()));
    }

    @Override
    public Person toEntity(PersonBean bean) {
        return bean == null ? null : Person.newInstance()
                .setId(bean.getId())
                .setExternalId(bean.getExternalId())
                .setFirstname(bean.getFirstname())
                .setLastname(bean.getLastname())
                .setAuthenticationInfo(AuthenticationInfo.newInstance().setLogin(bean.getEmail()))
                .setEmail(bean.getEmail())
                .setApplications(appnamesToApplications(bean.getApplications()));
    }


    private Set<Application> appnamesToApplications(Collection<String> appnames) {
        if (appnames == null || appnames.isEmpty())
            return Sets.newHashSet();
        return appnames.stream().map(appname -> applicationDAO.findByAppname(appname)).collect(Collectors.toSet());
    }
}
