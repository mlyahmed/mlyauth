package com.primasolutions.idp.authentication;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface AuthInfoByLoginDAO extends JpaRepository<AuthInfoByLogin, Long> {

    Set<AuthInfoByLogin> findByLogin(String login);


}
