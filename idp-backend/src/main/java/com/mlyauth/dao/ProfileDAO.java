package com.mlyauth.dao;

import com.mlyauth.constants.ProfileCode;
import com.mlyauth.domain.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileDAO extends JpaRepository<Profile, ProfileCode> {
}
