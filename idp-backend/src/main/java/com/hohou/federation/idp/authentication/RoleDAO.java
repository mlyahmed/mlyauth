package com.hohou.federation.idp.authentication;

import com.hohou.federation.idp.constants.RoleCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleDAO extends JpaRepository<Role, RoleCode> {
}
