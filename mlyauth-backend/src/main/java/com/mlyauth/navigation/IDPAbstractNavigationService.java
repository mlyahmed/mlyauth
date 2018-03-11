package com.mlyauth.navigation;

import com.mlyauth.beans.NavigationBean;
import com.mlyauth.context.IContext;
import com.mlyauth.dao.NavigationDAO;
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
    private NavigationDAO navigationDAO;

    @Autowired
    private IContext context;

    public NavigationBean newNavigation(String appname) {
        long start = System.currentTimeMillis();
        final NavigationBean navigationBean = process(appname);
        long consumedTime = System.currentTimeMillis() - start;

        try {
            final Set<NavigationAttribute> attributes = navigationBean.getAttributes().stream()
                    .map(att -> NavigationAttribute.newInstance().setCode(att.getCode())
                            .setAlias(att.getAlias()).setValue(att.getValue())).collect(Collectors.toSet());

            Navigation navigation = Navigation.newInstance()
                    .setCreatedAt(new Date())
                    .setTimeConsumed(consumedTime)
                    .setAttributes(attributes)
                    .setDirection(OUTBOUND)
                    .setTargetURL(navigationBean.getTarget())
                    .setSession(context.getAuthenticationSession());

            navigationDAO.save(navigation);
        } catch (Exception e) {
            logger.error("Error when tracing navigation : ", e);
        }

        return navigationBean;
    }

    abstract NavigationBean process(String appname);

}
