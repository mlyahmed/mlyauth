package com.mlyauth.interception;

import com.google.common.base.Stopwatch;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static java.util.concurrent.TimeUnit.MILLISECONDS;


@Component
@Aspect
public class PerformanceInterceptor {
    private static Logger logger = LoggerFactory.getLogger(PerformanceInterceptor.class);

    @Pointcut("execution(* com.mlyauth.token..*(..))")
    public void tokens() { }

    @Pointcut("execution(* com.mlyauth.api..*(..))")
    public void api() { }

    @Pointcut("execution(* com.mlyauth.beans..*(..))")
    public void beans() { }

    @Pointcut("execution(* com.mlyauth.context..*(..))")
    public void context() { }

    @Pointcut("execution(* com.mlyauth.credentials..*(..))")
    public void credentials() { }

    @Pointcut("execution(* com.mlyauth.dao..*(..))")
    public void dao() { }

    @Pointcut("execution(* com.mlyauth.mappers..*(..))")
    public void mappers() { }

    @Pointcut("execution(* com.mlyauth.security..*(..))")
    public void security() { }

    @Pointcut("execution(* com.mlyauth.validators..*(..))")
    public void validators() { }

    @Around("tokens() || api() || beans()  || context() || credentials() || dao() || mappers() || security() || validators()")
    public Object profile(ProceedingJoinPoint pjp) throws Throwable {
        final Stopwatch started = Stopwatch.createStarted();
        Object result = pjp.proceed();
        logger.trace("Method "+ pjp.toLongString()+" took "+started.elapsed(MILLISECONDS)+"ms.");
        return result;
    }

}
