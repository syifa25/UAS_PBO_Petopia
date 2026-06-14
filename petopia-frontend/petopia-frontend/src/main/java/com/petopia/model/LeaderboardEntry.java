package com.petopia.model;

public class LeaderboardEntry {
    private final int rank;
    private final String username;
    private final int win;
    private final int lose;
    private final int score;
    private final String lastEnemy;

    public LeaderboardEntry(int rank, String username, int win, int lose, int score, String lastEnemy) {
        this.rank = rank;
        this.username = username;
        this.win = win;
        this.lose = lose;
        this.score = score;
        this.lastEnemy = lastEnemy;
    }

    public int getRank() {
        return rank;
    }

    public String getUsername() {
        return username;
    }

    public int getWin() {
        return win;
    }

    public int getLose() {
        return lose;
    }

    public int getScore() {
        return score;
    }

    public String getLastEnemy() {
        return lastEnemy;
    }
}