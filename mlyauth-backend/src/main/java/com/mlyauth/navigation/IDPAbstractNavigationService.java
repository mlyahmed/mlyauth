package com.mlyauth.navigation;

import com.mlyauth.beans.NavigationBean;
import com.mlyauth.context.IContext;
import com.mlyauth.dao.NavigationDAO;
import com.mlyauth.dao.TokenDAO;
import com.mlyauth.domain.Navigation;
import com.mlyauth.domain.NavigationAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import static com.mlyauth.constants.NavigationDirection.OUTBOUND;

public abstract class IDPAbstractNavigationService implements IDPNavigationService {
    protected final static Logger logger = LoggerFactory.getLogger(IDPNavigationService.class);

    @Autowired
    private TokenDAO tokenDAO;

    @Autowired
    private NavigationDAO navigationDAO;

    @Autowired
    private IContext context;

    public NavigationBean newNavigation(String appname) {
        long start = System.currentTimeMillis();
        final NavigationBean navigationBean = process(appname);
        traceNavigation(navigationBean, System.currentTimeMillis() - start);
        return navigationBean;
    }

    private void traceNavigation(NavigationBean navigationBean, long consumedTime) {
        try {
            navigationDAO.save(buildNavigation(navigationBean, consumedTime));
        } catch (Exception e) {
            logger.error("Error when tracing navigation : ", e);
        }
    }

    private Navigation buildNavigation(NavigationBean navigationBean, long consumedTime) {
        return Navigation.newInstance()
                .setCreatedAt(new Date())
                .setTimeConsumed(consumedTime)
                .setAttributes(buildNavigationAttributes(navigationBean))
                .setDirection(OUTBOUND)
                .setTargetURL(navigationBean.getTarget())
                .setToken(tokenDAO.findOne(navigationBean.getTokenId()))
                .setSession(context.getAuthenticationSession());
    }

    private Set<NavigationAttribute> buildNavigationAttributes(NavigationBean navigationBean) {
        return navigationBean.getAttributes().stream()
                .map(att -> NavigationAttribute.newInstance().setCode(att.getCode())
                        .setAlias(att.getAlias()).setValue(att.getValue())).collect(Collectors.toSet());
    }

    abstract NavigationBean process(String appname);

}
