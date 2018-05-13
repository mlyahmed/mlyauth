package com.primasolutions.idp.dao;

import com.primasolutions.idp.constants.RoleCode;
import com.primasolutions.idp.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleDAO extends JpaRepository<Role, RoleCode> {
}
