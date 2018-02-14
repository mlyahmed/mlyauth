package com.mlyauth.security.context;

import com.mlyauth.domain.AuthenticationInfo;
import com.mlyauth.domain.Person;

import javax.servlet.http.HttpSession;
import java.util.Map;

public interface IContext {

    HttpSession getSession();

    Person getPerson();

    AuthenticationInfo getAuthenticationInfo();

    Map<String, String> getAttributes();

    String getAttribute(String key);

    boolean putAttribute(String key, String value);

}
