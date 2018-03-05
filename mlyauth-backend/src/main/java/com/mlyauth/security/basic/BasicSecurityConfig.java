package com.mlyauth.security.basic;

import com.mlyauth.hooks.SPSAMLUrlAuthenticationSuccessHandler;
import com.mlyauth.security.sso.sp.jose.JOSEAuthenticationProvider;
import com.mlyauth.security.sso.sp.jose.SPJOSEProcessingFilter;
import com.mlyauth.security.sso.sp.saml.SPSAMLMetadataDisplayFilter;
import com.mlyauth.security.sso.sp.saml.SPSAMLProcessingFilter;
import com.mlyauth.security.sso.sp.saml.SPSAMLWebSSOProfileConsumerImpl;
import liquibase.util.file.FilenameUtils;
import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.saml2.metadata.provider.ResourceBackedMetadataProvider;
import org.opensaml.util.resource.ClasspathResource;
import org.opensaml.util.resource.FilesystemResource;
import org.opensaml.xml.parse.ParserPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.saml.SAMLAuthenticationProvider;
import org.springframework.security.saml.SAMLProcessingFilter;
import org.springframework.security.saml.context.SAMLContextProviderImpl;
import org.springframework.security.saml.key.KeyManager;
import org.springframework.security.saml.metadata.*;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import org.springframework.security.saml.websso.*;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.RequestContextFilter;

import javax.servlet.Filter;
import java.io.File;
import java.util.*;

@EnableWebSecurity
@Configuration
public class BasicSecurityConfig extends WebSecurityConfigurerAdapter {

    public static final String SP_SAML_METADATA_ENDPOINT = "/sp/saml/metadata";
    public static final String SP_SAML_SSO_ENDPOINT = "/sp/saml/sso";
    public static final String SP_ENTITY_ID = "primainsure4sgi";

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private SAMLUserDetailsService samlUserDetailsService;

    @Autowired
    private ParserPool parserPool;

    @Autowired
    private KeyManager keyManager;

    @Value("${sp.saml.idps-metadata-dir:#{null}}")
    private File idpsMetadataDir;


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
        return new SPSAMLWebSSOProfileConsumerImpl();
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
    public ExtendedMetadata extendedMetadata() {
        ExtendedMetadata extendedMetadata = new ExtendedMetadata();
        extendedMetadata.setSignMetadata(true);
        extendedMetadata.setSupportUnsolicitedResponse(true);
        return extendedMetadata;
    }


    @Bean
    @Qualifier("metadata")
    public MetadataManager metadata() throws Exception {
        final MetadataManager metadataManager = new CachingMetadataManager(idpMetadata());
        metadataManager.setDefaultExtendedMetadata(extendedMetadata());
        metadataManager.setKeyManager(keyManager);
        metadataManager.setRefreshRequired(true);
        metadataManager.refreshMetadata();
        return metadataManager;
    }


    @Bean(name = "idpMetadata")
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


    @Bean
    public WebSSOProfileOptions profileOptions() {
        WebSSOProfileOptions options = new WebSSOProfileOptions();
        options.setBinding("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST");
        options.setIncludeScoping(false);
        return options;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(13);
    }

    @Bean
    public DaoAuthenticationProvider daoAuthProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public SAMLAuthenticationProvider samlAuthenticationProvider() {
        SAMLAuthenticationProvider samlAuthenticationProvider = new SAMLAuthenticationProvider();
        samlAuthenticationProvider.setUserDetails(samlUserDetailsService);
        samlAuthenticationProvider.setForcePrincipalAsString(false);
        return samlAuthenticationProvider;
    }

    @Bean
    public JOSEAuthenticationProvider joseAuthenticationProvider() {
        return new JOSEAuthenticationProvider();
    }


    @Autowired
    public void configAuthenticationProvider(AuthenticationManagerBuilder auth){
        auth.authenticationProvider(daoAuthProvider());
        auth.authenticationProvider(samlAuthenticationProvider());
        auth.authenticationProvider(joseAuthenticationProvider());
    }

    @Bean("requestContextFilter")
    public RequestContextFilter requestContextFilter() {
        return new RequestContextFilter();
    }

    @Bean
    public MetadataDisplayFilter metadataDisplayFilter() {
        final SPSAMLMetadataDisplayFilter metadataGeneratorFilter = new SPSAMLMetadataDisplayFilter();
        metadataGeneratorFilter.setFilterProcessesUrl(SP_SAML_METADATA_ENDPOINT);
        return metadataGeneratorFilter;
    }

    @Bean
    public MetadataGenerator metadataGenerator() {
        MetadataGenerator metadataGenerator = new MetadataGenerator();
        metadataGenerator.setWantAssertionSigned(true);
        metadataGenerator.setRequestSigned(true);
        metadataGenerator.setEntityId(SP_ENTITY_ID);
        metadataGenerator.setNameID(Collections.emptyList());
        metadataGenerator.setBindingsSSO(Arrays.asList("POST"));
        metadataGenerator.setBindingsSLO(Arrays.asList("POST"));
        metadataGenerator.setExtendedMetadata(extendedMetadata());
        return metadataGenerator;
    }

    @Bean
    public Filter metadataGeneratorFilter() {
        final MetadataGeneratorFilter metadataGeneratorFilter = new MetadataGeneratorFilter(metadataGenerator());
        metadataGeneratorFilter.setDisplayFilter(metadataDisplayFilter());
        return metadataGeneratorFilter;
    }

    @Bean
    public SavedRequestAwareAuthenticationSuccessHandler successRedirectHandler() {
        SavedRequestAwareAuthenticationSuccessHandler successRedirectHandler = new SPSAMLUrlAuthenticationSuccessHandler();
        successRedirectHandler.setDefaultTargetUrl("/home.html");
        return successRedirectHandler;
    }

    @Bean
    public SimpleUrlAuthenticationFailureHandler authenticationFailureHandler() {
        SimpleUrlAuthenticationFailureHandler failureHandler = new SimpleUrlAuthenticationFailureHandler();
        failureHandler.setUseForward(true);
        failureHandler.setDefaultFailureUrl("/error.html");
        return failureHandler;
    }

    @Bean
    public SPJOSEProcessingFilter spJoseProcessingFilter() throws Exception {
        final SPJOSEProcessingFilter processingFilter = new SPJOSEProcessingFilter();
        processingFilter.setFilterProcessesUrl("/sp/jose/sso");
        processingFilter.setAuthenticationManager(authenticationManager());
        processingFilter.setAuthenticationSuccessHandler(successRedirectHandler());
        processingFilter.setAuthenticationFailureHandler(authenticationFailureHandler());
        return processingFilter;
    }

    @Bean
    public SAMLProcessingFilter samlWebSSOProcessingFilter() throws Exception {
        SAMLProcessingFilter samlProcessing = new SPSAMLProcessingFilter();
        samlProcessing.setFilterProcessesUrl(SP_SAML_SSO_ENDPOINT);
        samlProcessing.setAuthenticationManager(authenticationManager());
        samlProcessing.setAuthenticationSuccessHandler(successRedirectHandler());
        samlProcessing.setAuthenticationFailureHandler(authenticationFailureHandler());
        return samlProcessing;
    }


    @Bean("samlFilter")
    public FilterChainProxy samlFilter() throws Exception {
        List<SecurityFilterChain> chains = new ArrayList<>();
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher(SP_SAML_METADATA_ENDPOINT), metadataDisplayFilter()));
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher(SP_SAML_SSO_ENDPOINT), samlWebSSOProcessingFilter()));
        chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher("/sp/jose/sso"), spJoseProcessingFilter()));
        return new FilterChainProxy(chains);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.ALWAYS);
        http.httpBasic();
        http.authorizeRequests()
                .antMatchers("/sp/jose/**").permitAll()
                .antMatchers("/sp/saml/**").permitAll()
                .antMatchers("/idp/saml/**").permitAll()
                .antMatchers("/login*").permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("/login.html")
                .failureUrl("/login-error.html")
                .successForwardUrl("/home")
                .and()
                .logout()
                .logoutSuccessUrl("/login.html");

        http.addFilterBefore(requestContextFilter(), ChannelProcessingFilter.class);
        http.csrf().disable();
    }

}
