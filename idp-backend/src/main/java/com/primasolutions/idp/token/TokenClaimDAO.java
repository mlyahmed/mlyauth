package com.primasolutions.idp.token;

import org.springframework.data.repository.CrudRepository;

public interface TokenClaimDAO extends CrudRepository<TokenClaim, Long> {
}
