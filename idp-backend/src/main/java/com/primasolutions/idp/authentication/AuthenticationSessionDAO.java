package com.primasolutions.idp.authentication;

import org.springframework.data.repository.CrudRepository;

public interface AuthenticationSessionDAO extends CrudRepository<AuthenticationSession, Long> {
}
