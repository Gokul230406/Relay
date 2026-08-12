package com.streaksaver.repository;

import com.streaksaver.model.SchedulerConfiguration;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SchedulerConfigurationRepository extends MongoRepository<SchedulerConfiguration, String> {
    Optional<SchedulerConfiguration> findByUserId(String userId);
    List<SchedulerConfiguration> findByEnabledTrue();
}
