package com.mlyauth.security.context;

import com.mlyauth.domain.AuthenticationInfo;
import com.mlyauth.domain.Person;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

@Configuration
public class ContextHolder implements IContextHolder {

    private final Map<String, IContext> contexts = new WeakHashMap<>();

    @Override
    public IContext getContext() {
        return contexts.get(getSession().getId());
    }

    @Override
    public void setContext(IContext context) {
        contexts.put(getSession().getId(), context);
    }

    @Override
    public IContext newContext(Person person) {
        final Context context = new Context(person);
        this.setContext(context);
        return context;
    }

    @Override
    public void reset() {
        contexts.remove(getSession().getId());
    }

    @Override
    public HttpSession getSession() {
        return (HttpSession) RequestContextHolder.getRequestAttributes().resolveReference(RequestAttributes.REFERENCE_SESSION);
    }

    @Override
    public Person getPerson() {
        return getContext() != null ? getContext().getPerson() : null;
    }

    @Override
    public AuthenticationInfo getAuthenticationInfo() {
        return getPerson() != null ? getPerson().getAuthenticationInfo() : null;
    }

    @Override
    public Map<String, String> getAttributes() {
        return getContext() != null ? getContext().getAttributes() : Collections.emptyMap();
    }

    @Override
    public String getAttribute(String key) {
        return getContext() != null ? getContext().getAttribute(key) : null;
    }

    @Override
    public boolean putAttribute(String key, String value) {
        return getContext() != null && getContext().putAttribute(key, value);
    }

    private static class Context implements IContext {

        private final Person person;
        private Map<String, String> attributes = new HashMap<>();

        protected Context(Person person) {
            this.person = person;
        }

        @Override
        public HttpSession getSession() {
            return (HttpSession) RequestContextHolder.getRequestAttributes().resolveReference(RequestAttributes.REFERENCE_SESSION);
        }

        @Override
        public Person getPerson() {
            return person;
        }

        @Override
        public AuthenticationInfo getAuthenticationInfo() {
            return person.getAuthenticationInfo();
        }

        @Override
        public Map<String, String> getAttributes() {
            return new HashMap<>(attributes);
        }

        @Override
        public String getAttribute(String key) {
            return attributes.get(key);
        }

        @Override
        public boolean putAttribute(String key, String value) {
            return String.valueOf(attributes.get(key)).equals(attributes.put(key, value));
        }
    }
}
