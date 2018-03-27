package com.mlyauth.sp.saml;

import com.mlyauth.constants.AspectAttribute;
import com.mlyauth.dao.ApplicationAspectAttributeDAO;
import com.mlyauth.domain.Application;
import com.mlyauth.domain.ApplicationAspectAttribute;
import com.mlyauth.exception.BadSPSAMLAspectAttributeValueException;
import com.mlyauth.exception.MissingSPSAMLAspectAttributeException;
import com.mlyauth.exception.NotSPSAMLApplicationException;
import com.mlyauth.token.saml.SAMLHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.List;

import static com.mlyauth.constants.AspectAttribute.*;
import static com.mlyauth.constants.AspectType.SP_SAML;

@Component
public class SPSAMLAspectValidator implements ISPSAMLAspectValidator {

    @Autowired
    private ApplicationAspectAttributeDAO appAspectAttrDAO;

    @Autowired
    private SAMLHelper samlHelper;

    @Override
    public void validate(Application application) {
        Assert.notNull(application, "The application argument is null");
        validateIsAnSPSAML(application);
        List<ApplicationAspectAttribute> attributes = appAspectAttrDAO.findByAppAndAspect(application.getId(), SP_SAML.name());
        validateAttributeExists(attributes, SP_SAML_ENTITY_ID);
        validateURL(validateAttributeExists(attributes, SP_SAML_SSO_URL));
        validateCertificate(validateAttributeExists(attributes, SP_SAML_ENCRYPTION_CERTIFICATE));

    }

    private void validateIsAnSPSAML(Application application) {
        if (application.getAspects() == null || !application.getAspects().contains(SP_SAML))
            throw NotSPSAMLApplicationException.newInstance();
    }

    private ApplicationAspectAttribute validateAttributeExists(List<ApplicationAspectAttribute> attributes, AspectAttribute attribute) {
        final ApplicationAspectAttribute found = attributes.stream()
                .filter(attr -> attribute == attr.getAttributeCode())
                .findFirst().orElse(null);

        if (found == null)
            throw MissingSPSAMLAspectAttributeException.newInstance();

        if (StringUtils.isBlank(found.getValue()))
            throw BadSPSAMLAspectAttributeValueException.newInstance();

        return found;
    }

    private void validateURL(ApplicationAspectAttribute ssoUrl) {
        UrlValidator urlValidator = new UrlValidator(new String[]{"http", "https"}, UrlValidator.ALLOW_LOCAL_URLS);
        if (!urlValidator.isValid(ssoUrl.getValue()))
            throw BadSPSAMLAspectAttributeValueException.newInstance();
    }

    private void validateCertificate(ApplicationAspectAttribute certificateAtt) {
        try {
            samlHelper.toX509Certificate(certificateAtt.getValue());
        } catch (Exception e) {
            throw BadSPSAMLAspectAttributeValueException.newInstance(e);
        }
    }

}
