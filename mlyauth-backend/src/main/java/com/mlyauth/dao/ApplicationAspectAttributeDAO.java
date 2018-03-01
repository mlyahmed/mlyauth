package com.mlyauth.dao;

import com.mlyauth.constants.SPSAMLAttribute;
import com.mlyauth.domain.ApplicationAspectAttribute;
import com.mlyauth.domain.ApplicationAspectAttributeId;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface ApplicationAspectAttributeDAO extends CrudRepository<ApplicationAspectAttribute, ApplicationAspectAttributeId> {

    @Query("SELECT att from ApplicationAspectAttribute att WHERE att.id.applicationId = ?1 and att.id.aspectCode = ?2")
    List<ApplicationAspectAttribute> findByAppAndAspect(long applicationId, String aspectId);


    default Map<SPSAMLAttribute, ApplicationAspectAttribute> findAndIndex(long applicationId, String aspectId) {
        final List<ApplicationAspectAttribute> attributes = this.findByAppAndAspect(applicationId, aspectId);
        return attributes.stream().collect(Collectors.toMap(attr -> attr.getAttributeCode(), attr -> attr));
    }
}
