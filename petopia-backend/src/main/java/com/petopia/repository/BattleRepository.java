package com.petopia.repository;

import com.petopia.model.Battle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BattleRepository extends JpaRepository<Battle, Long> {

    /** Leaderboard: wins, losses, total battles per player, ordered by wins */
    @Query("""
        SELECT
            COALESCE(u.username, 'Guest')  AS username,
            SUM(CASE WHEN b.winner = 'WIN'  THEN 1 ELSE 0 END) AS winCount,
            SUM(CASE WHEN b.winner = 'LOSS' THEN 1 ELSE 0 END) AS loseCount,
            COUNT(b.id)                     AS totalBattles,
            MAX(b.opponentName)             AS lastEnemy
        FROM Battle b
        LEFT JOIN b.player u
        GROUP BY COALESCE(u.username, 'Guest')
        ORDER BY winCount DESC, totalBattles DESC
        """)
    List<Object[]> findLeaderboardData();
}
