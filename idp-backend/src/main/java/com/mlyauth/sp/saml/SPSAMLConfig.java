package com.mlyauth.sp.saml;

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
import org.springframework.core.io.Resource;
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

import static java.lang.String.format;

@Configuration
@ImportResource({"classpath*:context/sp-saml-context.xml"})
public class SPSAMLConfig {

    @Autowired
    private ParserPool parserPool;

    @Autowired
    private KeyManager keyManager;

    @Value("${sp.saml.idps-metadata-dir:#{null}}")
    private File metadataDir;

    @Value("${sp.saml.entityId}")
    private String localEntityId;

    @Bean("metadata")
    public MetadataManager metadata() throws Exception {
        final MetadataManager metadataManager = new CachingMetadataManager(idpMetadata());
        metadataManager.setDefaultExtendedMetadata(extendedMetadata());
        metadataManager.setKeyManager(keyManager);
        metadataManager.setRefreshRequired(true);
        return metadataManager;
    }

    public ExtendedMetadata extendedMetadata() {
        ExtendedMetadata extendedMetadata = new ExtendedMetadata();
        extendedMetadata.setSignMetadata(true);
        extendedMetadata.setSupportUnsolicitedResponse(true);
        return extendedMetadata;
    }

    public List<MetadataProvider> idpMetadata() throws Exception {
        return (metadataDir == null) ? getIdpMetadataFromClasspath() : getIdpMetadataFromFileSystem(metadataDir);
    }


    private List<MetadataProvider> getIdpMetadataFromClasspath() throws Exception {
        List<MetadataProvider> providers = new LinkedList<>();
        final String classpathPattern = "classpath*:/sso/saml/idps/*-idp.xml";
        Resource[] resources = new PathMatchingResourcePatternResolver().getResources(classpathPattern);
        for (Resource rs : resources)
            providers.add(createMetadataProvider(new ClasspathResource(format("/sso/saml/idps/%s", rs.getFilename()))));
        return providers;
    }

    private List<MetadataProvider> getIdpMetadataFromFileSystem(final File confDir) throws Exception {
        final String metadataPathPattern = "file://" + new File(confDir, "/sso/saml/idps/*-idp.xml").getAbsolutePath();
        final String fileSystemPattern = FilenameUtils.separatorsToUnix(metadataPathPattern);
        List<MetadataProvider> providers = new LinkedList<>();
        for (Resource resource : new PathMatchingResourcePatternResolver().getResources(fileSystemPattern))
            providers.add(createMetadataProvider(new FilesystemResource(resource.getFile().getAbsolutePath())));
        return providers;
    }

    private ExtendedMetadataDelegate createMetadataProvider(final org.opensaml.util.resource.Resource metadata)
            throws MetadataProviderException {
        ResourceBackedMetadataProvider metadataProvider = new ResourceBackedMetadataProvider(new Timer(), metadata);
        metadataProvider.setParserPool(parserPool);
        ExtendedMetadataDelegate provider = new ExtendedMetadataDelegate(metadataProvider, new ExtendedMetadata());
        provider.setMetadataRequireSignature(false);
        provider.setMetadataTrustCheck(true);
        provider.setRequireValidMetadata(true);
        return provider;
    }

}
