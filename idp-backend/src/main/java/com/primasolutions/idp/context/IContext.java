package com.primasolutions.idp.context;

import com.primasolutions.idp.application.Application;
import com.primasolutions.idp.domain.AuthenticationInfo;
import com.primasolutions.idp.domain.AuthenticationSession;
import com.primasolutions.idp.domain.Profile;
import com.primasolutions.idp.person.Person;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.Set;

public interface IContext {

    String getId();

    HttpSession getSession();

    Person getPerson();

    Application getApplication();

    AuthenticationInfo getAuthenticationInfo();

    AuthenticationSession getAuthenticationSession();

    String getLogin();

    String getPassword();

    Set<Profile> getProfiles();

    Map<String, String> getAttributes();

    String getAttribute(String key);

    boolean putAttribute(String key, String value);

    boolean isAnApplication();

    boolean isAPerson();

}
