package com.hohou.federation.idp.person.saver;

import com.hohou.federation.idp.authentication.AuthenticationInfoSaver;
import com.hohou.federation.idp.person.model.Person;
import com.hohou.federation.idp.person.model.PersonByEmail;
import com.hohou.federation.idp.person.model.PersonByEmailDAO;
import com.hohou.federation.idp.person.model.PersonDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Set;

import static org.apache.commons.lang3.StringUtils.isEmpty;

@Component
public class PersonSaverImpl implements PersonSaver {

    @Autowired
    protected PersonDAO personDAO;

    @Autowired
    protected PersonByEmailDAO byEmailDAO;

    @Autowired
    protected AuthenticationInfoSaver authenticationInfoSaver;

    @Override
    public void create(final Person p) {
        Assert.notNull(p, "The person arg is mandatory.");
        Assert.notNull(p.getAuthenticationInfo(), "The person authentication arg is mandatory.");
        personDAO.saveAndFlush(p);
        indexByEmail(p);
        authenticationInfoSaver.create(p.getAuthenticationInfo());
    }


    @Override
    public void update(final Person p) {
        Assert.notNull(p, "The person arg is mandatory.");
        ignoreBlankAndEmptyUpdates(p);
        deleteByEmailIndex(p);
        updatePerson(p);
        indexByEmail(p);
        updateAuthentication(p);
    }

    private void ignoreBlankAndEmptyUpdates(final Person p) {
        final Person origin = personDAO.findByExternalId(p.getExternalId());
        p.setFirstname(isEmpty(p.getFirstname()) ? origin.getFirstname() : p.getFirstname());
        p.setLastname(isEmpty(p.getLastname()) ? origin.getLastname() : p.getLastname());
        p.setEmail(isEmpty(p.getEmail()) ? origin.getEmail() : p.getEmail());
        p.setRole(p.getRole() == null ? origin.getRole() : p.getRole());
        p.setBirthdate(p.getBirthdate() == null ? origin.getBirthdate() : p.getBirthdate());
    }

    private void deleteByEmailIndex(final Person p) {
        final Person existing = personDAO.findByExternalId(p.getExternalId());
        final Set<PersonByEmail> byEmails = byEmailDAO.findByEmail(existing.getEmail());
        final PersonByEmail byEmail = byEmails.stream()
                .filter(pbe -> pbe.getPersonId().equals(p.getExternalId()))
                .findFirst().get();
        byEmailDAO.deleteById(byEmail.getId());
    }

    private void updatePerson(final Person p) {
        final Person person = personDAO.findByExternalId(p.getExternalId());
        person.setEmail(p.getEmail())
                .setFirstname(p.getFirstname())
                .setLastname(p.getLastname())
                .setBirthdate(p.getBirthdate())
                .setRole(p.getRole());
        personDAO.saveAndFlush(person);
    }

    private void indexByEmail(final Person p) {
        byEmailDAO.saveAndFlush(PersonByEmail.newInstance().setPersonId(p.getExternalId()).setEmail(p.getEmail()));
    }

    private void updateAuthentication(final Person p) {
        authenticationInfoSaver.update(p.getAuthenticationInfo().clone().setLogin(p.getEmail()));
    }
}
