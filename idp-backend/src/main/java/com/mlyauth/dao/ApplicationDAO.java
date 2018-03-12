package com.mlyauth.dao;

import com.mlyauth.domain.Application;
import org.springframework.data.repository.CrudRepository;

public interface ApplicationDAO extends CrudRepository<Application, Long> {

    Application findByAppname(String appname);

}
