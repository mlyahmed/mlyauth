package com.primasolutions.idp.navigation;

import com.primasolutions.idp.domain.Role;
import org.springframework.data.repository.CrudRepository;

public interface AutoNavigationDAO extends CrudRepository<AutoNavigation, Long> {

    AutoNavigation findByRole(Role role);

}
