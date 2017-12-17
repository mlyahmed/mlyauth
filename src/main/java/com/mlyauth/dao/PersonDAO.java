package com.mlyauth.dao;

import com.mlyauth.domain.Person;
import org.springframework.data.repository.CrudRepository;

public interface PersonDAO extends CrudRepository<Person, Long> {

    Person findByUsername(String username);

}
