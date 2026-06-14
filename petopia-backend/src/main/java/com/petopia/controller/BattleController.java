package com.petopia.controller;

import com.petopia.dto.BattleResultRequest;
import com.petopia.dto.LeaderboardEntry;
import com.petopia.service.BattleService;
import com.petopia.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/battles")
public class BattleController {

    private final BattleService battleService;
    private final UserService   userService;

    public BattleController(BattleService battleService, UserService userService) {
        this.battleService = battleService;
        this.userService   = userService;
    }

    // POST /api/battles/result
    // Saves a battle result. Requires X-Auth-Token header if player is logged in.
    @PostMapping("/result")
    public ResponseEntity<?> saveResult(
            @Valid @RequestBody BattleResultRequest req,
            @RequestHeader(value = "X-Auth-Token", required = false) String token) {

        // If token is provided, validate it
        if (token != null && !token.isBlank() && !userService.isValidToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid or expired session token."));
        }

        try {
            battleService.saveBattleResult(req);
            return ResponseEntity.ok(Map.of("message", "Battle result saved."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to save battle result."));
        }
    }

    // GET /api/battles/leaderboard
    @GetMapping("/leaderboard")
    public ResponseEntity<List<LeaderboardEntry>> getLeaderboard() {
        return ResponseEntity.ok(battleService.getLeaderboard());
    }
}
