package com.streaksaver.controller;

import com.streaksaver.dto.PlatformConnectionDTO;
import com.streaksaver.model.PlatformConnection;
import com.streaksaver.model.PlatformEnum;
import com.streaksaver.repository.PlatformConnectionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/platforms")
public class PlatformController {

    private final PlatformConnectionRepository platformConnectionRepository;

    public PlatformController(PlatformConnectionRepository platformConnectionRepository) {
        this.platformConnectionRepository = platformConnectionRepository;
    }

    @GetMapping("/status")
    public ResponseEntity<List<PlatformConnectionDTO>> getPlatformStatuses(Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        List<PlatformConnection> connections = platformConnectionRepository.findByUserId(userId);

        List<PlatformConnectionDTO> dtoList = new ArrayList<>();
        for (PlatformConnection conn : connections) {
            dtoList.add(PlatformConnectionDTO.builder()
                    .platform(conn.getPlatform())
                    .platformUsername(conn.getPlatformUsername())
                    .connected(conn.isConnected())
                    .connectionMessage(conn.getConnectionMessage())
                    .build());
        }
        return ResponseEntity.ok(dtoList);
    }

    @PostMapping("/{platform}/connect")
    public ResponseEntity<PlatformConnectionDTO> connectPlatform(
            @PathVariable("platform") String platformStr,
            @RequestBody ConnectionRequest body,
            Authentication authentication) {
        
        String userId = (String) authentication.getPrincipal();
        PlatformEnum platform = PlatformEnum.valueOf(platformStr.toUpperCase());

        PlatformConnection conn = platformConnectionRepository.findByUserIdAndPlatform(userId, platform)
                .orElseGet(() -> PlatformConnection.builder()
                        .userId(userId)
                        .platform(platform)
                        .build());

        conn.setPlatformUsername(body.username());
        conn.setConnected(true);
        conn.setConnectionMessage("Connected to " + platform.getDisplayName());
        conn.setLastVerifiedAt(Instant.now());

        platformConnectionRepository.save(conn);

        return ResponseEntity.ok(PlatformConnectionDTO.builder()
                .platform(conn.getPlatform())
                .platformUsername(conn.getPlatformUsername())
                .connected(true)
                .connectionMessage(conn.getConnectionMessage())
                .build());
    }

    @DeleteMapping("/{platform}/disconnect")
    public ResponseEntity<PlatformConnectionDTO> disconnectPlatform(
            @PathVariable("platform") String platformStr,
            Authentication authentication) {

        String userId = (String) authentication.getPrincipal();
        PlatformEnum platform = PlatformEnum.valueOf(platformStr.toUpperCase());

        PlatformConnection conn = platformConnectionRepository.findByUserIdAndPlatform(userId, platform)
                .orElseGet(() -> PlatformConnection.builder()
                        .userId(userId)
                        .platform(platform)
                        .build());

        conn.setConnected(false);
        conn.setConnectionMessage("Disconnected");
        platformConnectionRepository.save(conn);

        return ResponseEntity.ok(PlatformConnectionDTO.builder()
                .platform(platform)
                .platformUsername(conn.getPlatformUsername())
                .connected(false)
                .connectionMessage("Disconnected")
                .build());
    }

    public record ConnectionRequest(String username, String authToken) {}
}
