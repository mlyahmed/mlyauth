package com.hohou.federation.idp.navigation;

import com.hohou.federation.idp.authentication.Role;
import org.springframework.data.repository.CrudRepository;

public interface AutoNavigationDAO extends CrudRepository<AutoNavigation, Long> {

    AutoNavigation findByRole(Role role);

}
