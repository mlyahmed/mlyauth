package com.primasolutions.idp.dao;

import com.primasolutions.idp.domain.Attribute;
import org.springframework.data.repository.CrudRepository;

public interface AttributeDAO extends CrudRepository<Attribute, String> {
}
