package com.mlyauth.validators;

import com.mlyauth.constants.SPSAMLAuthAttributes;
import com.mlyauth.dao.ApplicationAspectAttributeDAO;
import com.mlyauth.domain.Application;
import com.mlyauth.domain.ApplicationAspectAttribute;
import com.mlyauth.exception.BadSPSAMLAspectAttributeValue;
import com.mlyauth.exception.MissingSPSAMLAspectAttribute;
import com.mlyauth.exception.NotSPSAMLApplication;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.List;

import static com.mlyauth.constants.AuthAspectType.SP_SAML;
import static com.mlyauth.constants.SPSAMLAuthAttributes.SP_SAML_ENTITY_ID;
import static com.mlyauth.constants.SPSAMLAuthAttributes.SP_SAML_SSO_URL;

@Component
public class SPSAMLAspectValidator {

    @Autowired
    private ApplicationAspectAttributeDAO appAspectAttrDAO;

    public void validate(Application application) {
        Assert.notNull(application, "The application argument is null");

        if (application.getAspects() == null || !application.getAspects().contains(SP_SAML))
            throw NotSPSAMLApplication.newInstance();

        List<ApplicationAspectAttribute> attributes = appAspectAttrDAO.findByAppAndAspect(application.getId(), SP_SAML.name());
        validateAttributeExists(attributes, SP_SAML_ENTITY_ID);
        validateAttributeExists(attributes, SP_SAML_SSO_URL);

    }

    private ApplicationAspectAttribute validateAttributeExists(List<ApplicationAspectAttribute> attributes, SPSAMLAuthAttributes attribute) {
        final ApplicationAspectAttribute found = attributes.stream()
                .filter(attr -> attribute == attr.getAttributeCode())
                .findFirst().orElse(null);

        if (found == null)
            throw MissingSPSAMLAspectAttribute.newInstance();

        if (StringUtils.isBlank(found.getValue()))
            throw BadSPSAMLAspectAttributeValue.newInstance();

        return found;
    }

}
