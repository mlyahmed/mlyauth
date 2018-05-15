package com.primasolutions.idp.application;

import com.primasolutions.idp.constants.AspectType;
import org.springframework.data.repository.CrudRepository;

public interface AuthAspectDAO extends CrudRepository<AuthAspect, AspectType> {
}
