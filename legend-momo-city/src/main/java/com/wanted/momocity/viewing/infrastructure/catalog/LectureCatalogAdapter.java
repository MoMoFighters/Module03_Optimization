package com.wanted.momocity.viewing.infrastructure.catalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wanted.momocity.auth.application.port.LoadUserPort;
import com.wanted.momocity.global.domain.common.exception.DomainRuleViolationException;
import com.wanted.momocity.lecture.infrastructure.persistence.LectureJpaEntity;
import com.wanted.momocity.lecture.infrastructure.persistence.SpringDataLectureRepository;
import com.wanted.momocity.viewing.application.port.LecturePort;
import com.wanted.momocity.viewing.domain.model.Lecture;
import com.wanted.momocity.viewing.infrastructure.metrics.ViewingMetrics;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

import static reactor.netty.http.HttpConnectionLiveness.log;

/*
* comment.
*  [역할]
*  LecturePort 인터페이스 구현체
*  catalog 컨텍스트 소유의 Lecture 를 READ 전용으로 조회
*  -
*  [Redis 캐싱 전략]
 * @Cacheable("lecture") → lectureId 기준 단건 캐싱
 * -
 * 왜 캐싱이 필요한가:
 * -> getLectureMeta(), getMyLectures() 등 매번 DB 조회
 * -> Redis 캐싱으로 DB 부하 감소
 * -> 강의 정보는 자주 바뀌지 않아 캐싱 효과 극대화
 * -
 * TODO: 팀원 LectureJpaRepository 완성 후
 *       실제 DB 조회 코드로 교체
* */

@Component
@RequiredArgsConstructor
public class LectureCatalogAdapter implements LecturePort {

    // SpringDataLectureRepository 주입
    private final SpringDataLectureRepository springDataLectureRepository;
    // LoadUserPort 주입
    // → teacherId 로 강사 이름 조회할 때 사용
    private final LoadUserPort loadUserPort;
    private final RedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final ViewingMetrics viewingMetrics;

    /*
     * comment.
     *  강의 단건 조회
     *  @Cacheable("lecture")
     *  -> 처음 호출 시 DB 조회 후 Redis 에 저장
     *  -> 이후 호출 시 Redis 에서 반환 (DB 조회 없음)
     *  -> key = "lecture::1", "lecture::2" 형태로 저장
     */

    @Override
    public Lecture findById(Long lectureId) {

        String cacheKey = "lecture::" + lectureId;

        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                viewingMetrics.recordCacheHit();
                return objectMapper.convertValue(cached, Lecture.class);
            }
            viewingMetrics.recordCacheMiss();
        } catch (Exception e) {
            log.warn("[Viewing] lecture 캐시 조회 실패 | lectureId={}", lectureId);
        }

        LectureJpaEntity entity = springDataLectureRepository.findById(lectureId)
                .orElseThrow(() -> new DomainRuleViolationException("강의를 찾을 수 없습니다."));

        String instructorName = loadUserPort.findById(entity.getTeacherId())
                .map(user -> user.getName())
                .orElse("강사");

        Lecture lecture = Lecture.reconstitute(
                entity.getId(),
                entity.getTeacherId(),
                entity.getTitle(),
                entity.getThumbnailUrl(),
                entity.getCategory().name(),
                instructorName,
                entity.getStatus().name()
        );

        try {
            redisTemplate.opsForValue().set(cacheKey, lecture, Duration.ofHours(1));
        } catch (Exception e) {
            log.warn("[Viewing] lecture 캐시 저장 실패 | lectureId={}", lectureId);
        }

        return lecture;
    }

}
