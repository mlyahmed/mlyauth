package com.mlyauth.security.saml;

import liquibase.util.file.FilenameUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.velocity.app.VelocityEngine;
import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.saml2.metadata.provider.ResourceBackedMetadataProvider;
import org.opensaml.util.resource.ClasspathResource;
import org.opensaml.util.resource.FilesystemResource;
import org.opensaml.xml.parse.StaticBasicParserPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.saml.*;
import org.springframework.security.saml.context.SAMLContextProviderImpl;
import org.springframework.security.saml.key.JKSKeyManager;
import org.springframework.security.saml.key.KeyManager;
import org.springframework.security.saml.log.SAMLDefaultLogger;
import org.springframework.security.saml.log.SAMLLogger;
import org.springframework.security.saml.metadata.*;
import org.springframework.security.saml.parser.ParserPoolHolder;
import org.springframework.security.saml.processor.HTTPPostBinding;
import org.springframework.security.saml.processor.SAMLProcessorImpl;
import org.springframework.security.saml.trust.httpclient.TLSProtocolConfigurer;
import org.springframework.security.saml.trust.httpclient.TLSProtocolSocketFactory;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import org.springframework.security.saml.util.VelocityFactory;
import org.springframework.security.saml.websso.*;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.Filter;
import java.io.File;
import java.util.*;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
@Order(2)
public class SAMLSPConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private SAMLUserDetailsService samlUserDetailsService;

    @Value("${saml.idps-metadata-dir}")
    private File idpsMetadataDir;


    private java.util.Timer backgroundTaskTimer;
    private MultiThreadedHttpConnectionManager multiThreadedHttpConnectionManager;

    @Bean
    public static SAMLBootstrap samlBootstrap() {
        return new SAMLBootstrap();
    }

    @PostConstruct
    public void init() {
        this.backgroundTaskTimer = new java.util.Timer(true);
        this.multiThreadedHttpConnectionManager = new MultiThreadedHttpConnectionManager();
    }

    @PreDestroy
    public void destroy() {
        this.backgroundTaskTimer.purge();
        this.backgroundTaskTimer.cancel();
        this.multiThreadedHttpConnectionManager.shutdown();
    }

    @Bean
    public SAMLLogger samlLogger() {
        SAMLDefaultLogger logger = new SAMLDefaultLogger();
        logger.setLogErrors(true);
        logger.setLogMessages(true);
        return logger;
    }

    @Bean
    public VelocityEngine velocityEngine() {
        return VelocityFactory.getEngine();
    }

    @Bean(initMethod = "initialize")
    public StaticBasicParserPool parserPool() {
        final StaticBasicParserPool staticBasicParserPool = new StaticBasicParserPool();
        staticBasicParserPool.setExpandEntityReferences(false);
        return staticBasicParserPool;
    }

    @Bean(name = "parserPoolHolder")
    public ParserPoolHolder parserPoolHolder() {
        return new ParserPoolHolder();
    }

    @Bean
    public HttpClient httpClient() {
        return new HttpClient(this.multiThreadedHttpConnectionManager);
    }

    @Bean
    public SAMLAuthenticationProvider samlAuthenticationProvider() {
        SAMLAuthenticationProvider samlAuthenticationProvider = new SAMLAuthenticationProvider();
        samlAuthenticationProvider.setUserDetails(samlUserDetailsService);
        samlAuthenticationProvider.setForcePrincipalAsString(false);
        return samlAuthenticationProvider;
    }

    @Bean
    public SAMLContextProviderImpl contextProvider() {
        return new SAMLContextProviderImpl();
    }

    @Bean
    public WebSSOProfile webSSOprofile() {
        return new WebSSOProfileImpl();
    }

    @Bean
    public WebSSOProfileConsumer webSSOprofileConsumer() {
        return new PrimaWebSSOProfileConsumerImpl();
    }

    @Bean
    public WebSSOProfileConsumerHoKImpl hokWebSSOprofileConsumer() {
        return new WebSSOProfileConsumerHoKImpl();
    }

    @Bean
    public WebSSOProfileConsumerHoKImpl hokWebSSOProfile() {
        return new WebSSOProfileConsumerHoKImpl();
    }

    @Bean
    public WebSSOProfileECPImpl ecpprofile() {
        return new WebSSOProfileECPImpl();
    }

    @Bean
    public SingleLogoutProfile logoutprofile() {
        return new SingleLogoutProfileImpl();
    }

    @Bean
    public KeyManager keyManager() {
        DefaultResourceLoader loader = new DefaultResourceLoader();
        Resource storeFile = loader.getResource("classpath:/keysstores/saml/samlKS.jks");
        Map<String, String> passwords = new HashMap<String, String>();
        passwords.put("sgi.prima-solutions.com", "Bourso$17");
        return new JKSKeyManager(storeFile, "Bourso$17", passwords, "sgi.prima-solutions.com");
    }

    @Bean
    public TLSProtocolConfigurer tlsProtocolConfigurer() {
        return new TLSProtocolConfigurer();
    }

    @Bean
    public ProtocolSocketFactory socketFactory() {
        return new TLSProtocolSocketFactory(keyManager(), null, "default");
    }

    @Bean
    public Protocol socketFactoryProtocol() {
        return new Protocol("https", socketFactory(), 443);
    }

    @Bean
    public MethodInvokingFactoryBean socketFactoryInitialization() {
        MethodInvokingFactoryBean methodInvokingFactoryBean = new MethodInvokingFactoryBean();
        methodInvokingFactoryBean.setTargetClass(Protocol.class);
        methodInvokingFactoryBean.setTargetMethod("registerProtocol");
        Object[] args = {"https", socketFactoryProtocol()};
        methodInvokingFactoryBean.setArguments(args);
        return methodInvokingFactoryBean;
    }


    @Bean
    public WebSSOProfileOptions profileOptions() {
        WebSSOProfileOptions options = new WebSSOProfileOptions();
        options.setBinding("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST");
        options.setIncludeScoping(false);
        return options;
    }

    @Bean
    public SAMLEntryPoint samlEntryPoint() {
        SAMLEntryPoint entryPoint = new SAMLEntryPoint();
        entryPoint.setDefaultProfileOptions(profileOptions());
        return entryPoint;
    }


    @Bean
    public ExtendedMetadata spExtendedMetadata() {
        ExtendedMetadata extendedMetadata = new ExtendedMetadata();
        extendedMetadata.setSignMetadata(true);
        extendedMetadata.setSupportUnsolicitedResponse(true);
        return extendedMetadata;
    }


    @Bean
    @Qualifier("metadata")
    public CachingMetadataManager metadata() throws Exception {
        return new CachingMetadataManager(idpMetadata());
    }


    @Bean(name = "idpMetadata")
    public List<MetadataProvider> idpMetadata() throws Exception {
        return (idpsMetadataDir == null) ? loadSamlIdpMetadataFromClasspath() : loadSamlIdpMetadataFromFileSystem(idpsMetadataDir);
    }


    private List<MetadataProvider> loadSamlIdpMetadataFromClasspath() throws Exception {
        List<MetadataProvider> providers = new LinkedList<>();
        final String classpathPattern = "classpath*:/com/sgi/metadata/idp/*-idp.xml";
        org.springframework.core.io.Resource[] resources = new PathMatchingResourcePatternResolver().getResources(classpathPattern);
        for (org.springframework.core.io.Resource resource : resources)
            providers.add(createSamlMetadataProvider(new ClasspathResource(String.format("/com/sgi/metadata/idp/%s", resource.getFilename()))));
        return providers;
    }

    private List<MetadataProvider> loadSamlIdpMetadataFromFileSystem(File confDir) throws Exception {
        List<MetadataProvider> providers = new LinkedList<>();
        final String fileSystemPattern = FilenameUtils.separatorsToUnix("file://" + new File(confDir, "/saml/idp/*-idp.xml").getAbsolutePath());
        org.springframework.core.io.Resource[] resources = new PathMatchingResourcePatternResolver().getResources(fileSystemPattern);
        for (org.springframework.core.io.Resource resource : resources)
            providers.add(createSamlMetadataProvider(new FilesystemResource(resource.getFile().getAbsolutePath())));
        return providers;
    }

    private ExtendedMetadataDelegate createSamlMetadataProvider(org.opensaml.util.resource.Resource metadata) throws MetadataProviderException {
        ResourceBackedMetadataProvider metadataProvider = new ResourceBackedMetadataProvider(new Timer(), metadata);
        metadataProvider.setParserPool(parserPool());
        ExtendedMetadataDelegate provider = new ExtendedMetadataDelegate(metadataProvider, new ExtendedMetadata());
        provider.setMetadataRequireSignature(false);
        provider.setMetadataTrustCheck(true);
        provider.setRequireValidMetadata(true);
        return provider;
    }


    @Bean
    public MetadataGenerator spMetadataGenerator() {
        MetadataGenerator metadataGenerator = new MetadataGenerator();
        metadataGenerator.setWantAssertionSigned(true);
        metadataGenerator.setRequestSigned(true);
        metadataGenerator.setEntityId("primainsure4sgi");
        metadataGenerator.setNameID(Collections.emptyList());
        metadataGenerator.setBindingsSSO(Arrays.asList("POST"));
        metadataGenerator.setBindingsSLO(Arrays.asList("POST"));
        metadataGenerator.setExtendedMetadata(spExtendedMetadata());
        return metadataGenerator;
    }


    @Bean
    public MetadataDisplayFilter metadataDisplayFilter() {
        return new PrimaSPMetadataDisplayFilter();
    }


    @Bean
    public Filter metadataGeneratorFilter() {
        return new MetadataGeneratorFilter(spMetadataGenerator());
    }


    @Bean
    public SAMLProcessingFilter samlWebSSOProcessingFilter() throws Exception {
        SAMLProcessingFilter samlWebSSOProcessingFilter = new SAMLProcessingFilter();
        samlWebSSOProcessingFilter.setAuthenticationManager(authenticationManager());
        samlWebSSOProcessingFilter.setAuthenticationSuccessHandler(successRedirectHandler());
        samlWebSSOProcessingFilter.setAuthenticationFailureHandler(authenticationFailureHandler());
        return samlWebSSOProcessingFilter;
    }

    @Bean
    public SavedRequestAwareAuthenticationSuccessHandler successRedirectHandler() {
        SavedRequestAwareAuthenticationSuccessHandler successRedirectHandler = new SavedRequestAwareAuthenticationSuccessHandler();
        successRedirectHandler.setDefaultTargetUrl("/landing");
        return successRedirectHandler;
    }

    @Bean
    public SimpleUrlAuthenticationFailureHandler authenticationFailureHandler() {
        SimpleUrlAuthenticationFailureHandler failureHandler = new SimpleUrlAuthenticationFailureHandler();
        failureHandler.setUseForward(true);
        failureHandler.setDefaultFailureUrl("/error");
        return failureHandler;
    }

    @Bean
    public SimpleUrlLogoutSuccessHandler successLogoutHandler() {
        SimpleUrlLogoutSuccessHandler successLogoutHandler = new SimpleUrlLogoutSuccessHandler();
        successLogoutHandler.setDefaultTargetUrl("/");
        return successLogoutHandler;
    }

    @Bean
    public SecurityContextLogoutHandler logoutHandler() {
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.setInvalidateHttpSession(true);
        logoutHandler.setClearAuthentication(true);
        return logoutHandler;
    }

    @Bean
    public SAMLLogoutProcessingFilter samlLogoutProcessingFilter() {
        return new SAMLLogoutProcessingFilter(successLogoutHandler(), logoutHandler());
    }

    @Bean
    public SAMLLogoutFilter samlLogoutFilter() {
        return new SAMLLogoutFilter(successLogoutHandler(), new LogoutHandler[]{logoutHandler()}, new LogoutHandler[]{logoutHandler()});
    }


    @Bean
    public HTTPPostBinding httpPostBinding() {
        return new HTTPPostBinding(parserPool(), velocityEngine());
    }

    @Bean
    public SAMLProcessorImpl processor() {
        return new SAMLProcessorImpl(Arrays.asList(httpPostBinding()));
    }


    @Bean
    public FilterChainProxy samlFilter() throws Exception {
        List<SecurityFilterChain> chains = new ArrayList<>();
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/login/**"), samlEntryPoint()));
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/logout/**"), samlLogoutFilter()));
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/metadata/**"), metadataDisplayFilter()));
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/SingleLogout/**"), samlLogoutProcessingFilter()));
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/saml/SSO/**"), samlWebSSOProcessingFilter()));
        return new FilterChainProxy(chains);
    }


    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.antMatcher("/saml/**").authorizeRequests().anyRequest().fullyAuthenticated()
                .and().exceptionHandling()
                .defaultAuthenticationEntryPointFor(samlEntryPoint(), new AntPathRequestMatcher("/saml/**"));

        http.csrf().disable();
        http.addFilterBefore(metadataGeneratorFilter(), ChannelProcessingFilter.class);
        http.addFilterAfter(samlFilter(), BasicAuthenticationFilter.class);

        http.authorizeRequests()
                .antMatchers("/").permitAll()
                .antMatchers("/error").permitAll()
                .antMatchers("/saml/**").permitAll()
                .anyRequest().authenticated();
        http.logout()
                .logoutSuccessUrl("/");
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(samlAuthenticationProvider());
    }


}
