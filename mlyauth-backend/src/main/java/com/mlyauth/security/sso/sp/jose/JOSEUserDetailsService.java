package com.mlyauth.security.sso.sp.jose;

import com.mlyauth.security.context.IDPUser;
import com.mlyauth.token.jose.JOSEAccessToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface JOSEUserDetailsService {

    IDPUser loadUserByJOSE(JOSEAccessToken credential) throws UsernameNotFoundException;

}
