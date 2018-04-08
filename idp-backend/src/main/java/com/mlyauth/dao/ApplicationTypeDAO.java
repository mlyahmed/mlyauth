package com.mlyauth.dao;

import com.mlyauth.constants.ApplicationTypeCode;
import com.mlyauth.domain.ApplicationType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationTypeDAO extends JpaRepository<ApplicationType, ApplicationTypeCode> {
}
