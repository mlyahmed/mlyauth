package com.primasolutions.idp.authentication.sp.jose;

import com.primasolutions.idp.context.IDPUser;
import com.primasolutions.idp.token.jose.JOSEAccessToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface SPJOSEUserDetailsService {

    IDPUser loadUserByJOSE(JOSEAccessToken credential) throws UsernameNotFoundException;

}
