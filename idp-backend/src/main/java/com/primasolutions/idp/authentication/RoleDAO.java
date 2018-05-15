package com.primasolutions.idp.authentication;

import com.primasolutions.idp.constants.RoleCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleDAO extends JpaRepository<Role, RoleCode> {
}
