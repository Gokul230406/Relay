package com.streaksaver.repository;

import com.streaksaver.model.SubmissionHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmissionHistoryRepository extends MongoRepository<SubmissionHistory, String> {
    List<SubmissionHistory> findByUserIdOrderByDateDesc(String userId);
    List<SubmissionHistory> findByUserIdOrderByTimestampDesc(String userId);
}
