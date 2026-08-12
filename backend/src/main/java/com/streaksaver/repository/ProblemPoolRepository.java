package com.streaksaver.repository;

import com.streaksaver.model.PlatformEnum;
import com.streaksaver.model.ProblemPool;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProblemPoolRepository extends MongoRepository<ProblemPool, String> {
    List<ProblemPool> findByUserId(String userId);
    List<ProblemPool> findByUserIdAndPlatform(String userId, PlatformEnum platform);
    List<ProblemPool> findByUserIdAndPlatformAndActiveTrue(String userId, PlatformEnum platform);
    Optional<ProblemPool> findByUserIdAndPlatformAndProblemId(String userId, PlatformEnum platform, String problemId);
}
