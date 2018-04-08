package com.mlyauth.dao;

import com.mlyauth.constants.RoleCode;
import com.mlyauth.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleDAO extends JpaRepository<Role, RoleCode> {
}
