package com.primasolutions.idp.authentication;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthenticationInfoDAO extends JpaRepository<AuthenticationInfo, Long> {

}
