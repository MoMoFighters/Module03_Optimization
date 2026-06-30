package com.wanted.momocity.auth.infrastructure.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class AuthMetrics {

    private final MeterRegistry meterRegistry;

    // 로그인 실패 - reason 태그로 원인 구분
    // bad_credentials: 비번 틀림
    // user_not_found: 이메일 없음
    // inactive_banned: 정지 계정
    // inactive_pending: 승인 대기
    // inactive_rejected: 반려 계정
    // temp_pwd_expired: 임시비번 만료

    // 비활성 계정 로그인 시도 횟수
    // BANNED/PENDING/REJECTED 각각 태그로 구분

    public AuthMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

    }

    // 로그인 실패 - reason 태그로 원인 구분
    public void recordLoginFailed(String reason) {
        Counter.builder("momocity.auth.login.failed")
                .tag("reason", reason)
                .description("로그인 실패 횟수 - reason 태그로 원인 구분")
                .register(meterRegistry)
                .increment();
    }

    // 비활성 계정 로그인 시도 - status 태그로 구분
    public void recordLoginInactive(String status) {
        Counter.builder("momocity.auth.login.inactive")
                .tag("status", status)
                .description("비활성 계정 로그인 시도 횟수 - BANNED/PENDING/REJECTED 구분")
                .register(meterRegistry)
                .increment();
    }


}
