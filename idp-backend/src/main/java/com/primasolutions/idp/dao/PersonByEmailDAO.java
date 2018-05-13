package com.primasolutions.idp.dao;

import com.primasolutions.idp.domain.PersonByEmail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface PersonByEmailDAO extends JpaRepository<PersonByEmail, Long> {

    Set<PersonByEmail> findByEmail(String email);

}
