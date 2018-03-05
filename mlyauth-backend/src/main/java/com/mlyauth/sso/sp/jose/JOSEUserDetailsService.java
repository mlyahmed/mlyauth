package com.mlyauth.sso.sp.jose;

import com.mlyauth.context.IDPUser;
import com.mlyauth.token.jose.JOSEAccessToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface JOSEUserDetailsService {

    IDPUser loadUserByJOSE(JOSEAccessToken credential) throws UsernameNotFoundException;

}
