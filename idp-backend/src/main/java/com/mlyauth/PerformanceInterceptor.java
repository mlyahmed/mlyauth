package com.mlyauth;

import com.google.common.base.Stopwatch;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static java.util.concurrent.TimeUnit.MILLISECONDS;


@Component
@Aspect
public class PerformanceInterceptor {
    private static Logger logger = LoggerFactory.getLogger(PerformanceInterceptor.class);

    @Value("${performance.trace}")
    private Boolean trace;

    @Pointcut("execution(* com.mlyauth.token..*(..))")
    public void tokens() { }


    @Pointcut("execution(* com.mlyauth.beans..*(..))")
    public void beans() { }

    @Pointcut("execution(* com.mlyauth.context..*(..))")
    public void context() { }

    @Pointcut("execution(* com.mlyauth.credentials..*(..))")
    public void credentials() { }

    @Pointcut("execution(* com.mlyauth.dao..*(..))")
    public void dao() { }

    @Pointcut("execution(* com.mlyauth.security..*(..))")
    public void security() { }

    @Around("tokens() || beans()  || context() || credentials() || dao()  || security()")
    public Object profile(final ProceedingJoinPoint pjp) throws Throwable {
        final Stopwatch started = Stopwatch.createStarted();

        try {
            return pjp.proceed();
        } finally {
            final long elapsed = started.elapsed(MILLISECONDS);
            if (trace && elapsed > 0) logger.info("Method " + pjp.toLongString() + " took " + elapsed + "ms.");
        }

    }

}
