package com.mlyauth.security.saml;

import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.springframework.security.saml.metadata.MetadataDisplayFilter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class PrimaSPMetadataDisplayFilter extends MetadataDisplayFilter {

    @Override
    protected void processMetadataDisplay(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        try {
            org.springframework.security.saml.context.SAMLMessageContext context = contextProvider.getLocalEntity(request, response);
            String entityId = context.getLocalEntityId();
            response.setContentType("application/samlmetadata+xml"); // SAML_Meta, 4.1.1 - line 1235
            response.addHeader("Content-Disposition", "attachment; filename=\"prima_saml_metadata.xml\"");
            displayMetadata(entityId, response.getWriter());
        } catch (MetadataProviderException e) {
            throw new ServletException("Error initializing metadata", e);
        }
    }

}