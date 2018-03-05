package com.mlyauth.security.sso.sp.saml;

import liquibase.util.file.FilenameUtils;
import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.saml2.metadata.provider.ResourceBackedMetadataProvider;
import org.opensaml.util.resource.ClasspathResource;
import org.opensaml.util.resource.FilesystemResource;
import org.opensaml.xml.parse.ParserPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.security.saml.key.KeyManager;
import org.springframework.security.saml.metadata.CachingMetadataManager;
import org.springframework.security.saml.metadata.ExtendedMetadata;
import org.springframework.security.saml.metadata.ExtendedMetadataDelegate;
import org.springframework.security.saml.metadata.MetadataManager;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;

@Configuration
@ImportResource({"classpath*:context/sp-saml-context.xml"})
public class SPSAMLConfig {

    @Autowired
    private ParserPool parserPool;

    @Autowired
    private KeyManager keyManager;

    @Value("${sp.saml.idps-metadata-dir:#{null}}")
    private File idpsMetadataDir;

    @Bean("metadata")
    public MetadataManager metadata() throws Exception {
        final MetadataManager metadataManager = new CachingMetadataManager(idpMetadata());
        metadataManager.setDefaultExtendedMetadata(extendedMetadata());
        metadataManager.setKeyManager(keyManager);
        metadataManager.setRefreshRequired(true);
        metadataManager.refreshMetadata();
        return metadataManager;
    }

    public ExtendedMetadata extendedMetadata() {
        ExtendedMetadata extendedMetadata = new ExtendedMetadata();
        extendedMetadata.setSignMetadata(true);
        extendedMetadata.setSupportUnsolicitedResponse(true);
        return extendedMetadata;
    }

    public List<MetadataProvider> idpMetadata() throws Exception {
        return (idpsMetadataDir == null) ? loadSamlIdpMetadataFromClasspath() : loadSamlIdpMetadataFromFileSystem(idpsMetadataDir);
    }


    private List<MetadataProvider> loadSamlIdpMetadataFromClasspath() throws Exception {
        List<MetadataProvider> providers = new LinkedList<>();
        final String classpathPattern = "classpath*:/sso/saml/idps-metadata/*-idp.xml";
        org.springframework.core.io.Resource[] resources = new PathMatchingResourcePatternResolver().getResources(classpathPattern);
        for (org.springframework.core.io.Resource resource : resources)
            providers.add(createSamlMetadataProvider(new ClasspathResource(String.format("/sso/saml/idps-metadata/%s", resource.getFilename()))));
        return providers;
    }

    private List<MetadataProvider> loadSamlIdpMetadataFromFileSystem(File confDir) throws Exception {
        List<MetadataProvider> providers = new LinkedList<>();
        final String fileSystemPattern = FilenameUtils.separatorsToUnix("file://" + new File(confDir, "/sso/saml/idps-metadata/*-idp.xml").getAbsolutePath());
        org.springframework.core.io.Resource[] resources = new PathMatchingResourcePatternResolver().getResources(fileSystemPattern);
        for (org.springframework.core.io.Resource resource : resources)
            providers.add(createSamlMetadataProvider(new FilesystemResource(resource.getFile().getAbsolutePath())));
        return providers;
    }

    private ExtendedMetadataDelegate createSamlMetadataProvider(org.opensaml.util.resource.Resource metadata) throws MetadataProviderException {
        ResourceBackedMetadataProvider metadataProvider = new ResourceBackedMetadataProvider(new Timer(), metadata);
        metadataProvider.setParserPool(parserPool);
        ExtendedMetadataDelegate provider = new ExtendedMetadataDelegate(metadataProvider, new ExtendedMetadata());
        provider.setMetadataRequireSignature(false);
        provider.setMetadataTrustCheck(true);
        provider.setRequireValidMetadata(true);
        return provider;
    }

}
