package com.streaksaver.repository;

import com.streaksaver.model.DailyPlatformStatus;
import com.streaksaver.model.PlatformEnum;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyPlatformStatusRepository extends MongoRepository<DailyPlatformStatus, String> {
    List<DailyPlatformStatus> findByUserIdAndDate(String userId, LocalDate date);
    Optional<DailyPlatformStatus> findByUserIdAndDateAndPlatform(String userId, LocalDate date, PlatformEnum platform);
}
