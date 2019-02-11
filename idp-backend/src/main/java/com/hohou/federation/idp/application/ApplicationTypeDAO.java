package com.hohou.federation.idp.application;

import com.hohou.federation.idp.constants.ApplicationTypeCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationTypeDAO extends JpaRepository<ApplicationType, ApplicationTypeCode> {
}
