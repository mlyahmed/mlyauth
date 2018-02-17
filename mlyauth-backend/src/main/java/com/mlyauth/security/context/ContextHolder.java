package com.mlyauth.security.context;

import com.mlyauth.domain.AuthenticationInfo;
import com.mlyauth.domain.Person;
import com.mlyauth.exception.ContextAlreadyLoaded;
import org.springframework.beans.factory.annotation.Autowired;
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

    private static final String CONTEXT_ID_ATTRIBUTE = "IDP_CONTEXT_ID";

    private final Map<String, IContext> contexts = new WeakHashMap<>();

    @Autowired
    protected IContextIdGenerator idGenerator;

    @Override
    public String getId() {
        return getContext() != null ? getContext().getId() : null;
    }

    @Override
    public IContext getContext() {
        return contexts.get(String.valueOf(getSession().getAttribute(CONTEXT_ID_ATTRIBUTE)));
    }

    @Override
    public IContext newContext(Person person) {
        if (getContext() != null)
            throw new ContextAlreadyLoaded();

        final Context context = new Context(idGenerator.generateId(), person);
        contexts.put(context.getId(), context);
        return context;
    }

    @Override
    public void reset() {
        contexts.remove(getId());
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
    public String getLogin() {
        return getAuthenticationInfo() != null ? getAuthenticationInfo().getLogin() : null;
    }

    @Override
    public String getPassword() {
        return getAuthenticationInfo() != null ? getAuthenticationInfo().getPassword() : null;
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

        private final String id;
        private final Person person;
        private Map<String, String> attributes = new HashMap<>();

        protected Context(String id, Person person) {
            this.id = id;
            this.person = person;
            getSession().setAttribute(CONTEXT_ID_ATTRIBUTE, id);
        }

        @Override
        public String getId() {
            return id;
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
        public String getLogin() {
            return person.getAuthenticationInfo().getLogin();
        }

        @Override
        public String getPassword() {
            return person.getAuthenticationInfo().getPassword();
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
