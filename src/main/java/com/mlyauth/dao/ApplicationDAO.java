package com.mlyauth.dao;

import com.mlyauth.domain.Application;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface ApplicationDAO extends CrudRepository<Application, Long> {

    @Query("SELECT a FROM Application a WHERE a.appname = :appname")
    Application findByAppname(@Param("appname") String appname);
}
