package com.hohou.federation.idp.application;

import com.hohou.federation.idp.constants.AspectAttribute;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface ApplicationAspectAttributeDAO extends
        CrudRepository<AppAspAttr, ApplicationAspectAttributeId> {

    @Query("SELECT att from AppAspAttr att WHERE att.id.applicationId = ?1 and att.id.aspectCode = ?2")
    List<AppAspAttr> findByAppAndAspect(long applicationId, String aspectId);

    @Query("SELECT att from AppAspAttr att WHERE att.id.attributeCode = ?1 and att.value = ?2")
    AppAspAttr findByAttribute(String attribute, String value);

    default Map<AspectAttribute, AppAspAttr> findAndIndex(long applicationId, String aspectId) {
        final List<AppAspAttr> attributes = this.findByAppAndAspect(applicationId, aspectId);
        return attributes.stream().collect(Collectors.toMap(attr -> attr.getAttributeCode(), attr -> attr));
    }
}
