package com.hohou.federation.idp.context.mocks;

import com.hohou.federation.idp.application.Application;
import com.hohou.federation.idp.authentication.AuthInfo;
import com.hohou.federation.idp.authentication.AuthSession;
import com.hohou.federation.idp.authentication.Profile;
import com.hohou.federation.idp.context.IContext;
import com.hohou.federation.idp.person.model.Person;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MockContext implements IContext {

    private Map<String, String> attributes = new HashMap<>();

    @Override
    public String getId() {
        return null;
    }

    @Override
    public HttpSession getSession() {
        return null;
    }

    @Override
    public Person getPerson() {
        return null;
    }

    @Override
    public Application getApplication() {
        return null;
    }

    @Override
    public AuthInfo getAuthenticationInfo() {
        return null;
    }

    @Override
    public AuthSession getAuthenticationSession() {
        return null;
    }

    @Override
    public String getLogin() {
        return null;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public Set<Profile> getProfiles() {
        return null;
    }

    @Override
    public Map<String, String> getAttributes() {
        return attributes;
    }

    @Override
    public String getAttribute(final String key) {
        return attributes.get(key);
    }

    @Override
    public boolean putAttribute(final String key, final String value) {
        return String.valueOf(attributes.get(key)).equals(attributes.put(key, value));
    }

    @Override
    public boolean isAnApplication() {
        return false;
    }

    @Override
    public boolean isAPerson() {
        return false;
    }

    public void resetMock() {
        attributes.clear();
    }
}
