package com.primasolutions.idp.dao;

import com.primasolutions.idp.domain.Application;
import org.springframework.data.repository.CrudRepository;

public interface ApplicationDAO extends CrudRepository<Application, Long> {

    Application findByAppname(String appname);

}
