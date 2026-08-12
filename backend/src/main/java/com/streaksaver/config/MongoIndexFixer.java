package com.streaksaver.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
public class MongoIndexFixer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(MongoIndexFixer.class);
    private final MongoTemplate mongoTemplate;

    public MongoIndexFixer(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void run(String... args) {
        try {
            mongoTemplate.getCollection("submission_histories").dropIndexes();
            log.info("MONGO_INDEX_FIX_SUCCESS Dropped all legacy unique indexes from submission_histories collection");
        } catch (Exception e) {
            log.warn("MONGO_INDEX_FIX_WARN Could not drop indexes: {}", e.getMessage());
        }
    }
}
