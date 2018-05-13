package com.primasolutions.idp.dao;

import com.primasolutions.idp.domain.AutoNavigation;
import com.primasolutions.idp.domain.Role;
import org.springframework.data.repository.CrudRepository;

public interface AutoNavigationDAO extends CrudRepository<AutoNavigation, Long> {

    AutoNavigation findByRole(Role role);

}
