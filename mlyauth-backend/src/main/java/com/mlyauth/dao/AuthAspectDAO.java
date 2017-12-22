package com.mlyauth.dao;

import com.mlyauth.constants.AuthAspectType;
import com.mlyauth.domain.AuthAspect;
import org.springframework.data.repository.CrudRepository;

public interface AuthAspectDAO  extends CrudRepository<AuthAspect, AuthAspectType> {
}
