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
            
            // Query strictly against the live profile API for target handle
            PlatformStatusResult status = adapter.checkSubmissionStatus(handle, date);
            resultMap.put(platform, status);

            Optional<DailyPlatformStatus> existingOpt = dailyPlatformStatusRepository
                    .findByUserIdAndDateAndPlatform(userId, date, platform);

            DailyPlatformStatus statusDoc = existingOpt.orElseGet(() -> DailyPlatformStatus.builder()
                    .userId(userId)
                    .date(today(date))
                    .platform(platform)
                    .build());

            statusDoc.setSubmitted(status.isSubmittedToday());
            statusDoc.setStreakCount(status.getStreakCount());
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

    private LocalDate today(LocalDate date) {
        return date != null ? date : LocalDate.now();
    }
}
