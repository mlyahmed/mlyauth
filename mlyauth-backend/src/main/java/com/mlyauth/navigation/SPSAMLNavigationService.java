package com.mlyauth.navigation;

import com.mlyauth.beans.AttributeBean;
import com.mlyauth.beans.NavigationBean;
import com.mlyauth.constants.AspectType;
import com.mlyauth.dao.ApplicationDAO;
import com.mlyauth.domain.Application;
import com.mlyauth.exception.ApplicationNotFoundException;
import com.mlyauth.exception.NotSPSAMLApplicationException;
import com.mlyauth.token.IDPToken;
import com.mlyauth.token.saml.SAMLAccessTokenProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.LinkedList;

import static com.mlyauth.beans.AttributeBean.newAttribute;
import static com.mlyauth.constants.AspectType.SP_SAML;
import static com.mlyauth.constants.NavigationAttribute.SAML_RESPONSE;

@Service
public class SPSAMLNavigationService implements ISPNavigationService {

    @Autowired
    private ApplicationDAO applicationDAO;

    @Autowired
    private SAMLAccessTokenProducer responseGenerator;

    @Override
    public NavigationBean newNavigation(String appname) {
        Collection<AttributeBean> navigationAttributes = new LinkedList<>();
        NavigationBean navigation = new NavigationBean();
        checkApplication(applicationDAO.findByAppname(appname));
        final IDPToken token = responseGenerator.produce(applicationDAO.findByAppname(appname));
        navigationAttributes.add(newAttribute(SAML_RESPONSE.getValue()).setValue(token.serialize()));
        navigation.setTarget(token.getTargetURL());
        navigation.setAttributes(navigationAttributes);
        return navigation;
    }

    @Override
    public AspectType getSupportedAspect() {
        return SP_SAML;
    }

    private void checkApplication(Application application) {
        if (application == null)
            throw ApplicationNotFoundException.newInstance();

        if (!application.getAspects().contains(SP_SAML))
            throw NotSPSAMLApplicationException.newInstance();
    }
}
