package com.primasolutions.idp.dao;

import com.primasolutions.idp.constants.ApplicationTypeCode;
import com.primasolutions.idp.domain.ApplicationType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationTypeDAO extends JpaRepository<ApplicationType, ApplicationTypeCode> {
}
