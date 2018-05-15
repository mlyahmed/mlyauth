package com.primasolutions.idp.application;

import com.primasolutions.idp.constants.ApplicationTypeCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationTypeDAO extends JpaRepository<ApplicationType, ApplicationTypeCode> {
}
