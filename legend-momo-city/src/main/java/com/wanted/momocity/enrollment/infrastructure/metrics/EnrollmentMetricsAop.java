package com.wanted.momocity.enrollment.infrastructure.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class EnrollmentMetricsAop {

    private static final String SUCCESS = "success";
    private static final String FAILURE = "failure";
    private static final String NONE = "none";

    private final MeterRegistry meterRegistry;

    @Around("execution(public * com.wanted.momocity.enrollment.application.service..*(..))")
    public Object measureEnrollmentService(ProceedingJoinPoint joinPoint) throws Throwable {
        Timer.Sample sample = Timer.start(meterRegistry);
        String service = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String method = ((MethodSignature) joinPoint.getSignature()).getMethod().getName();
        String outcome = SUCCESS;
        String exception = NONE;

        try {
            return joinPoint.proceed();
        } catch (Throwable throwable) {
            outcome = FAILURE;
            exception = throwable.getClass().getSimpleName();
            throw throwable;
        } finally {
            record(sample, service, method, outcome, exception);
        }
    }

    private void record(
            Timer.Sample sample,
            String service,
            String method,
            String outcome,
            String exception
    ) {
        String[] tags = {
                "service", service,
                "method", method,
                "outcome", outcome,
                "exception", exception
        };

        Timer timer = Timer.builder("momocity.enrollment.service.duration")
                .description("Enrollment service method duration")
                .tags(tags)
                .register(meterRegistry);

        sample.stop(timer);

        Counter.builder("momocity.enrollment.service.invocations")
                .description("Enrollment service method invocation count")
                .tags(tags)
                .register(meterRegistry)
                .increment();
    }
}
