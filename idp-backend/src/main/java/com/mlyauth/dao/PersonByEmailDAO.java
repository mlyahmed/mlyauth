package com.mlyauth.dao;

import com.mlyauth.domain.PersonByEmail;
import org.springframework.data.repository.CrudRepository;

import java.util.Set;

public interface PersonByEmailDAO extends CrudRepository<PersonByEmail, Long> {

    Set<PersonByEmail> findByEmail(String email);

}
