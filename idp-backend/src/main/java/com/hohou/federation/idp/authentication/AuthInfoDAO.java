package com.hohou.federation.idp.authentication;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthInfoDAO extends JpaRepository<AuthInfo, Long> {

}
