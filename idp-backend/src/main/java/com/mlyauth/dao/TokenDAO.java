package com.mlyauth.dao;

import com.mlyauth.constants.TokenNorm;
import com.mlyauth.constants.TokenType;
import com.mlyauth.domain.Application;
import com.mlyauth.domain.Token;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TokenDAO extends CrudRepository<Token, Long> {

    List<Token> findByApplicationAndNormAndType(Application application, TokenNorm norm, TokenType type);

    Token findByChecksum(String checksum);
}
