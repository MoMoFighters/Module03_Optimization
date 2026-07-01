package com.wanted.momocity.global.metrics;

import com.wanted.momocity.global.infrastructure.metrics.MomoMetrics;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MomoMetricsTest {

    private SimpleMeterRegistry registry;
    private MomoMetrics momoMetrics;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        momoMetrics = new MomoMetrics(registry);
    }

    // ===== Admin BC =====

    @Test
    @DisplayName("Admin 대시보드 타이머 - stop 호출 시 count 1 증가")
    void adminDashboard_timerCountIncreases() {
        Timer.Sample sample = momoMetrics.startTimer();
        momoMetrics.stopAdminDashboardTimer(sample);

        Timer timer = registry.find("momocity.admin.dashboard.duration").timer();
        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Admin 에러로그 타이머 - stop 호출 시 count 1 증가")
    void adminErrorLogQuery_timerCountIncreases() {
        Timer.Sample sample = momoMetrics.startTimer();
        momoMetrics.stopAdminErrorLogQueryTimer(sample);

        Timer timer = registry.find("momocity.admin.errorlog.query.duration").timer();
        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1L);
    }

    // ===== Report BC =====

    @Test
    @DisplayName("신고 접수 타이머 - stop 호출 시 count 1 증가")
    void reportSubmit_timerCountIncreases() {
        Timer.Sample sample = momoMetrics.startTimer();
        momoMetrics.stopReportSubmitTimer(sample);

        Timer timer = registry.find("momocity.report.submit.duration").timer();
        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1L);
    }

    @Test
    @DisplayName("신고 접수 카운터 - increment 1회 시 count 1")
    void reportSubmit_counterIncreasesOnce() {
        momoMetrics.incrementReportSubmitCounter();

        Counter counter = registry.find("momocity.report.submit.total").counter();
        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("신고 접수 카운터 - increment 3회 시 count 3")
    void reportSubmit_counterIncreasesThreeTimes() {
        momoMetrics.incrementReportSubmitCounter();
        momoMetrics.incrementReportSubmitCounter();
        momoMetrics.incrementReportSubmitCounter();

        Counter counter = registry.find("momocity.report.submit.total").counter();
        assertThat(counter.count()).isEqualTo(3.0);
    }

    @Test
    @DisplayName("신고 조회 타이머 - stop 호출 시 count 1 증가")
    void reportQuery_timerCountIncreases() {
        Timer.Sample sample = momoMetrics.startTimer();
        momoMetrics.stopReportQueryTimer(sample);

        Timer timer = registry.find("momocity.report.query.duration").timer();
        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1L);
    }

    @Test
    @DisplayName("신고 접수 실패 시 - 타이머는 기록되지만 카운터는 0")
    void reportSubmit_exceptionStillRecordsTimerButNotCounter() {
        Timer.Sample sample = momoMetrics.startTimer();
        try {
            throw new RuntimeException("submit fail");
        } catch (RuntimeException ignored) {
            // counter는 호출 안 함
        } finally {
            momoMetrics.stopReportSubmitTimer(sample);
        }

        Timer timer = registry.find("momocity.report.submit.duration").timer();
        Counter counter = registry.find("momocity.report.submit.total").counter();
        assertThat(timer.count()).isEqualTo(1L);
        assertThat(counter.count()).isEqualTo(0.0);
    }
}
