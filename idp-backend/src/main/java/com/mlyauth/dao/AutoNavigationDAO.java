package com.mlyauth.dao;

import com.mlyauth.domain.AutoNavigation;
import com.mlyauth.domain.Role;
import org.springframework.data.repository.CrudRepository;

public interface AutoNavigationDAO extends CrudRepository<AutoNavigation, Long> {

    AutoNavigation findByRole(Role role);

}
