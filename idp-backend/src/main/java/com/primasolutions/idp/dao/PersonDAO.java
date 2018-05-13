package com.primasolutions.idp.dao;

import com.primasolutions.idp.domain.Person;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonDAO extends JpaRepository<Person, Long> {

    Person findByEmail(String email);

    Person findByExternalId(String externalId);
}
