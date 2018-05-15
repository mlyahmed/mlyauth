package com.primasolutions.idp.context;

import com.primasolutions.idp.application.Application;
import com.primasolutions.idp.constants.AuthenticationSessionStatus;
import com.primasolutions.idp.dao.AuthenticationSessionDAO;
import com.primasolutions.idp.domain.AuthenticationInfo;
import com.primasolutions.idp.domain.AuthenticationSession;
import com.primasolutions.idp.domain.Profile;
import com.primasolutions.idp.person.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import static org.springframework.web.context.request.RequestAttributes.REFERENCE_SESSION;

@Configuration
public class ContextHolder implements IContextHolder {

    private static final String CONTEXT_ID_ATTRIBUTE = "IDP_CONTEXT_ID";

    private final Map<String, IContext> contexts = new WeakHashMap<>();

    @Autowired
    protected IContextIdGenerator idGenerator;

    @Autowired
    protected AuthenticationSessionDAO sauthSessionDAO;

    @Override
    public String getId() {
        return getContext() != null ? getContext().getId() : null;
    }

    @Override
    public IContext getContext() {
        return contexts.get(String.valueOf(getSession().getAttribute(CONTEXT_ID_ATTRIBUTE)));
    }

    @Override
    public IContext newPersonContext(final Person person) {
        final String contextId = newId();
        closeCurrentSession();
        final Context context = new Context(contextId, person, newAuthSession(person, contextId));
        contexts.put(contextId, context);
        return context;
    }

    @Override
    public IContext newApplicationContext(final Application application) {
        final String contextId = newId();
        closeCurrentSession();
        final Context context = new Context(contextId, application, newAuthSession(application, contextId));
        contexts.put(contextId, context);
        return context;
    }

    @Override
    public void reset() {
        closeCurrentSession();
        contexts.remove(getId());
    }

    @Override
    public HttpSession getSession() {
        return (HttpSession) RequestContextHolder.getRequestAttributes().resolveReference(REFERENCE_SESSION);
    }

    @Override
    public Person getPerson() {
        return getContext() != null ? getContext().getPerson() : null;
    }

    @Override
    public Application getApplication() {
        return getContext() != null ? getContext().getApplication() : null;
    }

    @Override
    public AuthenticationInfo getAuthenticationInfo() {
        if (isAPerson()) {
            return getPerson() != null ? getPerson().getAuthenticationInfo() : null;
        } else {
            return getApplication() != null ? getApplication().getAuthenticationInfo() : null;
        }
    }

    @Override
    public AuthenticationSession getAuthenticationSession() {
        return getContext() != null ? getContext().getAuthenticationSession() : null;
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
    public Set<Profile> getProfiles() {
        return getContext() != null ? getContext().getProfiles() : Collections.emptySet();
    }

    @Override
    public Map<String, String> getAttributes() {
        return getContext() != null ? getContext().getAttributes() : Collections.emptyMap();
    }

    @Override
    public String getAttribute(final String key) {
        return getContext() != null ? getContext().getAttribute(key) : null;
    }

    @Override
    public boolean putAttribute(final String key, final String value) {
        return getContext() != null && getContext().putAttribute(key, value);
    }

    @Override
    public boolean isAnApplication() {
        return this.getApplication() != null;
    }

    @Override
    public boolean isAPerson() {
        return this.getPerson() != null;
    }

    private void closeCurrentSession() {
        final AuthenticationSession currentAuthSession = getAuthenticationSession();
        if (currentAuthSession != null) {
            currentAuthSession.setStatus(AuthenticationSessionStatus.CLOSED);
            currentAuthSession.setClosedAt(new Date());
            sauthSessionDAO.save(currentAuthSession);
        }
    }

    private AuthenticationSession newAuthSession(final Person person, final String contextId) {
        AuthenticationSession authSession = AuthenticationSession.newInstance()
                .setContextId(contextId)
                .setStatus(AuthenticationSessionStatus.ACTIVE)
                .setCreatedAt(new Date())
                .setAuthenticationInfo(person.getAuthenticationInfo());
        return sauthSessionDAO.save(authSession);
    }

    private AuthenticationSession newAuthSession(final Application application, final String contextId) {
        AuthenticationSession authSession = AuthenticationSession.newInstance()
                .setContextId(contextId)
                .setStatus(AuthenticationSessionStatus.ACTIVE)
                .setCreatedAt(new Date())
                .setAuthenticationInfo(application.getAuthenticationInfo());
        return sauthSessionDAO.save(authSession);
    }

    private String newId() {
        return getContext() != null ? getContext().getId() : idGenerator.generateId();
    }

    private static class Context implements IContext {


        private final String id;
        private final Person person;
        private final Application application;
        private final AuthenticationSession authSession;
        private Map<String, String> attributes = new HashMap<>();

        protected Context(final String id, final Person person, final AuthenticationSession authSession) {
            this.id = id;
            this.person = person;
            this.application = null;
            this.authSession = authSession;
            getSession().setAttribute(CONTEXT_ID_ATTRIBUTE, id);
        }

        protected Context(final String id, final Application application, final AuthenticationSession authSession) {
            this.id = id;
            this.person = null;
            this.application = application;
            this.authSession = authSession;
            getSession().setAttribute(CONTEXT_ID_ATTRIBUTE, id);
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public HttpSession getSession() {
            return (HttpSession) RequestContextHolder.getRequestAttributes().resolveReference(REFERENCE_SESSION);
        }

        @Override
        public Person getPerson() {
            return person;
        }

        @Override
        public Application getApplication() {
            return application;
        }

        @Override
        public AuthenticationInfo getAuthenticationInfo() {
            return isAPerson() ? person.getAuthenticationInfo() : application.getAuthenticationInfo();
        }

        @Override
        public AuthenticationSession getAuthenticationSession() {
            return authSession;
        }

        @Override
        public String getLogin() {
            return getAuthenticationInfo().getLogin();
        }

        @Override
        public String getPassword() {
            return getAuthenticationInfo().getPassword();
        }

        @Override
        public Set<Profile> getProfiles() {
            final Set<Profile> profiles = isAPerson() ? person.getProfiles() : application.getProfiles();
            return profiles != null ? profiles : Collections.emptySet();
        }

        @Override
        public Map<String, String> getAttributes() {
            return new HashMap<>(attributes);
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
            return application != null;
        }

        @Override
        public boolean isAPerson() {
            return person != null;
        }
    }
}
