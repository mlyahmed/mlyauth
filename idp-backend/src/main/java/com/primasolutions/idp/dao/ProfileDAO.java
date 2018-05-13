package com.primasolutions.idp.dao;

import com.primasolutions.idp.constants.ProfileCode;
import com.primasolutions.idp.domain.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileDAO extends JpaRepository<Profile, ProfileCode> {
}
