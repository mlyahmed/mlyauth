package com.mlyauth.dao;

import com.mlyauth.domain.Token;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TokenDAO extends CrudRepository<Token, Long> {

    @Query("SELECT t from Token t WHERE t.application.id = ?1 " +
            "and t.norm=com.mlyauth.constants.TokenNorm.JOSE " +
            "and t.type=com.mlyauth.constants.TokenType.REFRESH " +
            "and t.purpose=com.mlyauth.constants.TokenPurpose.DELEGATION " +
            "and t.status=com.mlyauth.constants.TokenStatus.READY")
    List<Token> findReadyJOSERefreshToken(long applicationId);

}
