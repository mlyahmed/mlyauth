package com.hohou.federation.idp.application;

import com.hohou.federation.idp.constants.AspectType;
import org.springframework.data.repository.CrudRepository;

public interface AuthAspectDAO extends CrudRepository<AuthAspect, AspectType> {
}
