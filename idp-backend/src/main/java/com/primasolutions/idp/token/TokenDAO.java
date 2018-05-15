package com.primasolutions.idp.token;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenDAO extends JpaRepository<Token, Long> {

    Token findByChecksum(String checksum);

    Token findByStamp(String stamp);
}
