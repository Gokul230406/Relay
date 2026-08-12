package com.streaksaver.submission;

import com.streaksaver.model.DailySubmissionGuard;
import com.streaksaver.model.GuardStatusEnum;
import com.streaksaver.model.PlatformEnum;
import com.streaksaver.repository.DailySubmissionGuardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class SubmissionGuardService {

    private static final Logger log = LoggerFactory.getLogger(SubmissionGuardService.class);
    private static final String LOCK_KEY_PREFIX = "lock:daily_submission:";

    private final DailySubmissionGuardRepository guardRepository;
    private final StringRedisTemplate redisTemplate;

    public SubmissionGuardService(DailySubmissionGuardRepository guardRepository, StringRedisTemplate redisTemplate) {
        this.guardRepository = guardRepository;
        this.redisTemplate = redisTemplate;
    }

    public boolean isSubmissionAttemptedToday(String userId, LocalDate date) {
        return guardRepository.existsByUserIdAndDate(userId, date);
    }

    public Optional<DailySubmissionGuard> getTodayGuardRecord(String userId, LocalDate date) {
        return guardRepository.findByUserIdAndDate(userId, date);
    }

    public boolean acquireLock(String userId, LocalDate date, long timeoutSeconds) {
        String lockKey = LOCK_KEY_PREFIX + userId + ":" + date;
        try {
            Boolean success = redisTemplate.opsForValue().setIfAbsent(lockKey, "LOCKED", Duration.ofSeconds(timeoutSeconds));
            return Boolean.TRUE.equals(success);
        } catch (Exception e) {
            log.warn("REDIS_LOCK_WARNING Failed to reach Redis for lock, falling back to database guard. error={}", e.getMessage());
            return true;
        }
    }

    public void releaseLock(String userId, LocalDate date) {
        String lockKey = LOCK_KEY_PREFIX + userId + ":" + date;
        try {
            redisTemplate.delete(lockKey);
        } catch (Exception e) {
            log.warn("REDIS_LOCK_RELEASE_ERROR lockKey={} error={}", lockKey, e.getMessage());
        }
    }

    public DailySubmissionGuard createGuardRecord(String userId, LocalDate date, PlatformEnum selectedPlatform) {
        log.info("DAILY_GUARD_CREATED userId={} date={} platform={}", userId, date, selectedPlatform);

        Optional<DailySubmissionGuard> existing = guardRepository.findByUserIdAndDate(userId, date);
        if (existing.isPresent()) {
            DailySubmissionGuard g = existing.get();
            g.setSelectedPlatform(selectedPlatform);
            g.setStatus(GuardStatusEnum.PENDING);
            g.setAttemptStartedAt(Instant.now());
            return guardRepository.save(g);
        }
        
        DailySubmissionGuard guard = DailySubmissionGuard.builder()
                .userId(userId)
                .date(date)
                .selectedPlatform(selectedPlatform)
                .status(GuardStatusEnum.PENDING)
                .attemptStartedAt(Instant.now())
                .build();

        try {
            return guardRepository.save(guard);
        } catch (DuplicateKeyException e) {
            log.info("REUSING_GUARD_RECORD userId={} date={}", userId, date);
            return guardRepository.findByUserIdAndDate(userId, date).orElse(guard);
        }
    }

    public DailySubmissionGuard updateGuardRecord(DailySubmissionGuard guard) {
        return guardRepository.save(guard);
    }
}
