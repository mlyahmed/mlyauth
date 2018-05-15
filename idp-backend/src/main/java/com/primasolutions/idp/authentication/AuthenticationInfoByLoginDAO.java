package com.primasolutions.idp.authentication;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface AuthenticationInfoByLoginDAO extends JpaRepository<AuthenticationInfoByLogin, Long> {

    Set<AuthenticationInfoByLogin> findByLogin(String login);


}
