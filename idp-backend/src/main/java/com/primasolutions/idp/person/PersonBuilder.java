package com.primasolutions.idp.person;

import com.google.common.collect.Sets;
import com.primasolutions.idp.IDomainMapper;
import com.primasolutions.idp.application.Application;
import com.primasolutions.idp.authentication.RoleDAO;
import com.primasolutions.idp.constants.RoleCode;
import com.primasolutions.idp.exception.IDPException;
import com.primasolutions.idp.person.model.Person;
import com.primasolutions.idp.person.model.PersonBean;
import com.primasolutions.idp.person.model.PersonDAO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.stream.Collectors;

@Component
public class PersonBuilder implements IDomainMapper<Person, PersonBean> {

    public static final String DATE_FORMAT = "yyyy-MM-dd";
    private SimpleDateFormat dateFormatter = new SimpleDateFormat(DATE_FORMAT);

    @Autowired
    protected PersonDAO personDAO;

    @Autowired
    protected RoleDAO roleDAO;

    @Override
    public PersonBean toBean(final Person person) {
        if (person == null) return null;
        return PersonBean.newInstance()
                .setId(person.getId())
                .setRole(person.getRole().getCode().getValue())
                .setExternalId(person.getExternalId())
                .setFirstname(person.getFirstname())
                .setLastname(person.getLastname())
                .setBirthdate(parseBithdate(person))
                .setEmail(person.getEmail())
                .setApplications(applicationsToAppnames(person));
    }

    private String parseBithdate(final Person person) {
        return person.getBirthdate() != null ? dateFormatter.format(person.getBirthdate()) : null;
    }

    private HashSet<String> applicationsToAppnames(final Person person) {
        return Sets.newHashSet(person.getApplications().stream()
                .filter(app -> app.getAppname() != null)
                .map(Application::getAppname)
                .collect(Collectors.toSet()));
    }

    @Override
    public Person toEntity(final PersonBean bean) {
        return bean == null ? null : createPerson(bean)
                .setRole(roleDAO.getOne(RoleCode.create(bean.getRole())))
                .setExternalId(bean.getExternalId())
                .setFirstname(bean.getFirstname())
                .setLastname(bean.getLastname())
                .setBirthdate(parseBirthdate(bean))
                .setEmail(bean.getEmail());
    }

    private Person createPerson(final PersonBean bean) {
        final Person person = personDAO.findByExternalId(bean.getExternalId());
        return person != null ? person : Person.newInstance().setId(bean.getId());
    }

    private Date parseBirthdate(final PersonBean bean) {
        try {
            return StringUtils.isNotBlank(bean.getBirthdate()) ? dateFormatter.parse(bean.getBirthdate()) : null;
        } catch (final ParseException e) {
            throw IDPException.newInstance(e);
        }
    }

}
