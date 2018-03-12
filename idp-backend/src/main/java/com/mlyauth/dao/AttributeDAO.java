package com.mlyauth.dao;

import com.mlyauth.domain.Attribute;
import org.springframework.data.repository.CrudRepository;

public interface AttributeDAO extends CrudRepository<Attribute, String> {
}
