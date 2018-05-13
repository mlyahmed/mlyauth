package com.primasolutions.idp.dao;

import com.primasolutions.idp.domain.AuthenticationInfoByLogin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface AuthenticationInfoByLoginDAO extends JpaRepository<AuthenticationInfoByLogin, Long> {

    Set<AuthenticationInfoByLogin> findByLogin(String login);


}
