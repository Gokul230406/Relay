package com.streaksaver.controller;

import com.streaksaver.model.SubmissionHistory;
import com.streaksaver.repository.SubmissionHistoryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/history")
public class HistoryController {

    private final SubmissionHistoryRepository historyRepository;

    public HistoryController(SubmissionHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    @GetMapping
    public ResponseEntity<List<SubmissionHistory>> getHistory(Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        List<SubmissionHistory> list = historyRepository.findByUserIdOrderByTimestampDesc(userId);
        if (list == null || list.isEmpty()) {
            list = historyRepository.findByUserIdOrderByDateDesc(userId);
        }

        // Sanitize legacy history records to fix contradiction between botAction and checklist status
        for (SubmissionHistory item : list) {
            String action = (item.getBotAction() != null ? item.getBotAction() : "") + " " + (item.getDetails() != null ? item.getDetails() : "");
            
            if (action.contains("LeetCode") && item.getSelectedPlatform() != null && item.getSelectedPlatform().name().equals("LEETCODE")) {
                item.setLeetCodeSubmitted(true);
            }
            if (action.contains("CodeChef")) {
                item.setCodeChefSubmitted(true);
            }
            if (action.contains("GeeksforGeeks") || action.contains("GFG")) {
                item.setGfgSubmitted(true);
            }
            
            // If action was successful submit, mark at least the target platform as submitted
            if (item.getSubmissionStatus() != null && item.getSubmissionStatus().name().equals("SUCCESS")) {
                if (item.getSelectedPlatform() != null) {
                    switch (item.getSelectedPlatform()) {
                        case LEETCODE -> item.setLeetCodeSubmitted(true);
                        case CODECHEF -> item.setCodeChefSubmitted(true);
                        case GEEKSFORGEEKS -> item.setGfgSubmitted(true);
                    }
                } else {
                    item.setLeetCodeSubmitted(true);
                }
            }
            historyRepository.save(item);
        }

        return ResponseEntity.ok(list);
    }
}
