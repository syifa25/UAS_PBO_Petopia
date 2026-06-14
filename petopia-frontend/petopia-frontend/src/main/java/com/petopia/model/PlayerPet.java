package com.petopia.model;

public class PlayerPet extends Pet implements Combatant {
    public PlayerPet(String name, String imagePath, int level, int maxHp, int attack, int defense) {
        super(name, imagePath, level, maxHp, attack, defense);
    }
}