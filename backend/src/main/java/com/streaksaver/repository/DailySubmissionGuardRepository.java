package com.streaksaver.repository;

import com.streaksaver.model.DailySubmissionGuard;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DailySubmissionGuardRepository extends MongoRepository<DailySubmissionGuard, String> {
    Optional<DailySubmissionGuard> findByUserIdAndDate(String userId, LocalDate date);
    boolean existsByUserIdAndDate(String userId, LocalDate date);
}
