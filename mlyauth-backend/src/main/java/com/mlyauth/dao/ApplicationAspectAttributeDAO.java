package com.mlyauth.dao;

import com.mlyauth.domain.ApplicationAspectAttribute;
import com.mlyauth.domain.ApplicationAspectAttributeId;
import org.springframework.data.repository.CrudRepository;

public interface ApplicationAspectAttributeDAO extends CrudRepository<ApplicationAspectAttribute, ApplicationAspectAttributeId> {
}
