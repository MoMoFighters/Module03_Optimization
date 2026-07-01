package com.wanted.momocity.global.metrics;

import com.wanted.momocity.global.infrastructure.metrics.MetricsAop;
import com.wanted.momocity.global.infrastructure.metrics.MomoMetrics;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MetricsAopTest {

    private SimpleMeterRegistry registry;
    private MomoMetrics momoMetrics;
    private MetricsAop metricsAop;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        momoMetrics = new MomoMetrics(registry);
        metricsAop = new MetricsAop(momoMetrics);
    }

    // ===== Admin BC =====

    @Test
    @DisplayName("measureAdminDashboard - 정상 처리 시 타이머 count 1")
    void measureAdminDashboard_success() throws Throwable {
        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        when(pjp.proceed()).thenReturn("dashboard");

        metricsAop.measureAdminDashboard(pjp);

        Timer timer = registry.find("momocity.admin.dashboard.duration").timer();
        assertThat(timer.count()).isEqualTo(1L);
    }

    @Test
    @DisplayName("measureAdminDashboard - 예외 발생 시에도 타이머 기록")
    void measureAdminDashboard_exception_stillRecordsTimer() throws Throwable {
        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        when(pjp.proceed()).thenThrow(new RuntimeException("db error"));

        assertThatThrownBy(() -> metricsAop.measureAdminDashboard(pjp))
                .isInstanceOf(RuntimeException.class);

        Timer timer = registry.find("momocity.admin.dashboard.duration").timer();
        assertThat(timer.count()).isEqualTo(1L);
    }

    @Test
    @DisplayName("measureAdminErrorLogQuery - 정상 처리 시 타이머 count 1")
    void measureAdminErrorLogQuery_success() throws Throwable {
        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        when(pjp.proceed()).thenReturn("logs");

        metricsAop.measureAdminErrorLogQuery(pjp);

        Timer timer = registry.find("momocity.admin.errorlog.query.duration").timer();
        assertThat(timer.count()).isEqualTo(1L);
    }

    // ===== Report BC =====

    @Test
    @DisplayName("measureReportSubmit - 성공 시 타이머 count 1, 카운터 count 1")
    void measureReportSubmit_success() throws Throwable {
        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        when(pjp.proceed()).thenReturn("report");

        metricsAop.measureReportSubmit(pjp);

        Timer timer = registry.find("momocity.report.submit.duration").timer();
        Counter counter = registry.find("momocity.report.submit.total").counter();
        assertThat(timer.count()).isEqualTo(1L);
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("measureReportSubmit - 예외 시 타이머 기록되지만 카운터는 0")
    void measureReportSubmit_exception_counterNotIncremented() throws Throwable {
        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        when(pjp.proceed()).thenThrow(new RuntimeException("fail"));

        assertThatThrownBy(() -> metricsAop.measureReportSubmit(pjp))
                .isInstanceOf(RuntimeException.class);

        Timer timer = registry.find("momocity.report.submit.duration").timer();
        Counter counter = registry.find("momocity.report.submit.total").counter();
        assertThat(timer.count()).isEqualTo(1L);
        assertThat(counter.count()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("measureReportSubmit - 2회 성공 시 카운터 count 2")
    void measureReportSubmit_twoSuccesses_counterIsTwo() throws Throwable {
        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        when(pjp.proceed()).thenReturn("report");

        metricsAop.measureReportSubmit(pjp);
        metricsAop.measureReportSubmit(pjp);

        Counter counter = registry.find("momocity.report.submit.total").counter();
        assertThat(counter.count()).isEqualTo(2.0);
    }

    @Test
    @DisplayName("measureReportQuery - 정상 처리 시 타이머 count 1")
    void measureReportQuery_success() throws Throwable {
        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        when(pjp.proceed()).thenReturn("reportList");

        metricsAop.measureReportQuery(pjp);

        Timer timer = registry.find("momocity.report.query.duration").timer();
        assertThat(timer.count()).isEqualTo(1L);
    }
}
