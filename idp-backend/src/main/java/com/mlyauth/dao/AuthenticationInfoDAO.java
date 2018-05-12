package com.mlyauth.dao;

import com.mlyauth.domain.AuthenticationInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthenticationInfoDAO extends JpaRepository<AuthenticationInfo, Long> {

}
