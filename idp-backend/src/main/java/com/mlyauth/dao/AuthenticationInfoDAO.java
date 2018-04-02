package com.mlyauth.dao;

import com.mlyauth.domain.AuthenticationInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface AuthenticationInfoDAO extends JpaRepository<AuthenticationInfo, Long> {

    AuthenticationInfo findByLogin(String login);

}
