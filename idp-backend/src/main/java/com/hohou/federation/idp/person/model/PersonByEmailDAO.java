package com.hohou.federation.idp.person.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface PersonByEmailDAO extends JpaRepository<PersonByEmail, Long> {

    Set<PersonByEmail> findByEmail(String email);

}
