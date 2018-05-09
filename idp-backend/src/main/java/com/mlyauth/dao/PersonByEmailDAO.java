package com.mlyauth.dao;

import com.mlyauth.domain.PersonByEmail;
import org.springframework.data.repository.CrudRepository;

public interface PersonByEmailDAO extends CrudRepository<PersonByEmail, String> {

    PersonByEmail findByEmail(String email);

}
