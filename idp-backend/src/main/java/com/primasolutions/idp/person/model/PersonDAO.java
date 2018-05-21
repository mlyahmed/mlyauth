package com.primasolutions.idp.person.model;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonDAO extends JpaRepository<Person, Long> {

    Person findByExternalId(String externalId);
}
