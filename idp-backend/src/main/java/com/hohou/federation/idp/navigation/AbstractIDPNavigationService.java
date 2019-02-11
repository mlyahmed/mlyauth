package com.hohou.federation.idp.navigation;

import com.google.common.base.Stopwatch;
import com.hohou.federation.idp.constants.Direction;
import com.hohou.federation.idp.context.IContext;
import com.hohou.federation.idp.token.TokenDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public abstract class AbstractIDPNavigationService implements IDPNavigationService {
    protected static final Logger LOGGER = LoggerFactory.getLogger(IDPNavigationService.class);

    @Autowired
    private TokenDAO tokenDAO;

    @Autowired
    private NavigationDAO navigationDAO;

    @Autowired
    private IContext context;

    public NavigationBean newNavigation(final String appname) {
        final Stopwatch started = Stopwatch.createStarted();
        final NavigationBean navigationBean = process(appname);
        traceNavigation(navigationBean, started.elapsed(MILLISECONDS));
        return navigationBean;
    }

    private void traceNavigation(final NavigationBean navigationBean, final long consumedTime) {
        try {
            final Navigation navigation = navigationDAO.save(buildNavigation(navigationBean, consumedTime));
            navigationBean.setId(navigation.getId());
        } catch (Exception e) {
            LOGGER.error("Error when tracing navigation : ", e);
        }
    }

    private Navigation buildNavigation(final NavigationBean navigationBean, final long consumedTime) {
        return Navigation.newInstance()
                .setCreatedAt(new Date())
                .setTimeConsumed(consumedTime)
                .setAttributes(buildNavigationAttributes(navigationBean))
                .setDirection(Direction.OUTBOUND)
                .setTargetURL(navigationBean.getTarget())
                .setToken(tokenDAO.findById(navigationBean.getTokenId()).orElse(null))
                .setSession(context.getAuthenticationSession());
    }

    private Set<NavigationAttribute> buildNavigationAttributes(final NavigationBean navigationBean) {
        return navigationBean.getAttributes().stream()
                .map(att -> NavigationAttribute.newInstance().setCode(att.getCode())
                        .setAlias(att.getAlias()).setValue(att.getValue())).collect(Collectors.toSet());
    }

    abstract NavigationBean process(String appname);

}
