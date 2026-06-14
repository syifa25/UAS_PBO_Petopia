package com.petopia.model;

public interface Combatant {
    String getName();
    String getImagePath();
    int getLevel();
    int getAttack();
    int getDefense();
    int getHp();
    int getMaxHp();
    boolean isFainted();
    void heal(int amount);
    int takeDamage(int damage);
    default int computeAttackValue() {
        return getAttack();
    }
}