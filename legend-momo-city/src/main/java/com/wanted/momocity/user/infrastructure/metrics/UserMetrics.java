package com.wanted.momocity.user.infrastructure.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class UserMetrics {

    private final MeterRegistry meterRegistry;

    private final Counter nicknameRegisterCounter;

    public UserMetrics( MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        this.nicknameRegisterCounter = Counter.builder("momocity.user.nickname.register")
                .description("닉네임 등록 완료 횟수 - 가입자 수 대비 낮으면 온보딩 이탈")
                .register(meterRegistry);
    }

    public void recordNicknameRegister() {
        nicknameRegisterCounter.increment();
    }

}