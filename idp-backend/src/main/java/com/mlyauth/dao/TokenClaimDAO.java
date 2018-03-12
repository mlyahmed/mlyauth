package com.mlyauth.dao;

import com.mlyauth.domain.TokenClaim;
import org.springframework.data.repository.CrudRepository;

public interface TokenClaimDAO extends CrudRepository<TokenClaim, Long> {
}
