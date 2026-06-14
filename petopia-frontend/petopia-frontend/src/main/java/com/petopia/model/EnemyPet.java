package com.petopia.model;

import java.util.Random;

public class EnemyPet extends Pet implements Combatant {
    private final double critChance;
    private final Random rnd = new Random();

    public EnemyPet(String name, String imagePath, int level, int maxHp, int attack, int defense) {
        this(name, imagePath, level, maxHp, attack, defense, 0.15); // default 15% crit
    }

    public EnemyPet(String name, String imagePath, int level, int maxHp, int attack, int defense, double critChance) {
        super(name, imagePath, level, maxHp, attack, defense);
        this.critChance = critChance;
    }

    //Example of polymorphic behavior: enemy can perform an attack which sometimes crits.
    @Override
    public int computeAttackValue() {
        int atk = getAttack();
        if (rnd.nextDouble() < critChance) {
            return atk * 2; // critical hit
        }
        return atk;
    }
}