package com.petopia.service;

import com.petopia.dto.BattleResultRequest;
import com.petopia.dto.LeaderboardEntry;
import com.petopia.model.Battle;
import com.petopia.model.User;
import com.petopia.repository.BattleRepository;
import com.petopia.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class BattleService {

    private final BattleRepository battleRepository;
    private final UserRepository   userRepository;

    public BattleService(BattleRepository battleRepository, UserRepository userRepository) {
        this.battleRepository = battleRepository;
        this.userRepository   = userRepository;
    }

    // ── Save battle result ───────────────────────────────────
    @Transactional
    public Battle saveBattleResult(BattleResultRequest req) {
        User player = null;
        if (req.getPlayerId() != null) {
            player = userRepository.findById(req.getPlayerId()).orElse(null);
        }
        Battle battle = new Battle(player, req.getOpponentName(),
                                   req.getWinner(), req.getDurationMs());
        return battleRepository.save(battle);
    }

    // ── Leaderboard ──────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<LeaderboardEntry> getLeaderboard() {
        List<Object[]> rows = battleRepository.findLeaderboardData();
        List<LeaderboardEntry> result = new ArrayList<>();
        int rank = 1;
        for (Object[] row : rows) {
            String username    = (String) row[0];
            int    winCount    = ((Number) row[1]).intValue();
            int    loseCount   = ((Number) row[2]).intValue();
            String lastEnemy   = (String) row[4];
            result.add(new LeaderboardEntry(rank++, username, winCount, loseCount, lastEnemy));
        }
        return result;
    }
}
