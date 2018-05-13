package com.primasolutions.idp.dao;

import com.primasolutions.idp.constants.AspectType;
import com.primasolutions.idp.domain.AuthAspect;
import org.springframework.data.repository.CrudRepository;

public interface AuthAspectDAO extends CrudRepository<AuthAspect, AspectType> {
}
