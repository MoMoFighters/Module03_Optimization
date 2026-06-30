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
        if ("LectureQueryService".equals(service)) {
            if ("getLectures".equals(method)) {
                return new MetricSpec(
                        "momocity.lecture.student.list",
                        "학생 강의 목록 조회 소요 시간",
                        STUDENT
                );
            }

            if ("getStudentLectureDetail".equals(method)) {
                return new MetricSpec(
                        "momocity.lecture.student.detail",
                        "학생 강의 상세 조회 소요 시간",
                        STUDENT
                );
            }

            if ("getTeacherLectures".equals(method)) {
                return new MetricSpec(
                        "momocity.lecture.teacher.list",
                        "강사 강의 목록 조회 소요 시간",
                        TEACHER
                );
            }

            if ("getTeacherLectureDetail".equals(method)) {
                return new MetricSpec(
                        "momocity.lecture.teacher.detail",
                        "강사 강의 상세 조회 소요 시간",
                        TEACHER
                );
            }
        }

        if ("AdminLectureQueryService".equals(service)) {
            if ("getAdminLectures".equals(method)) {
                return new MetricSpec(
                        "momocity.lecture.admin.list",
                        "관리자 강의 목록 조회 소요 시간",
                        ADMIN
                );
            }

            if ("getAdminLectureDetail".equals(method)) {
                return new MetricSpec(
                        "momocity.lecture.admin.detail",
                        "관리자 강의 상세 조회 소요 시간",
                        ADMIN
                );
            }
        }

        if ("LectureCommandService".equals(service)) {
            if ("createLecture".equals(method)) {
                return new MetricSpec(
                        "momocity.lecture.create",
                        "강의 등록 소요 시간",
                        NONE
                );
            }

            if ("changeLectureStatus".equals(method)) {
                return new MetricSpec(
                        "momocity.lecture.status.change",
                        "강의 상태 변경 소요 시간",
                        NONE
                );
            }
        }

        if ("AdminLectureCommandService".equals(service)
                && "changeLectureStatus".equals(method)) {
            return new MetricSpec(
                    "momocity.lecture.admin.status.change",
                    "관리자 강의 상태 변경 소요 시간",
                    ADMIN
            );
        }

        if ("ChapterCommandService".equals(service)) {
            if ("createChapter".equals(method)) {
                return new MetricSpec(
                        "momocity.chapter.create",
                        "챕터 등록 소요 시간",
                        NONE
                );
            }

            if ("registerChapterVideo".equals(method)) {
                return new MetricSpec(
                        "momocity.chapter.video.register",
                        "챕터 동영상 등록 소요 시간",
                        NONE
                );
            }

            if ("changeChapterVideoStatus".equals(method)) {
                return new MetricSpec(
                        "momocity.chapter.video.status.change",
                        "챕터 동영상 상태 변경 소요 시간",
                        NONE
                );
            }
        }

        return new MetricSpec(
                "momocity.lecture.service",
                "강의 서비스 메서드 소요 시간",
                NONE
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
                "viewer", metricSpec.viewer(),
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
            String description,
            String viewer
    ) {
    }
}
