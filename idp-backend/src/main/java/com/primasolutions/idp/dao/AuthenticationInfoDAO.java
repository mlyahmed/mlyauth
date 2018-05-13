package com.primasolutions.idp.dao;

import com.primasolutions.idp.domain.AuthenticationInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthenticationInfoDAO extends JpaRepository<AuthenticationInfo, Long> {

}
