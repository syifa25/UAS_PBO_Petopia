package com.petopia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class BattleResultRequest {

    // optional — null means guest battle
    private Long playerId;

    @NotBlank(message = "Opponent name is required")
    private String opponentName;

    @NotBlank(message = "Winner is required")
    @Pattern(regexp = "WIN|LOSS|FLED", message = "Winner must be WIN, LOSS or FLED")
    private String winner;

    private long durationMs;

    public Long   getPlayerId()              { return playerId; }
    public void   setPlayerId(Long v)        { this.playerId = v; }
    public String getOpponentName()          { return opponentName; }
    public void   setOpponentName(String v)  { this.opponentName = v; }
    public String getWinner()                { return winner; }
    public void   setWinner(String v)        { this.winner = v; }
    public long   getDurationMs()            { return durationMs; }
    public void   setDurationMs(long v)      { this.durationMs = v; }
}
