package com.primasolutions.idp.atests.world;

import com.primasolutions.idp.person.model.PersonBean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("cucumber-glue")
public class CurrentPersonHolder {

    private PersonBean currentPerson;

    public PersonBean getCurrentPerson() {
        return currentPerson;
    }

    public void setCurrentPerson(final PersonBean currentPerson) {
        this.currentPerson = currentPerson;
    }

    public String getUsername() {
        return (currentPerson != null) ? currentPerson.getEmail() : null;
    }

    public String getPassword() {
        return (currentPerson != null) ? String.valueOf(currentPerson.getPassword()) : null;
    }
}
