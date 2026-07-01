package com.wanted.momocity.viewing.infrastructure.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class ViewingMetrics {

    private final Counter optimisticLockConflictCounter;
    private final Counter chapterCompletedCounter;
    private final Counter cacheHitCounter;
    private final Counter cacheMissCounter;
    private final Counter skipBlockedCounter;
    private final Timer saveProgressTimer;
    private final Timer s3PresignedUrlTimer;

    public ViewingMetrics(MeterRegistry meterRegistry) {

        this.optimisticLockConflictCounter = Counter.builder("momocity.viewing.optimistic.lock.conflict")
                .description("낙관적 락 충돌 횟수 - 트래픽 집중 신호 감지")
                .register(meterRegistry);

        this.chapterCompletedCounter = Counter.builder("momocity.viewing.chapter.completed")
                .description("챕터 완료 횟수 - 학습 전환율 지표")
                .register(meterRegistry);

        this.cacheHitCounter = Counter.builder("momocity.viewing.cache.hit")
                .description("캐시 히트 횟수 - Redis 캐싱 효과 측정")
                .register(meterRegistry);

        this.cacheMissCounter = Counter.builder("momocity.viewing.cache.miss")
                .description("캐시 미스 횟수 - DB 조회 빈도 측정")
                .register(meterRegistry);

        this.skipBlockedCounter = Counter.builder("momocity.viewing.skip.blocked")
                .description("10초 초과 건너뛰기 차단 횟수 - 어뷰징 패턴 감지")
                .register(meterRegistry);

        this.saveProgressTimer = Timer.builder("momocity.viewing.save.progress.time")
                .description("진척도 저장 처리 시간 - 가장 빈번한 로직 성능 측정")
                // p95/p99 확인용 (after 비교)
                .publishPercentiles(0.95, 0.99)
                .register(meterRegistry);

        this.s3PresignedUrlTimer = Timer.builder("momocity.viewing.s3.presigned.url.time")
                .description("S3 Presigned URL 발급 시간 - 외부 호출 성능 및 AWS 장애 감지")
                .register(meterRegistry);
    }

    public void recordOptimisticLockConflict() { optimisticLockConflictCounter.increment(); }
    public void recordChapterCompleted() { chapterCompletedCounter.increment(); }
    public void recordCacheHit() { cacheHitCounter.increment(); }
    public void recordCacheMiss() { cacheMissCounter.increment(); }
    public void recordSkipBlocked() { skipBlockedCounter.increment(); }
    public Timer getSaveProgressTimer() { return saveProgressTimer; }
    public Timer getS3PresignedUrlTimer() { return s3PresignedUrlTimer; }
}