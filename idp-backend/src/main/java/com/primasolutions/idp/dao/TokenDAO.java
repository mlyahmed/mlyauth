package com.primasolutions.idp.dao;

import com.primasolutions.idp.domain.Token;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenDAO extends JpaRepository<Token, Long> {

    Token findByChecksum(String checksum);

    Token findByStamp(String stamp);
}
