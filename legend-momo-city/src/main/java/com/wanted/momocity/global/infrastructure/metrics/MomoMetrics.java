package com.wanted.momocity.global.infrastructure.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class MomoMetrics {

    private final MeterRegistry meterRegistry;

    // ===== Timer =====
    private final Timer s3UploadTimer;
    private final Timer blacklistCheckTimer;
    private final Timer enrollmentTimer;
    private final Timer friendListTimer;
    private final Timer messageHistoryTimer;
    private final Timer chatRoomListTimer;
    private final Timer lectureUploadTimer;
    private final Timer lectureListTimer;
    private final Timer authLoginTimer;
    private final Timer userDetailTimer;
    private final Timer userUpdateTimer;
    private final Timer teacherApplicationListTimer;
    private final Timer teacherApplicationDetailTimer;
    private final Timer adminUserListTimer;


    // ===== Admin BC =====
    /* comment. 대시보드 : 외부 BC 3개 × DB쿼리 5회 크로스호출 — 병목 측정 핵심 */
    private final Timer adminDashboardTimer;
    /* comment. 에러로그 : admin 자체 도메인 단순조회, 베이스라인 측정용 */
    private final Timer adminErrorLogQueryTimer;

    // ===== Report BC =====
    /* comment. 신고 접수 소요시간 + 성공 횟수 카운터 (성공 시에만 increment) */
    private final Timer reportSubmitTimer;
    private final Timer reportQueryTimer;
    private final Counter reportSubmitCounter;

    // ===== Counter =====
    private final Counter s3UploadFailCounter;


    public MomoMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // Timer: S3 업로드 소요 시간
        this.s3UploadTimer = Timer.builder("momocity.s3.upload.duration")
                .description("S3 파일 업로드 소요 시간")
                .register(meterRegistry);

        // Timer: 블랙리스트 Redis 조회 소요 시간
        // 모든 API 요청마다 실행 — S3 presigned URL 발급 시 135ms 소요 확인됨
        this.blacklistCheckTimer = Timer.builder("momocity.blacklist.check.duration")
                .description("요청마다 실행되는 블랙리스트 Redis 조회 소요 시간")
                .register(meterRegistry);


        // Timer: 수강 신청 소요 시간
        this.enrollmentTimer = Timer.builder("momocity.enrollment.duration")
                .description("수강 신청 소요 시간")
                .register(meterRegistry);

        // Timer: 친구 목록 조회 소요 시간
        // 93ms, SQL 10개, N+1 발생 확인됨 — 최적화 전후 비교용
        this.friendListTimer = Timer.builder("momocity.friend.list.duration")
                .description("친구 목록 조회 소요 시간 - N+1 최적화 전후 비교")
                .register(meterRegistry);

        // Timer: 메시지 내역 조회 소요 시간
        // 171ms, SQL 14개, N+1 발생 확인됨 — 최적화 before/after 비교용
        this.messageHistoryTimer = Timer.builder("momocity.message.history.duration")
                .description("메시지 내역 조회 소요 시간 - N+1 최적화 전후 비교")
                .register(meterRegistry);

        // Timer: 채팅방 목록 조회 소요 시간
        // 137ms, SQL 14개 확인됨
        this.chatRoomListTimer = Timer.builder("momocity.chatroom.list.duration")
                .description("채팅방 목록 조회 소요 시간")
                .register(meterRegistry);

        // Timer: 강의 등록 소요 시간
        // 트래픽 몰림, 영상 길이에 따른 S3 업로드 포함 전체 시간 측정
        this.lectureUploadTimer = Timer.builder("momocity.lecture.upload.duration")
                .description("강의 등록 소요 시간 - S3 업로드 포함")
                .register(meterRegistry);

        // Timer: 강의 목록 조회 소요 시간
        this.lectureListTimer = Timer.builder("momocity.lecture.list.duration")
                .description("강의 목록 조회 소요 시간")
                .register(meterRegistry);

        // Counter: S3 업로드 실패 횟수
        this.s3UploadFailCounter = Counter.builder("momocity.s3.upload.failed")
                .description("S3 파일 업로드 실패 횟수")
                .register(meterRegistry);

        // Timer : 로그인 소요 시간
        this.authLoginTimer = Timer.builder("momocity.auth.login.duration")
                .description("로그인 소요 시간 - AuthenticationManager + Redis 저장 포함")
                .register(meterRegistry);

        // Timer
        this.userDetailTimer = Timer.builder("momocity.user.detail.duration")
                .description("마이페이지 조회 소요 시간")
                .register(meterRegistry);

        // Timer
        this.userUpdateTimer = Timer.builder("momocity.user.update.duration")
                .description("유저 정보 수정 소요 시간")
                .register(meterRegistry);

        // Timer
        this.teacherApplicationListTimer = Timer.builder("momocity.teacher.application.list.duration")
                .description("대기 강사 목록 조회 소요 시간")
                .register(meterRegistry);

        // Timer
        this.teacherApplicationDetailTimer = Timer.builder("momocity.teacher.application.detail.duration")
                .description("강사 신청 상세 조회 소요 시간")
                .register(meterRegistry);

        // Timer
        this.adminUserListTimer = Timer.builder("momocity.admin.user.list.duration")
                .description("관리자 회원 목록 조회 소요 시간 - Redis 캐싱 전후 비교")
                .register(meterRegistry);

        // Admin BC
        this.adminDashboardTimer = Timer.builder("momocity.admin.dashboard.duration")
                .description("대시보드 요약 조회 소요 시간 - 크로스 BC 쿼리 5회")
                .register(meterRegistry);

        this.adminErrorLogQueryTimer = Timer.builder("momocity.admin.errorlog.query.duration")
                .description("에러로그 최근 조회 소요 시간")
                .register(meterRegistry);

        // Report BC
        this.reportSubmitTimer = Timer.builder("momocity.report.submit.duration")
                .description("신고 접수 소요 시간")
                .register(meterRegistry);

        this.reportQueryTimer = Timer.builder("momocity.report.query.duration")
                .description("신고 조회 소요 시간 - getRecent / getByStatus 공용")
                .register(meterRegistry);

        this.reportSubmitCounter = Counter.builder("momocity.report.submit.total")
                .description("신고 접수 성공 횟수")
                .register(meterRegistry);
    }

    // 작업 시작 시점의 시간을 기억
    // 실제 Timer는 작업이 끝난 뒤 sample.stop(timer)를 호출할 때 결정된다.
    // try-finally로 감싸서 성공/실패 여부와 관계없이 시간을 기록할 수 있다.
    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }

    // S3 업로드 전체 소요 시간을 기록
    public void stopS3UploadTimer(Timer.Sample sample) {
        sample.stop(s3UploadTimer);
    }

    // S3 업로드 실패 횟수를 기록
    public void recordS3UploadFailed() {
        s3UploadFailCounter.increment();
    }

    // 블랙리스트 Redis 조회 소요 시간을 기록
    // 모든 API 요청마다 실행되므로 누적 지연이 전체 응답시간에 미치는 영향을 파악 가능
    public void stopBlacklistCheckTimer(Timer.Sample sample) {
        sample.stop(blacklistCheckTimer);
    }

    // 수강 신청 소요 시간 기록
    public void stopEnrollmentTimer(Timer.Sample sample) {
        sample.stop(enrollmentTimer);
    }

    // 친구 목록 조회 소요 시간 기록
    public void stopFriendListTimer(Timer.Sample sample) {
        sample.stop(friendListTimer);
    }

    // 메시지 내역 조회 소요 시간 기록
    public void stopMessageHistoryTimer(Timer.Sample sample) {
        sample.stop(messageHistoryTimer);
    }

    // 채팅방 목록 조회 소요 시간 기록
    public void stopChatRoomListTimer(Timer.Sample sample) {
        sample.stop(chatRoomListTimer);
    }

    // 강의 등록 소요 시간 기록
    public void stopLectureUploadTimer(Timer.Sample sample) {
        sample.stop(lectureUploadTimer);
    }

    // 강의 목록 조회 소요 시간 기록
    public void stopLectureListTimer(Timer.Sample sample) {
        sample.stop(lectureListTimer);
    }

    // 로그인 소요 시간 기록
    public void stopAuthLoginTimer(Timer.Sample sample) {
        sample.stop(authLoginTimer);
    }

    public void stopUserDetailTimer(Timer.Sample sample) { sample.stop(userDetailTimer); }

    public void stopUserUpdateTimer(Timer.Sample sample) { sample.stop(userUpdateTimer); }

    public void stopTeacherApplicationListTimer(Timer.Sample sample) { sample.stop(teacherApplicationListTimer); }

    public void stopTeacherApplicationDetailTimer(Timer.Sample sample) { sample.stop(teacherApplicationDetailTimer); }

    public void stopAdminUserListTimer(Timer.Sample sample) { sample.stop(adminUserListTimer); }

    // Admin BC
    public void stopAdminDashboardTimer(Timer.Sample sample) { sample.stop(adminDashboardTimer); }

    public void stopAdminErrorLogQueryTimer(Timer.Sample sample) { sample.stop(adminErrorLogQueryTimer); }

    // Report BC
    public void stopReportSubmitTimer(Timer.Sample sample) { sample.stop(reportSubmitTimer); }

    public void stopReportQueryTimer(Timer.Sample sample) { sample.stop(reportQueryTimer); }

    /* comment. 성공 시에만 호출 — proceed() 후 finally 전에 increment 해서 실패는 카운트 제외 */
    public void incrementReportSubmitCounter() { reportSubmitCounter.increment(); }
}