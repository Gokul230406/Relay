package com.streaksaver.repository;

import com.streaksaver.model.PlatformConfiguration;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlatformConfigurationRepository extends MongoRepository<PlatformConfiguration, String> {
    Optional<PlatformConfiguration> findByUserId(String userId);
}
