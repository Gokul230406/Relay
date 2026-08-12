package com.streaksaver.controller;

import com.streaksaver.dto.ProblemPoolDTO;
import com.streaksaver.model.PlatformEnum;
import com.streaksaver.model.ProblemPool;
import com.streaksaver.repository.ProblemPoolRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/problem-pool")
public class ProblemPoolController {

    private final ProblemPoolRepository problemPoolRepository;

    public ProblemPoolController(ProblemPoolRepository problemPoolRepository) {
        this.problemPoolRepository = problemPoolRepository;
    }

    @GetMapping
    public ResponseEntity<List<ProblemPoolDTO>> getAllPoolItems(Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        List<ProblemPool> items = problemPoolRepository.findByUserId(userId);
        
        List<ProblemPoolDTO> dtoList = new ArrayList<>();
        for (ProblemPool item : items) {
            dtoList.add(ProblemPoolDTO.builder()
                    .id(item.getId())
                    .platform(item.getPlatform())
                    .problemId(item.getProblemId())
                    .problemTitle(item.getProblemTitle())
                    .language(item.getLanguage())
                    .solutionCode(item.getSolutionCode())
                    .targetUrl(item.getTargetUrl())
                    .active(item.isActive())
                    .build());
        }
        return ResponseEntity.ok(dtoList);
    }

    @PostMapping("/{platform}")
    public ResponseEntity<ProblemPoolDTO> addProblemToPool(
            @PathVariable("platform") String platformStr,
            @RequestBody ProblemPoolDTO dto,
            Authentication authentication) {

        String userId = (String) authentication.getPrincipal();
        PlatformEnum platform = PlatformEnum.valueOf(platformStr.toUpperCase());

        String problemId = (dto.getProblemId() != null && !dto.getProblemId().isBlank())
                ? dto.getProblemId()
                : "prob_" + UUID.randomUUID().toString().substring(0, 6);

        ProblemPool pool = ProblemPool.builder()
                .userId(userId)
                .platform(platform)
                .problemId(problemId)
                .problemTitle(dto.getProblemTitle() != null ? dto.getProblemTitle() : "User Approved Solution")
                .language(dto.getLanguage() != null ? dto.getLanguage() : "python3")
                .solutionCode(dto.getSolutionCode() != null ? dto.getSolutionCode() : "# Approved submission code")
                .targetUrl(dto.getTargetUrl())
                .active(true)
                .build();

        pool = problemPoolRepository.save(pool);

        return ResponseEntity.ok(ProblemPoolDTO.builder()
                .id(pool.getId())
                .platform(pool.getPlatform())
                .problemId(pool.getProblemId())
                .problemTitle(pool.getProblemTitle())
                .language(pool.getLanguage())
                .solutionCode(pool.getSolutionCode())
                .targetUrl(pool.getTargetUrl())
                .active(pool.isActive())
                .build());
    }

    @DeleteMapping("/{platform}/{id}")
    public ResponseEntity<Void> deleteProblemFromPool(
            @PathVariable("platform") String platformStr,
            @PathVariable("id") String id,
            Authentication authentication) {

        String userId = (String) authentication.getPrincipal();
        problemPoolRepository.findById(id).ifPresent(pool -> {
            if (pool.getUserId().equals(userId)) {
                problemPoolRepository.delete(pool);
            }
        });
        return ResponseEntity.noContent().build();
    }
}
