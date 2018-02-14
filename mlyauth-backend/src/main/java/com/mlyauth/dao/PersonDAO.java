package com.mlyauth.dao;

import com.mlyauth.domain.Person;
import org.springframework.data.repository.CrudRepository;

public interface PersonDAO extends CrudRepository<Person, Long> {

    Person findByEmail(String email);

    Person findByExternalId(String externalId);
}
