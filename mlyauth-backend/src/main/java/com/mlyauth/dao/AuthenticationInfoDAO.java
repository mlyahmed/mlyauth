package com.mlyauth.dao;

import com.mlyauth.domain.AuthenticationInfo;
import org.springframework.data.repository.CrudRepository;

public interface AuthenticationInfoDAO extends CrudRepository<AuthenticationInfo, Long> {

    AuthenticationInfo findByLogin(String login);

}
