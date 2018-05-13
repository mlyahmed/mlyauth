package com.primasolutions.idp.dao;

import com.primasolutions.idp.domain.TokenClaim;
import org.springframework.data.repository.CrudRepository;

public interface TokenClaimDAO extends CrudRepository<TokenClaim, Long> {
}
