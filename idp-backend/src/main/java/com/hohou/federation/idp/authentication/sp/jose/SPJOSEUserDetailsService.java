package com.hohou.federation.idp.authentication.sp.jose;

import com.hohou.federation.idp.context.IDPUser;
import com.hohou.federation.idp.token.jose.JOSEAccessToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface SPJOSEUserDetailsService {

    IDPUser loadUserByJOSE(JOSEAccessToken credential) throws UsernameNotFoundException;

}
