package com.mlyauth.dao;

import com.mlyauth.domain.AuthenticationSession;
import org.springframework.data.repository.CrudRepository;

public interface AuthenticationSessionDAO extends CrudRepository<AuthenticationSession, Long> {
}
