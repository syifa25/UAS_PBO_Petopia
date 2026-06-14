package com.petopia.dto;

public class LeaderboardEntry {

    private int    rank;
    private String username;
    private int    win;
    private int    lose;
    private int    score;
    private String lastEnemy;

    public LeaderboardEntry(int rank, String username, int win, int lose, String lastEnemy) {
        this.rank      = rank;
        this.username  = username;
        this.win       = win;
        this.lose      = lose;
        this.score     = (win * 100) - (lose * 30);
        this.lastEnemy = lastEnemy;
    }

    public int    getRank()       { return rank; }
    public String getUsername()   { return username; }
    public int    getWin()        { return win; }
    public int    getLose()       { return lose; }
    public int    getScore()      { return score; }
    public String getLastEnemy()  { return lastEnemy; }
}
