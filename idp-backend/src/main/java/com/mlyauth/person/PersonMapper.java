package com.mlyauth.person;

import com.google.common.collect.Sets;
import com.mlyauth.IDomainMapper;
import com.mlyauth.beans.PersonBean;
import com.mlyauth.dao.ApplicationDAO;
import com.mlyauth.domain.Application;
import com.mlyauth.domain.AuthenticationInfo;
import com.mlyauth.domain.Person;
import com.mlyauth.exception.IDPException;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PersonMapper implements IDomainMapper<Person, PersonBean> {

    private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

    @Autowired
    private ApplicationDAO applicationDAO;

    @Override
    public PersonBean toBean(Person person) {
        if(person == null) return null;
        return PersonBean.newInstance()
                .setId(person.getId())
                .setExternalId(person.getExternalId())
                .setFirstname(person.getFirstname())
                .setLastname(person.getLastname())
                .setBirthdate(parseBithdate(person))
                .setEmail(person.getEmail())
                .setApplications(applicationsToAppnames(person));
    }

    private String parseBithdate(Person person) {
        return person.getBirthdate() != null ? dateFormatter.format(person.getBirthdate()) : null;
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
                .setBirthdate(parseBirthdate(bean))
                .setAuthenticationInfo(AuthenticationInfo.newInstance().setLogin(bean.getEmail()))
                .setEmail(bean.getEmail())
                .setApplications(appnamesToApplications(bean.getApplications()));
    }

    private Date parseBirthdate(PersonBean bean) {
        try{
            return StringUtils.isNotBlank(bean.getBirthdate())? dateFormatter.parse(bean.getBirthdate()) : null;
        }catch(ParseException e){
            throw IDPException.newInstance(e);
        }
    }


    private Set<Application> appnamesToApplications(Collection<String> appnames) {
        if (appnames == null || appnames.isEmpty())
            return Sets.newHashSet();
        return appnames.stream().map(appname -> applicationDAO.findByAppname(appname)).collect(Collectors.toSet());
    }
}
