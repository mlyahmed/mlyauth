package com.primasolutions.idp.person;

import com.primasolutions.idp.dao.AuthenticationInfoByLoginDAO;
import com.primasolutions.idp.dao.AuthenticationInfoDAO;
import com.primasolutions.idp.dao.PersonByEmailDAO;
import com.primasolutions.idp.dao.PersonDAO;
import com.primasolutions.idp.domain.AuthenticationInfo;
import com.primasolutions.idp.domain.AuthenticationInfoByLogin;
import com.primasolutions.idp.domain.Person;
import com.primasolutions.idp.domain.PersonByEmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PersonSaver {

    @Autowired
    private AuthenticationInfoDAO authenticationInfoDAO;

    @Autowired
    private AuthenticationInfoByLoginDAO authInfoByLoginDAO;

    @Autowired
    private PersonDAO personDAO;

    @Autowired
    private PersonByEmailDAO personByEmailDAO;

    public void save(final Person p) {
        final Person person = personDAO.saveAndFlush(p);

        personByEmailDAO.saveAndFlush(PersonByEmail.newInstance()
                .setPersonId(person.getExternalId())
                .setEmail(person.getEmail()));

        final AuthenticationInfo authInfo = authenticationInfoDAO.saveAndFlush(person.getAuthenticationInfo());

        authInfoByLoginDAO.saveAndFlush(AuthenticationInfoByLogin.newInstance()
                .setAuthInfoId(authInfo.getId())
                .setLogin(authInfo.getLogin()));
    }
}
