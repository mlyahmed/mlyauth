package com.hohou.federation.idp.authentication;

import com.hohou.federation.idp.constants.ProfileCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileDAO extends JpaRepository<Profile, ProfileCode> {
}
