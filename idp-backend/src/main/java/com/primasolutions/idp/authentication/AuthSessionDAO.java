package com.primasolutions.idp.authentication;

import org.springframework.data.repository.CrudRepository;

public interface AuthSessionDAO extends CrudRepository<AuthSession, Long> {
}
