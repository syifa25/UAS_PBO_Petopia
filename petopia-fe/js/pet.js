// Pet Data Model - Enhanced Version
class Pet {
    constructor(name, level, attack, defense, speed, image, specialMove) {
        this.name = name;
        this.level = level;
        this.maxHp = 80 + (level * 8);
        this.currentHp = this.maxHp;
        this.attack = attack;
        this.defense = defense;
        this.speed = speed;
        this.image = image;
        this.specialMove = specialMove; // Special ability
        
        // New stats
        this.maxEnergy = 100;
        this.currentEnergy = 0; // Start at 0, gain per turn
        this.critChance = 10 + Math.floor(speed / 10); // Higher speed = more crit
        this.comboCount = 0; // Track consecutive hits
    }

    takeDamage(damage, isCritical = false) {
        const baseDamage = isCritical ? damage * 2 : damage;
        const actualDamage = Math.max(1, baseDamage - Math.floor(this.defense / 2));
        this.currentHp = Math.max(0, this.currentHp - actualDamage);
        
        // Reset combo if hit
        this.comboCount = 0;
        
        return { damage: actualDamage, isCritical };
    }

    heal(amount) {
        const actualHeal = Math.min(amount, this.maxHp - this.currentHp);
        this.currentHp = Math.min(this.maxHp, this.currentHp + amount);
        return actualHeal;
    }

    calculateDamage() {
        // Base damage with variation
        const variation = 0.8 + (Math.random() * 0.4);
        let damage = Math.floor(this.attack * variation);
        
        // Combo bonus (each combo = +10% damage)
        if (this.comboCount > 0) {
            const comboBonus = 1 + (this.comboCount * 0.1);
            damage = Math.floor(damage * comboBonus);
        }
        
        return damage;
    }

    checkCritical() {
        return Math.random() * 100 < this.critChance;
    }

    gainEnergy(amount = 25) {
        this.currentEnergy = Math.min(this.maxEnergy, this.currentEnergy + amount);
    }

    canUseSpecial() {
        return this.currentEnergy >= 50;
    }

    useSpecial() {
        if (this.canUseSpecial()) {
            this.currentEnergy -= 50;
            return true;
        }
        return false;
    }

    incrementCombo() {
        this.comboCount++;
    }

    resetCombo() {
        this.comboCount = 0;
    }

    isAlive() {
        return this.currentHp > 0;
    }

    getHpPercentage() {
        return (this.currentHp / this.maxHp) * 100;
    }

    getEnergyPercentage() {
        return (this.currentEnergy / this.maxEnergy) * 100;
    }

    getHpColorClass() {
        const percentage = this.getHpPercentage();
        if (percentage > 60) return 'hp-high';
        if (percentage > 30) return 'hp-medium';
        if (percentage > 15) return 'hp-low';
        return 'hp-critical';
    }
}

// Pet Database - Using Images with Special Moves
const PET_DATA = [
    {
        name: "RISSOLE CAT",
        level: 15,
        attack: 45,
        defense: 30,
        speed: 60,
        image: "images/pet1.png",
        status: "READY",
        specialMove: {
            name: "Fury Swipe",
            description: "3 quick attacks!",
            effect: "multi-hit"
        }
    },
    {
        name: "GRIZZLY BEAR",
        level: 12,
        attack: 50,
        defense: 40,
        speed: 45,
        image: "images/pet2.png",
        status: "RESTING",
        specialMove: {
            name: "Iron Wall",
            description: "Double defense for 2 turns!",
            effect: "defense-buff"
        }
    },
    {
        name: "AQUA SLIME",
        level: 5,
        attack: 35,
        defense: 20,
        speed: 70,
        image: "images/pet3.png",
        status: "IN TRAINING",
        specialMove: {
            name: "Speed Burst",
            description: "Guaranteed critical hit!",
            effect: "crit-guarantee"
        }
    },
    {
        name: "SWIFT BIRD",
        level: 8,
        attack: 38,
        defense: 22,
        speed: 75,
        image: "images/burung.png",
        status: "READY",
        specialMove: {
            name: "Wind Strike",
            description: "High damage + heal!",
            effect: "damage-heal"
        }
    }
];

// Level System - 5 Levels with increasing difficulty
const LEVEL_ENEMIES = [
    {
        level: 1,
        name: "WEAK WOLF",
        baseLevel: 5,
        attack: 25,
        defense: 15,
        speed: 35,
        image: "images/enemy.png",
        difficulty: "EASY",
        specialMove: { name: "Bite", description: "Basic heavy attack", effect: "damage" }
    },
    {
        level: 2,
        name: "YOUNG WOLF",
        baseLevel: 8,
        attack: 32,
        defense: 20,
        speed: 42,
        image: "images/enemy.png",
        difficulty: "NORMAL",
        specialMove: { name: "Quick Strike", description: "Double hit!", effect: "multi-hit" }
    },
    {
        level: 3,
        name: "BRAVE WOLF PUP",
        baseLevel: 12,
        attack: 40,
        defense: 25,
        speed: 50,
        image: "images/enemy.png",
        difficulty: "MEDIUM",
        specialMove: { name: "Wolf Howl", description: "Buffs defense!", effect: "defense-buff" }
    },
    {
        level: 4,
        name: "FIERCE WOLF",
        baseLevel: 15,
        attack: 48,
        defense: 32,
        speed: 58,
        image: "images/enemy.png",
        difficulty: "HARD",
        specialMove: { name: "Vampiric Bite", description: "Damage + Heal!", effect: "damage-heal" }
    },
    {
        level: 5,
        name: "ALPHA WOLF",
        baseLevel: 20,
        attack: 60,
        defense: 40,
        speed: 65,
        image: "images/enemy.png",
        difficulty: "BOSS",
        specialMove: { name: "Alpha Strike", description: "Guaranteed critical!", effect: "crit-guarantee" }
    }
];

// Current game level
let currentGameLevel = 1;

// Create Pet from data
function createPet(index) {
    const data = PET_DATA[index];
    return new Pet(
        data.name,
        data.level,
        data.attack,
        data.defense,
        data.speed,
        data.image,
        data.specialMove
    );
}

// Create Enemy based on current level
function createEnemy(level = 1) {
    // Make sure level is between 1-5
    const safeLevel = Math.max(1, Math.min(5, level));
    const enemyData = LEVEL_ENEMIES[safeLevel - 1];
    
    return new Pet(
        enemyData.name,
        enemyData.baseLevel,
        enemyData.attack,
        enemyData.defense,
        enemyData.speed,
        enemyData.image,
        enemyData.specialMove
    );
}

// Get enemy difficulty name
function getEnemyDifficulty(level = 1) {
    const safeLevel = Math.max(1, Math.min(5, level));
    return LEVEL_ENEMIES[safeLevel - 1].difficulty;
}
