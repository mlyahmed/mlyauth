package com.primasolutions.idp.authentication;

import com.primasolutions.idp.constants.ProfileCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileDAO extends JpaRepository<Profile, ProfileCode> {
}
