package com.hohou.federation.idp.authentication.sp.jose;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource({"classpath*:context/sp-jose-context.xml"})
public class SPJOSEConfig {
}
