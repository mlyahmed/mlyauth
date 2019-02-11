package com.hohou.federation.idp.application;

import org.springframework.data.repository.CrudRepository;

public interface ApplicationDAO extends CrudRepository<Application, Long> {

    Application findByAppname(String appname);

}
