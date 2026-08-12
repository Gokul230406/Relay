package com.streaksaver.streak;

import com.streaksaver.model.DailyPlatformStatus;
import com.streaksaver.model.PlatformConnection;
import com.streaksaver.model.PlatformEnum;
import com.streaksaver.platform.CodingPlatformAdapter;
import com.streaksaver.platform.PlatformAdapterFactory;
import com.streaksaver.platform.PlatformStatusResult;
import com.streaksaver.repository.DailyPlatformStatusRepository;
import com.streaksaver.repository.PlatformConnectionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class StreakCheckerService {

    private static final Logger log = LoggerFactory.getLogger(StreakCheckerService.class);

    private final PlatformConnectionRepository platformConnectionRepository;
    private final DailyPlatformStatusRepository dailyPlatformStatusRepository;
    private final PlatformAdapterFactory adapterFactory;

    public StreakCheckerService(PlatformConnectionRepository platformConnectionRepository,
                                 DailyPlatformStatusRepository dailyPlatformStatusRepository,
                                 PlatformAdapterFactory adapterFactory) {
        this.platformConnectionRepository = platformConnectionRepository;
        this.dailyPlatformStatusRepository = dailyPlatformStatusRepository;
        this.adapterFactory = adapterFactory;
    }

    public Map<PlatformEnum, PlatformStatusResult> checkAllPlatforms(String userId, LocalDate date) {
        log.info("STREAK_CHECK_STARTED userId={} date={}", userId, date);

        List<PlatformConnection> connections = platformConnectionRepository.findByUserId(userId);
        Map<PlatformEnum, String> userPlatformHandles = new EnumMap<>(PlatformEnum.class);
        for (PlatformConnection conn : connections) {
            if (conn.isConnected()) {
                userPlatformHandles.put(conn.getPlatform(), conn.getPlatformUsername());
            }
        }

        Map<PlatformEnum, PlatformStatusResult> resultMap = new EnumMap<>(PlatformEnum.class);

        for (PlatformEnum platform : PlatformEnum.values()) {
            CodingPlatformAdapter adapter = adapterFactory.getAdapter(platform);
            String handle = userPlatformHandles.get(platform);
            
            PlatformStatusResult status = adapter.checkSubmissionStatus(handle, date);

            Optional<DailyPlatformStatus> existingOpt = dailyPlatformStatusRepository
                    .findByUserIdAndDateAndPlatform(userId, date, platform);

            boolean isSubmitted = status.isSubmittedToday() || existingOpt.map(DailyPlatformStatus::isSubmitted).orElse(false);
            int streakCount = Math.max(status.getStreakCount(), existingOpt.map(DailyPlatformStatus::getStreakCount).orElse(0));
            if (isSubmitted && streakCount == 0) {
                streakCount = 1;
            }

            PlatformStatusResult finalStatus = PlatformStatusResult.builder()
                    .platform(platform)
                    .username(status.getUsername())
                    .date(date)
                    .submittedToday(isSubmitted)
                    .streakCount(streakCount)
                    .totalSolved(status.getTotalSolved() + (isSubmitted ? 1 : 0))
                    .message(isSubmitted ? "Submission recorded today for " + status.getUsername() : "No submission detected today for " + status.getUsername())
                    .checkedAt(Instant.now())
                    .build();

            resultMap.put(platform, finalStatus);

            DailyPlatformStatus statusDoc = existingOpt.orElseGet(() -> DailyPlatformStatus.builder()
                    .userId(userId)
                    .date(date)
                    .platform(platform)
                    .build());

            statusDoc.setSubmitted(isSubmitted);
            statusDoc.setStreakCount(streakCount);
            statusDoc.setCheckedAt(Instant.now());
            dailyPlatformStatusRepository.save(statusDoc);
        }

        log.info("STREAK_CHECK_COMPLETED userId={} date={} leetcodeSubmitted={} codechefSubmitted={} gfgSubmitted={}",
                userId, date,
                resultMap.get(PlatformEnum.LEETCODE).isSubmittedToday(),
                resultMap.get(PlatformEnum.CODECHEF).isSubmittedToday(),
                resultMap.get(PlatformEnum.GEEKSFORGEEKS).isSubmittedToday());

        return resultMap;
    }
}
