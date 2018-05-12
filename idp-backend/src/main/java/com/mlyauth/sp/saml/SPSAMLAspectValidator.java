package com.mlyauth.sp.saml;

import com.mlyauth.constants.AspectAttribute;
import com.mlyauth.dao.ApplicationAspectAttributeDAO;
import com.mlyauth.domain.AppAspAttr;
import com.mlyauth.domain.Application;
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

import static com.mlyauth.constants.AspectAttribute.SP_SAML_ENCRYPTION_CERTIFICATE;
import static com.mlyauth.constants.AspectAttribute.SP_SAML_ENTITY_ID;
import static com.mlyauth.constants.AspectAttribute.SP_SAML_SSO_URL;
import static com.mlyauth.constants.AspectType.SP_SAML;

@Component
public class SPSAMLAspectValidator implements ISPSAMLAspectValidator {

    @Autowired
    private ApplicationAspectAttributeDAO appAspectAttrDAO;

    @Autowired
    private SAMLHelper samlHelper;

    @Override
    public void validate(final Application app) {
        Assert.notNull(app, "The application argument is null");
        validateIsAnSPSAML(app);
        List<AppAspAttr> attributes = appAspectAttrDAO.findByAppAndAspect(app.getId(), SP_SAML.name());
        validateAttributeExists(attributes, SP_SAML_ENTITY_ID);
        validateURL(validateAttributeExists(attributes, SP_SAML_SSO_URL));
        validateCertificate(validateAttributeExists(attributes, SP_SAML_ENCRYPTION_CERTIFICATE));

    }

    private void validateIsAnSPSAML(final Application application) {
        if (application.getAspects() == null || !application.getAspects().contains(SP_SAML))
            throw NotSPSAMLApplicationException.newInstance();
    }

    private AppAspAttr validateAttributeExists(final List<AppAspAttr> attributes,
                                               final AspectAttribute attribute) {
        final AppAspAttr found = attributes.stream()
                .filter(attr -> attribute == attr.getAttributeCode())
                .findFirst().orElse(null);

        if (found == null)
            throw MissingSPSAMLAspectAttributeException.newInstance();

        if (StringUtils.isBlank(found.getValue()))
            throw BadSPSAMLAspectAttributeValueException.newInstance();

        return found;
    }

    private void validateURL(final AppAspAttr ssoUrl) {
        UrlValidator urlValidator = new UrlValidator(new String[]{"http", "https"}, UrlValidator.ALLOW_LOCAL_URLS);
        if (!urlValidator.isValid(ssoUrl.getValue()))
            throw BadSPSAMLAspectAttributeValueException.newInstance();
    }

    private void validateCertificate(final AppAspAttr certificateAtt) {
        try {
            samlHelper.toX509Certificate(certificateAtt.getValue());
        } catch (Exception e) {
            throw BadSPSAMLAspectAttributeValueException.newInstance(e);
        }
    }

}
