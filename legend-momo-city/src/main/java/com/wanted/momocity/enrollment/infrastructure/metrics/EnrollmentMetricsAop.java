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
        MetricSpec metricSpec = resolveMetricSpec(service, method);
        String outcome = SUCCESS;
        String exception = NONE;

        try {
            return joinPoint.proceed();
        } catch (Throwable throwable) {
            outcome = FAILURE;
            exception = throwable.getClass().getSimpleName();
            throw throwable;
        } finally {
            record(sample, service, method, metricSpec, outcome, exception);
        }
    }

    private MetricSpec resolveMetricSpec(String service, String method) {
        if ("EnrollmentCommandService".equals(service)
                && "createEnrollment".equals(method)) {
            return new MetricSpec(
                    "momocity.enrollment.create",
                    "수강신청 소요 시간"
            );
        }

        return new MetricSpec(
                "momocity.enrollment.service",
                "수강신청 서비스 메서드 소요 시간"
        );
    }

    private void record(
            Timer.Sample sample,
            String service,
            String method,
            MetricSpec metricSpec,
            String outcome,
            String exception
    ) {
        String[] tags = {
                "service", service,
                "method", method,
                "outcome", outcome,
                "exception", exception
        };

        Timer timer = Timer.builder(metricSpec.name() + ".duration")
                .description(metricSpec.description())
                .tags(tags)
                .register(meterRegistry);

        sample.stop(timer);

        Counter.builder(metricSpec.name() + ".invocations")
                .description(metricSpec.description() + " 호출 횟수")
                .tags(tags)
                .register(meterRegistry)
                .increment();
    }

    private record MetricSpec(
            String name,
            String description
    ) {
    }
}
