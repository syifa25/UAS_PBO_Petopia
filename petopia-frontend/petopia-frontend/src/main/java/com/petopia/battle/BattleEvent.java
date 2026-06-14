package com.petopia.battle;

public class BattleEvent {
    public enum Type { LOG, HP_UPDATE, BATTLE_END, ENEMY_ATTACK }

    private final Type type;
    private final String message;    // untuk LOG atau BATTLE_END
    private final int actor;         // 0 = player, 1 = enemy (untuk HP_UPDATE)
    private final int hp;            // nilai HP sekarang (untuk HP_UPDATE)
    private final int maxHp;         // max HP (untuk HP_UPDATE)

    private BattleEvent(Type type, String message, int actor, int hp, int maxHp) {
        this.type = type;
        this.message = message;
        this.actor = actor;
        this.hp = hp;
        this.maxHp = maxHp;
    }

    public static BattleEvent log(String message) {
        return new BattleEvent(Type.LOG, message, -1, -1, -1);
    }

    public static BattleEvent hpUpdate(int actor, int hp, int maxHp) {
        return new BattleEvent(Type.HP_UPDATE, null, actor, hp, maxHp);
    }

    public static BattleEvent end(String message) {
        return new BattleEvent(Type.BATTLE_END, message, -1, -1, -1);
    }

    public static BattleEvent enemyAttack() {
        return new BattleEvent(Type.ENEMY_ATTACK, null, -1, -1, -1);
    }

    public Type getType() { return type; }
    public String getMessage() { return message; }
    public int getActor() { return actor; }
    public int getHp() { return hp; }
    public int getMaxHp() { return maxHp; }
}