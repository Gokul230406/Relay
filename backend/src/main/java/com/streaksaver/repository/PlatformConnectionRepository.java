package com.streaksaver.repository;

import com.streaksaver.model.PlatformConnection;
import com.streaksaver.model.PlatformEnum;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlatformConnectionRepository extends MongoRepository<PlatformConnection, String> {
    List<PlatformConnection> findByUserId(String userId);
    Optional<PlatformConnection> findByUserIdAndPlatform(String userId, PlatformEnum platform);
    void deleteByUserIdAndPlatform(String userId, PlatformEnum platform);
}
