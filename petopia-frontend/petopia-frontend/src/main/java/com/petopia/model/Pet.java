package com.petopia.model;

public class Pet {
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

    public String getName() { return name; }
    public String getImagePath() { return imagePath; }
    public int getLevel() { return level; }
    public int getMaxHp() { return maxHp; }
    public int getHp() { return hp; }
    public int getAttack() { return attack; }
    public int getDefense() { return defense; }

    public boolean isFainted() { return hp <= 0; }

    public void heal(int amount) {
        if (isFainted()) return;
        hp += amount;
        if (hp > maxHp) hp = maxHp;
    }

    public int takeDamage(int damage) {
        if (isFainted()) return 0;
        hp -= damage;
        if (hp < 0) hp = 0;
        return Math.max(damage, 0);
    }

    @Override
    public String toString() {
        return name + " (LV." + level + ")";
    }
}