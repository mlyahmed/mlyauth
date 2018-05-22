package com.primasolutions.idp.person.saver;

import com.primasolutions.idp.authentication.AuthenticationInfoSaver;
import com.primasolutions.idp.person.model.Person;
import com.primasolutions.idp.person.model.PersonByEmail;
import com.primasolutions.idp.person.model.PersonByEmailDAO;
import com.primasolutions.idp.person.model.PersonDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Set;

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
        deleteByEmailIndex(p);
        updatePerson(p);
        indexByEmail(p);
        updateAuthentication(p);
    }

    private void deleteByEmailIndex(final Person p) {
        final Person existing = personDAO.findByExternalId(p.getExternalId());
        final Set<PersonByEmail> byEmails = byEmailDAO.findByEmail(existing.getEmail());
        final PersonByEmail byEmail = byEmails.stream()
                .filter(pbe -> pbe.getPersonId().equals(p.getExternalId()))
                .findFirst().get();
        byEmailDAO.delete(byEmail.getId());
    }

    private void updatePerson(final Person p) {
        final Person existing2 = personDAO.findByExternalId(p.getExternalId());
        existing2.setEmail(p.getEmail())
                .setFirstname(p.getFirstname())
                .setLastname(p.getLastname())
                .setBirthdate(p.getBirthdate())
                .setRole(p.getRole());
        personDAO.saveAndFlush(existing2);
    }

    private void indexByEmail(final Person p) {
        byEmailDAO.saveAndFlush(PersonByEmail.newInstance().setPersonId(p.getExternalId()).setEmail(p.getEmail()));
    }

    private void updateAuthentication(final Person p) {
        authenticationInfoSaver.update(p.getAuthenticationInfo().clone().setLogin(p.getEmail()));
    }
}
