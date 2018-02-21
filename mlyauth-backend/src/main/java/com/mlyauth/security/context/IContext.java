package com.mlyauth.security.context;

import com.mlyauth.domain.AuthenticationInfo;
import com.mlyauth.domain.AuthenticationSession;
import com.mlyauth.domain.Person;
import com.mlyauth.domain.Profile;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.Set;

public interface IContext {

    String getId();

    HttpSession getSession();

    Person getPerson();

    AuthenticationInfo getAuthenticationInfo();

    AuthenticationSession getAuthenticationSession();

    String getLogin();

    String getPassword();

    Set<Profile> getProfiles();

    Map<String, String> getAttributes();

    String getAttribute(String key);

    boolean putAttribute(String key, String value);

}
