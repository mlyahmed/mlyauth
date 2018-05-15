package com.primasolutions.idp.person;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonDAO extends JpaRepository<Person, Long> {

    Person findByEmail(String email);

    Person findByExternalId(String externalId);
}
