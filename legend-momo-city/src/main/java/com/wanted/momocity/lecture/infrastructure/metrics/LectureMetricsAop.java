package com.wanted.momocity.lecture.infrastructure.metrics;

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
public class LectureMetricsAop {

    private static final String SUCCESS = "success";
    private static final String FAILURE = "failure";
    private static final String NONE = "none";
    private static final String STUDENT = "student";
    private static final String TEACHER = "teacher";
    private static final String ADMIN = "admin";

    private final MeterRegistry meterRegistry;

    @Around("execution(public * com.wanted.momocity.lecture.application.service..*(..))")
    public Object measureLectureService(ProceedingJoinPoint joinPoint) throws Throwable {
        Timer.Sample sample = Timer.start(meterRegistry);
        String service = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String method = ((MethodSignature) joinPoint.getSignature()).getMethod().getName();
        String viewer = resolveViewer(service, method);
        String outcome = SUCCESS;
        String exception = NONE;

        try {
            return joinPoint.proceed();
        } catch (Throwable throwable) {
            outcome = FAILURE;
            exception = throwable.getClass().getSimpleName();
            throw throwable;
        } finally {
            record(sample, service, method, viewer, outcome, exception);
        }
    }

    private String resolveViewer(String service, String method) {
        if ("LectureQueryService".equals(service)) {
            if ("getLectures".equals(method) || "getStudentLectureDetail".equals(method)) {
                return STUDENT;
            }

            if ("getTeacherLectures".equals(method) || "getTeacherLectureDetail".equals(method)) {
                return TEACHER;
            }
        }

        if ("AdminLectureQueryService".equals(service)) {
            return ADMIN;
        }

        return NONE;
    }

    private void record(
            Timer.Sample sample,
            String service,
            String method,
            String viewer,
            String outcome,
            String exception
    ) {
        String[] tags = {
                "service", service,
                "method", method,
                "viewer", viewer,
                "outcome", outcome,
                "exception", exception
        };

        Timer timer = Timer.builder("momocity.lecture.service.duration")
                .description("Lecture service method duration")
                .tags(tags)
                .register(meterRegistry);

        sample.stop(timer);

        Counter.builder("momocity.lecture.service.invocations")
                .description("Lecture service method invocation count")
                .tags(tags)
                .register(meterRegistry)
                .increment();
    }
}
