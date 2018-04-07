package com.mlyauth.person;

import com.google.common.collect.Sets;
import com.mlyauth.IDomainMapper;
import com.mlyauth.beans.PersonBean;
import com.mlyauth.dao.PersonDAO;
import com.mlyauth.domain.Application;
import com.mlyauth.domain.Person;
import com.mlyauth.exception.IDPException;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.stream.Collectors;

@Component
public class PersonMapper implements IDomainMapper<Person, PersonBean> {

    private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

    @Autowired
    private PersonDAO personDAO;

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
        return bean == null ? null : createPerson(bean)
                .setExternalId(bean.getExternalId())
                .setFirstname(bean.getFirstname())
                .setLastname(bean.getLastname())
                .setBirthdate(parseBirthdate(bean))
                .setEmail(bean.getEmail());
    }

    private Person createPerson(PersonBean bean){
        final Person person = personDAO.findByExternalId(bean.getExternalId());
        return person != null ? person : Person.newInstance().setId(bean.getId());
    }

    private Date parseBirthdate(PersonBean bean) {
        try{
            return StringUtils.isNotBlank(bean.getBirthdate())? dateFormatter.parse(bean.getBirthdate()) : null;
        }catch(ParseException e){
            throw IDPException.newInstance(e);
        }
    }

}
