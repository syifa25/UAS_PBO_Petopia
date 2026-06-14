package com.petopia.model;

public class Pet implements Combatant {
    private final String name;
    private final String imagePath;
    private final int level;
    private final int maxHp;
    private int hp;
    private final int attack;
    private final int defense;

    public Pet(String name, String imagePath, int level, int maxHp, int attack, int defense) {
        this.name = name;
        this.imagePath = imagePath;
        this.level = level;
        this.maxHp = maxHp;
        this.hp = maxHp;
        this.attack = attack;
        this.defense = defense;
    }

    @Override
    public String getName() { return name; }

    @Override
    public String getImagePath() { return imagePath; }

    @Override
    public int getLevel() { return level; }

    @Override
    public int getMaxHp() { return maxHp; }

    @Override
    public int getHp() { return hp; }

    @Override
    public int getAttack() { return attack; }

    @Override
    public int getDefense() { return defense; }

    @Override
    public boolean isFainted() { return hp <= 0; }

    @Override
    public void heal(int amount) {
        if (isFainted()) return;
        hp += amount;
        if (hp > maxHp) hp = maxHp;
    }

    @Override
    public int takeDamage(int damage) {
        if (isFainted()) return 0;
        int actualDamage = Math.min(damage, hp); // can't deal more than remaining HP
        hp -= actualDamage;
        return actualDamage;
    }

    @Override
    public String toString() {
        return name + " (LV." + level + ")";
    }
}