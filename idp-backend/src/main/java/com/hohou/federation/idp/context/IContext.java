package com.hohou.federation.idp.context;

import com.hohou.federation.idp.application.Application;
import com.hohou.federation.idp.authentication.AuthInfo;
import com.hohou.federation.idp.authentication.AuthSession;
import com.hohou.federation.idp.authentication.Profile;
import com.hohou.federation.idp.person.model.Person;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.Set;

public interface IContext {

    String getId();

    HttpSession getSession();

    Person getPerson();

    Application getApplication();

    AuthInfo getAuthenticationInfo();

    AuthSession getAuthenticationSession();

    String getLogin();

    String getPassword();

    Set<Profile> getProfiles();

    Map<String, String> getAttributes();

    String getAttribute(String key);

    boolean putAttribute(String key, String value);

    boolean isAnApplication();

    boolean isAPerson();

}
