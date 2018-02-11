package com.mlyauth.dao;

import com.mlyauth.domain.ApplicationAspectAttribute;
import com.mlyauth.domain.ApplicationAspectAttributeId;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ApplicationAspectAttributeDAO extends CrudRepository<ApplicationAspectAttribute, ApplicationAspectAttributeId> {

    @Query("SELECT att from ApplicationAspectAttribute att WHERE att.id.applicationId = ?1 and att.id.aspectCode = ?2")
    List<ApplicationAspectAttribute> findByAppAndAspect(long applicationId, String aspectId);
}
