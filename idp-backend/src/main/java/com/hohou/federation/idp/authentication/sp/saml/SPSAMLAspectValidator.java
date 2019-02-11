package com.hohou.federation.idp.authentication.sp.saml;

import com.hohou.federation.idp.application.AppAspAttr;
import com.hohou.federation.idp.application.Application;
import com.hohou.federation.idp.application.ApplicationAspectAttributeDAO;
import com.hohou.federation.idp.constants.AspectAttribute;
import com.hohou.federation.idp.constants.AspectType;
import com.hohou.federation.idp.exception.BadSPSAMLAspectAttributeValueExc;
import com.hohou.federation.idp.exception.MissingSPSAMLAspectAttributeExc;
import com.hohou.federation.idp.exception.NotSPSAMLApplicationExc;
import com.hohou.federation.idp.token.saml.SAMLHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.List;

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
        List<AppAspAttr> attributes = appAspectAttrDAO.findByAppAndAspect(app.getId(), AspectType.SP_SAML.name());
        validateAttributeExists(attributes, AspectAttribute.SP_SAML_ENTITY_ID);
        validateURL(validateAttributeExists(attributes, AspectAttribute.SP_SAML_SSO_URL));
        validateCertificate(validateAttributeExists(attributes, AspectAttribute.SP_SAML_ENCRYPTION_CERTIFICATE));

    }

    private void validateIsAnSPSAML(final Application application) {
        if (application.getAspects() == null || !application.getAspects().contains(AspectType.SP_SAML))
            throw NotSPSAMLApplicationExc.newInstance();
    }

    private AppAspAttr validateAttributeExists(final List<AppAspAttr> attributes,
                                               final AspectAttribute attribute) {
        final AppAspAttr found = attributes.stream()
                .filter(attr -> attribute == attr.getAttributeCode())
                .findFirst().orElse(null);

        if (found == null)
            throw MissingSPSAMLAspectAttributeExc.newInstance();

        if (StringUtils.isBlank(found.getValue()))
            throw BadSPSAMLAspectAttributeValueExc.newInstance();

        return found;
    }

    private void validateURL(final AppAspAttr ssoUrl) {
        UrlValidator urlValidator = new UrlValidator(new String[]{"http", "https"}, UrlValidator.ALLOW_LOCAL_URLS);
        if (!urlValidator.isValid(ssoUrl.getValue()))
            throw BadSPSAMLAspectAttributeValueExc.newInstance();
    }

    private void validateCertificate(final AppAspAttr certificateAtt) {
        try {
            samlHelper.toX509Certificate(certificateAtt.getValue());
        } catch (Exception e) {
            throw BadSPSAMLAspectAttributeValueExc.newInstance(e);
        }
    }

}
