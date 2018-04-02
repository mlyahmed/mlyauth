package com.mlyauth.dao;

import com.mlyauth.domain.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface TokenDAO extends JpaRepository<Token, Long> {

    Token findByChecksum(String checksum);

    Token findByStamp(String stamp);
}
