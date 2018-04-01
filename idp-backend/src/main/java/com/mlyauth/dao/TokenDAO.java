package com.mlyauth.dao;

import com.mlyauth.domain.Token;
import org.springframework.data.repository.CrudRepository;

public interface TokenDAO extends CrudRepository<Token, Long> {

    Token findByChecksum(String checksum);

    Token findByStamp(String stamp);
}
