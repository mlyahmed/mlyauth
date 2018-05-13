package com.primasolutions.idp.dao;

import com.primasolutions.idp.domain.AuthenticationSession;
import org.springframework.data.repository.CrudRepository;

public interface AuthenticationSessionDAO extends CrudRepository<AuthenticationSession, Long> {
}
