// Battle System - Enhanced Version
let playerPet = null;
let enemyPet = null;
let playerTurn = true;
let battleEnded = false;
let potionCount = 3;
let isAnimating = false;
let playerDefenseBuff = 0; // Buff turns remaining
let turnCount = 0;
let totalDamageDealt = 0;

// Start Battle
function startBattle() {
    // Create pets
    playerPet = createPet(selectedFighterIndex);
    enemyPet = createEnemy(currentGameLevel); // Use selected level
    
    // Reset battle state
    playerTurn = true;
    battleEnded = false;
    potionCount = 3;
    isAnimating = false;
    
    // Navigate to battle page
    navigateTo('battle');
    
    // Initialize battle UI
    setTimeout(() => {
        initializeBattle();
    }, 100);
}

function initializeBattle() {
    // Set pet images
    document.getElementById('player-sprite').src = playerPet.image;
    document.getElementById('enemy-sprite').src = enemyPet.image;
    document.getElementById('player-name').textContent = playerPet.name;
    document.getElementById('enemy-name').textContent = enemyPet.name;
    
    // Update HP
    updateHP();
    
    // Update Energy bars
    updateEnergy();
    
    // Update potion button
    updatePotionButton();
    
    // Update special move button
    updateSpecialButton();
    
    // Reset counters
    turnCount = 0;
    totalDamageDealt = 0;
    playerDefenseBuff = 0;
    
    // Reset enemy enrage state
    if (enemyPet) enemyPet.isEnraged = false;
    
    // Remove enraged-aura from enemy sprite if leftover from prev battle
    const enemySpriteEl = document.getElementById('enemy-sprite');
    if (enemySpriteEl) enemySpriteEl.classList.remove('enraged-aura');
    
    // Start intro animation
    startIntroAnimation();
}

function startIntroAnimation() {
    const actionButtons = document.getElementById('action-buttons');
    actionButtons.style.opacity = '0.5';
    actionButtons.style.pointerEvents = 'none';
    
    addLog('⚔ BATTLE START! ' + playerPet.name + ' vs ' + enemyPet.name + '!');
    
    setTimeout(() => {
        determineTurnOrder();
        actionButtons.style.opacity = '1';
        actionButtons.style.pointerEvents = 'auto';
    }, 2000);
}

function determineTurnOrder() {
    if (playerPet.speed >= enemyPet.speed) {
        playerTurn = true;
        addLog('💨 ' + playerPet.name + ' moves first!');
        enableActionButtons();
    } else {
        playerTurn = false;
        addLog('💨 ' + enemyPet.name + ' moves first!');
        setTimeout(() => {
            enemyTurn();
        }, 1500);
    }
}

// Perform Action - Enhanced
function performAction(action) {
    if (battleEnded || !playerTurn || isAnimating) return;
    
    disableActionButtons();
    turnCount++;
    
    switch(action) {
        case 'attack':
            playerAttack();
            break;
        case 'special':
            playerSpecialMove();
            break;
        case 'potion':
            usePotion();
            break;
        case 'flee':
            flee();
            break;
    }
}

function playerAttack() {
    isAnimating = true;
    
    // Check for critical
    const isCritical = playerPet.checkCritical();
    const baseDamage = playerPet.calculateDamage();
    const result = enemyPet.takeDamage(baseDamage, isCritical);
    
    // Gain energy
    playerPet.gainEnergy(25);
    
    // Increment combo
    playerPet.incrementCombo();
    
    // Total damage tracking
    totalDamageDealt += result.damage;
    
    // Log with critical indicator
    const critText = isCritical ? ' 💥 CRITICAL HIT!' : '';
    const comboText = playerPet.comboCount > 1 ? ` (COMBO ×${playerPet.comboCount})` : '';
    addLog('⚔️ ' + playerPet.name + ' attacks!' + critText + ' Deals ' + result.damage + ' damage!' + comboText);
    
    // Attack animation with critical effect
    animateAttack('player-sprite', 'enemy-sprite', isCritical, () => {
        // Show damage number
        showDamageNumber('enemy-pet', result.damage, isCritical);
        
        updateHP();
        updateEnergy();
        updateComboDisplay();
        
        if (!enemyPet.isAlive()) {
            battleEnded = true;
            addLog('🎉 VICTORY! ' + playerPet.name + ' wins!');
            setTimeout(() => {
                showVictoryModal();
            }, 1500);
        } else {
            playerTurn = false;
            setTimeout(() => {
                enemyTurn();
            }, 1500);
        }
        
        isAnimating = false;
    });
}

function playerSpecialMove() {
    if (!playerPet.canUseSpecial()) return;
    
    isAnimating = true;
    playerPet.useSpecial();
    
    const special = playerPet.specialMove;
    addLog('✨ ' + playerPet.name + ' uses ' + special.name + '!');
    
    // Execute special based on effect
    switch(special.effect) {
        case 'multi-hit':
            executeMultiHit();
            break;
        case 'defense-buff':
            executeDefenseBuff();
            break;
        case 'crit-guarantee':
            executeCritGuarantee();
            break;
        case 'damage-heal':
            executeDamageHeal();
            break;
    }
}

function executeMultiHit() {
    // Fury Swipe - 3 quick hits
    let hitCount = 0;
    const hit = () => {
        hitCount++;
        const damage = Math.floor(playerPet.calculateDamage() * 0.6);
        const result = enemyPet.takeDamage(damage, false);
        totalDamageDealt += result.damage;
        
        animateAttack('player-sprite', 'enemy-sprite', false, () => {
            showDamageNumber('enemy-pet', result.damage, false);
            updateHP();
            
            if (hitCount < 3 && enemyPet.isAlive()) {
                setTimeout(hit, 300);
            } else {
                finishSpecialMove();
            }
        });
    };
    
    setTimeout(hit, 300);
}

function executeDefenseBuff() {
    // Iron Wall - Defense buff
    playerDefenseBuff = 2; // 2 turns
    const originalDef = playerPet.defense;
    playerPet.defense *= 2;
    
    addLog('🛡️ ' + playerPet.name + ' defense doubled!');
    
    // Visual effect
    const playerSprite = document.getElementById('player-sprite');
    playerSprite.style.filter = 'drop-shadow(0 0 10px #4169E1)';
    
    setTimeout(() => {
        finishSpecialMove();
    }, 1000);
}

function executeCritGuarantee() {
    // Speed Burst - Guaranteed crit
    const damage = playerPet.calculateDamage();
    const result = enemyPet.takeDamage(damage, true); // Force critical
    totalDamageDealt += result.damage;
    
    addLog('⚡ Guaranteed critical strike!');
    
    animateAttack('player-sprite', 'enemy-sprite', true, () => {
        showDamageNumber('enemy-pet', result.damage, true);
        updateHP();
        finishSpecialMove();
    });
}

function executeDamageHeal() {
    // Wind Strike - Damage + Heal
    const damage = Math.floor(playerPet.calculateDamage() * 1.5);
    const result = enemyPet.takeDamage(damage, false);
    totalDamageDealt += result.damage;
    
    animateAttack('player-sprite', 'enemy-sprite', false, () => {
        showDamageNumber('enemy-pet', result.damage, false);
        
        // Heal
        const healAmount = playerPet.heal(30);
        showDamageNumber('player-pet', healAmount, false, true);
        addLog('💚 ' + playerPet.name + ' healed ' + healAmount + ' HP!');
        
        updateHP();
        finishSpecialMove();
    });
}

function finishSpecialMove() {
    updateEnergy();
    updateSpecialButton();
    isAnimating = false;
    
    if (!enemyPet.isAlive()) {
        battleEnded = true;
        addLog('🎉 VICTORY! ' + playerPet.name + ' wins!');
        setTimeout(() => {
            showVictoryModal();
        }, 1500);
    } else {
        playerTurn = false;
        setTimeout(() => {
            enemyTurn();
        }, 1500);
    }
}

function enemyTurn() {
    if (battleEnded) return;
    
    isAnimating = true;
    
    // Boss Enrage Mechanic
    if (enemyPet.name === "ALPHA WOLF" && enemyPet.getHpPercentage() < 30 && !enemyPet.isEnraged) {
        enemyPet.isEnraged = true;
        enemyPet.attack = Math.floor(enemyPet.attack * 1.5);
        enemyPet.speed = Math.floor(enemyPet.speed * 1.5);
        addLog('⚠️ ALPHA WOLF is ENRAGED! Stats increased!');
        
        const enemySprite = document.getElementById('enemy-sprite');
        enemySprite.classList.add('enraged-aura');
        const container = document.querySelector('.battle-arena-container');
        if(container) container.classList.add('shake');
        
        setTimeout(() => {
            if(container) container.classList.remove('shake');
            executeEnemyAction();
        }, 1500);
    } else {
        executeEnemyAction();
    }
}

function executeEnemyAction() {
    // Enemy gains energy too
    enemyPet.gainEnergy(25);
    
    // Apply defense buff if active
    if (playerDefenseBuff > 0) {
        playerDefenseBuff--;
        if (playerDefenseBuff === 0) {
            // Reset defense
            playerPet.defense = Math.floor(playerPet.defense / 2);
            const playerSprite = document.getElementById('player-sprite');
            playerSprite.style.filter = '';
            addLog('🛡️ Defense buff expired!');
        }
    }
    
    if (enemyPet.canUseSpecial() && enemyPet.specialMove) {
        enemyPet.useSpecial();
        const special = enemyPet.specialMove;
        addLog('✨ ' + enemyPet.name + ' uses ' + special.name + '!');
        
        if (special.effect === 'defense-buff') {
            enemyPet.defense = Math.floor(enemyPet.defense * 1.5);
            addLog('🛡️ ' + enemyPet.name + ' defense increased!');
            const enemySprite = document.getElementById('enemy-sprite');
            enemySprite.style.filter = 'drop-shadow(0 0 10px #DC143C)';
            setTimeout(finishEnemyTurn, 1000);
            return;
        }
        
        let isCritical = false;
        let baseDamage = enemyPet.calculateDamage();
        
        if (special.effect === 'crit-guarantee') {
            isCritical = true;
            addLog('⚡ Guaranteed critical strike from ' + enemyPet.name + '!');
        } else if (special.effect === 'damage-heal') {
            baseDamage = Math.floor(baseDamage * 1.2);
        } else if (special.effect === 'damage') {
            isCritical = enemyPet.checkCritical();
            baseDamage = Math.floor(baseDamage * 1.5);
        } else if (special.effect === 'multi-hit') {
            let hitCount = 0;
            const hit = () => {
                hitCount++;
                const damage = Math.floor(enemyPet.calculateDamage() * 0.7);
                const result = playerPet.takeDamage(damage, false);
                animateAttack('enemy-sprite', 'player-sprite', false, () => {
                    showDamageNumber('player-pet', result.damage, false);
                    updateHP();
                    if (hitCount < 2 && playerPet.isAlive()) {
                        setTimeout(hit, 300);
                    } else {
                        finishEnemyTurn();
                    }
                });
            };
            setTimeout(hit, 300);
            return;
        }

        const result = playerPet.takeDamage(baseDamage, isCritical);
        const critText = isCritical ? ' 💥 CRITICAL HIT!' : '';
        addLog('💥 ' + enemyPet.name + ' attacks!' + critText + ' Deals ' + result.damage + ' damage!');
        
        animateAttack('enemy-sprite', 'player-sprite', isCritical, () => {
            showDamageNumber('player-pet', result.damage, isCritical);
            
            if (special.effect === 'damage-heal') {
                const healAmount = enemyPet.heal(20);
                showDamageNumber('enemy-pet', healAmount, false, true);
                addLog('💚 ' + enemyPet.name + ' healed ' + healAmount + ' HP!');
            }
            
            updateHP();
            finishEnemyTurn();
        });
        
    } else {
        const isCritical = enemyPet.checkCritical();
        const baseDamage = enemyPet.calculateDamage();
        const result = playerPet.takeDamage(baseDamage, isCritical);
        
        const critText = isCritical ? ' 💥 CRITICAL HIT!' : '';
        addLog('💥 ' + enemyPet.name + ' attacks!' + critText + ' Deals ' + result.damage + ' damage!');
        
        // Attack animation
        animateAttack('enemy-sprite', 'player-sprite', isCritical, () => {
            showDamageNumber('player-pet', result.damage, isCritical);
            updateHP();
            finishEnemyTurn();
        });
    }
}

function finishEnemyTurn() {
    updateEnergy();
    
    if (!playerPet.isAlive()) {
        battleEnded = true;
        addLog('💀 DEFEAT! ' + playerPet.name + ' has been defeated...');
        setTimeout(() => {
            showDefeatModal();
        }, 1500);
        isAnimating = false;
    } else {
        triggerEnvironmentEvent(() => {
            playerTurn = true;
            enableActionButtons();
            isAnimating = false;
        });
    }
}

function triggerEnvironmentEvent(callback) {
    if (Math.random() < 0.20) {
        const events = [
            { name: "Meteor Strike", type: "damage", value: 20, log: "☄️ Meteor Strike hits both pets for 20 damage!" },
            { name: "Healing Rain", type: "heal", value: 30, log: "🌧️ Healing Rain restores 30 HP to both pets!" },
            { name: "Energy Surge", type: "energy", value: 30, log: "⚡ Energy Surge! Both pets gain 30 energy!" }
        ];
        
        const evt = events[Math.floor(Math.random() * events.length)];
        addLog(evt.log);
        
        const container = document.querySelector('.battle-arena-container');
        if(container) container.classList.add('environment-flash');
        
        setTimeout(() => {
            if (evt.type === 'damage') {
                playerPet.currentHp = Math.max(0, playerPet.currentHp - evt.value);
                enemyPet.currentHp = Math.max(0, enemyPet.currentHp - evt.value);
                showDamageNumber('player-pet', evt.value, false);
                showDamageNumber('enemy-pet', evt.value, false);
            } else if (evt.type === 'heal') {
                playerPet.heal(evt.value);
                enemyPet.heal(evt.value);
                showDamageNumber('player-pet', evt.value, false, true);
                showDamageNumber('enemy-pet', evt.value, false, true);
            } else if (evt.type === 'energy') {
                playerPet.gainEnergy(evt.value);
                enemyPet.gainEnergy(evt.value);
            }
            
            updateHP();
            updateEnergy();
            if(container) container.classList.remove('environment-flash');
            
            if (!playerPet.isAlive()) {
                battleEnded = true;
                addLog('💀 DEFEAT! ' + playerPet.name + ' has been defeated by the environment...');
                setTimeout(() => showDefeatModal(), 1500);
            } else if (!enemyPet.isAlive()) {
                battleEnded = true;
                addLog('🎉 VICTORY! ' + enemyPet.name + ' was defeated by the environment!');
                setTimeout(() => showVictoryModal(), 1500);
            } else {
                callback();
            }
        }, 800);
    } else {
        callback();
    }
}

function usePotion() {
    if (potionCount <= 0) return;
    
    isAnimating = true;
    potionCount--;
    const healAmount = 50;
    const actualHeal = playerPet.heal(healAmount);
    
    addLog('🧪 Used Potion! Restored ' + actualHeal + ' HP. (' + potionCount + ' left)');
    
    // Heal animation
    animateHeal('player-sprite', () => {
        updateHP();
        updatePotionButton();
        
        playerTurn = false;
        
        setTimeout(() => {
            enemyTurn();
        }, 1500);
        
        isAnimating = false;
    });
}

function flee() {
    addLog('🏃 ' + playerPet.name + ' fled from battle!');
    battleEnded = true;
    
    setTimeout(() => {
        navigateTo('arena');
    }, 1500);
}

// Animations — Close Combat System
function animateAttack(attackerId, defenderId, isCritical, callback) {
    const attacker = document.getElementById(attackerId);
    const defender = document.getElementById(defenderId);
    if (!attacker || !defender) { if (callback) callback(); return; }

    const isPlayerAttacking = attackerId === 'player-sprite';

    // Physics calculations for the jump and clash
    const attackerRushX   = isPlayerAttacking ?  300 : -300;
    const defenderRushX   = isPlayerAttacking ?  -40 :   40;  
    const defenderRecoilX = isPlayerAttacking ? -120 :  120;  
    const jumpY           = -40;

    // Screen shake on critical
    const arenaContainer = document.querySelector('.battle-arena-container');
    if (isCritical && arenaContainer) arenaContainer.classList.add('shake');

    // --- Phase 1 : Attacker leaps toward center ---
    attacker.style.transition = 'transform 0.25s cubic-bezier(0.34, 1.56, 0.64, 1)'; // slight overshoot
    defender.style.transition = 'transform 0.2s ease-out';
    attacker.style.transform  = `translate(${attackerRushX}px, ${jumpY}px) scale(1.15)`;
    defender.style.transform  = `translateX(${defenderRushX}px)`;

    setTimeout(() => {
        // --- Phase 2 : Clash! ---
        spawnClashEffect(isCritical);
        spawnSparks(isCritical);

        // Flash defender
        const wasEnraged = defender.classList.contains('enraged-aura');
        defender.style.filter = isCritical
            ? 'brightness(3) saturate(3) hue-rotate(10deg)'
            : 'brightness(2) sepia(0.5)';
        setTimeout(() => { defender.style.filter = ''; }, 220);

        // Defender recoils away violently
        defender.style.transition = 'transform 0.15s ease-out';
        defender.style.transform  = `translate(${defenderRecoilX}px, -20px) rotate(${isPlayerAttacking ? '15deg' : '-15deg'})`;

        // Attacker stays briefly then returns
        setTimeout(() => {
            // --- Phase 3 : Both return to base ---
            attacker.style.transition = 'transform 0.35s cubic-bezier(0.25, 0.8, 0.25, 1)';
            attacker.style.transform  = 'translate(0, 0) scale(1)';
            defender.style.transition = 'transform 0.45s cubic-bezier(0.34, 1.56, 0.64, 1)';
            defender.style.transform  = 'translate(0, 0) rotate(0deg)';

            setTimeout(() => {
                if (arenaContainer) arenaContainer.classList.remove('shake');
                attacker.style.transition = '';
                defender.style.transition = '';
                if (callback) callback();
            }, 460);
        }, 270);
    }, 250);
}

function spawnClashEffect(isCritical) {
    const arena = document.querySelector('.battle-arena-container');
    if (!arena) return;

    const clash = document.createElement('div');
    clash.className = isCritical ? 'clash-burst clash-critical' : 'clash-burst';
    clash.textContent = isCritical ? '💥' : '⚔️';
    arena.appendChild(clash);

    setTimeout(() => clash.remove(), 700);
}

function spawnSparks(isCritical) {
    const arena = document.querySelector('.battle-arena-container');
    if (!arena) return;

    const count = isCritical ? 16 : 8;
    const colors = ['#FFD700', '#FF6600', '#FFFFFF', '#FF4444', '#FFA500'];

    for (let i = 0; i < count; i++) {
        const spark = document.createElement('div');
        spark.className = 'spark-particle';

        const angle = (360 / count) * i + Math.random() * 40;
        const dist  = 60 + Math.random() * (isCritical ? 120 : 70);
        const dx    = Math.cos(angle * Math.PI / 180) * dist;
        const dy    = Math.sin(angle * Math.PI / 180) * dist;
        const size  = isCritical ? 5 + Math.random() * 7 : 4 + Math.random() * 5;

        spark.style.cssText = `
            left: 50%;
            top: 140px;
            width: ${size}px;
            height: ${size}px;
            background: ${colors[Math.floor(Math.random() * colors.length)]};
            --dx: ${dx}px;
            --dy: ${dy}px;
        `;

        arena.appendChild(spark);
        setTimeout(() => spark.remove(), 550);
    }
}


function animateHeal(spriteId, callback) {
    const sprite = document.getElementById(spriteId);
    sprite.classList.add('heal');
    
    setTimeout(() => {
        sprite.classList.remove('heal');
        if (callback) callback();
    }, 800);
}

// UI Updates
function updateHP() {
    // Player HP
    const playerHpBar = document.getElementById('player-hp-bar');
    const playerHpText = document.getElementById('player-hp-text');
    const playerPercentage = playerPet.getHpPercentage();
    
    playerHpBar.style.width = playerPercentage + '%';
    playerHpBar.className = 'hp-bar ' + playerPet.getHpColorClass();
    playerHpText.textContent = playerPet.currentHp + '/' + playerPet.maxHp;
    
    // Enemy HP
    const enemyHpBar = document.getElementById('enemy-hp-bar');
    const enemyHpText = document.getElementById('enemy-hp-text');
    const enemyPercentage = enemyPet.getHpPercentage();
    
    enemyHpBar.style.width = enemyPercentage + '%';
    enemyHpBar.className = 'hp-bar ' + enemyPet.getHpColorClass();
    enemyHpText.textContent = enemyPet.currentHp + '/' + enemyPet.maxHp;
}

function updatePotionButton() {
    const potionBtn = document.getElementById('potion-btn');
    const potionLabel = document.getElementById('potion-label');
    
    potionLabel.textContent = 'POTION (' + potionCount + ')';
    
    if (potionCount <= 0) {
        potionBtn.disabled = true;
    }
}

function addLog(message) {
    const logText = document.getElementById('battle-log-text');
    logText.textContent = '▸ ' + message;
}

function disableActionButtons() {
    const buttons = document.querySelectorAll('.action-btn');
    buttons.forEach(btn => btn.disabled = true);
}

function enableActionButtons() {
    const buttons = document.querySelectorAll('.action-btn');
    buttons.forEach(btn => {
        if (btn.id === 'potion-btn' && potionCount <= 0) {
            btn.disabled = true;
        } else if (btn.id === 'special-btn' && !playerPet.canUseSpecial()) {
            btn.disabled = true;
        } else {
            btn.disabled = false;
        }
    });
}

// New UI Update Functions
function updateEnergy() {
    // Player energy
    const playerEnergyBar = document.getElementById('player-energy-bar');
    if (playerEnergyBar) {
        const percentage = playerPet.getEnergyPercentage();
        playerEnergyBar.style.width = percentage + '%';
    }
    
    // Update special button
    updateSpecialButton();
}

function updateSpecialButton() {
    const specialBtn = document.getElementById('special-btn');
    const specialLabel = document.getElementById('special-label');
    
    if (playerPet && playerPet.canUseSpecial()) {
        specialBtn.disabled = false;
        specialBtn.classList.add('special-ready');
        if (specialLabel && playerPet.specialMove) {
            specialLabel.textContent = playerPet.specialMove.name.toUpperCase();
        }
    } else {
        specialBtn.disabled = true;
        specialBtn.classList.remove('special-ready');
        if (specialLabel) {
            specialLabel.textContent = 'SPECIAL (50⚡)';
        }
    }
}

function updateComboDisplay() {
    const comboDisplay = document.getElementById('player-combo');
    if (comboDisplay && playerPet.comboCount > 1) {
        comboDisplay.textContent = `COMBO ×${playerPet.comboCount}`;
        comboDisplay.style.display = 'block';
    } else if (comboDisplay) {
        comboDisplay.style.display = 'none';
    }
}

// Floating Damage Numbers
function showDamageNumber(targetId, damage, isCritical, isHeal = false) {
    const target = document.getElementById(targetId);
    if (!target) return;
    
    const damageNum = document.createElement('div');
    damageNum.className = 'damage-number';
    
    if (isCritical) {
        damageNum.classList.add('critical');
        damageNum.textContent = damage + '!';
    } else if (isHeal) {
        damageNum.classList.add('heal');
        damageNum.textContent = '+' + damage;
    } else {
        damageNum.textContent = damage;
    }
    
    // Position relative to target
    const rect = target.getBoundingClientRect();
    const arenaRect = document.querySelector('.battle-arena-container').getBoundingClientRect();
    
    damageNum.style.left = (rect.left - arenaRect.left + rect.width / 2) + 'px';
    damageNum.style.top = (rect.top - arenaRect.top + rect.height / 2) + 'px';
    
    document.querySelector('.battle-arena-container').appendChild(damageNum);
    
    // Remove after animation
    setTimeout(() => {
        damageNum.remove();
    }, 1000);
}


// Victory Modal - Enhanced
function showVictoryModal() {
    const modal = document.getElementById('victory-modal');
    const completedLevel = document.getElementById('completed-level');
    const finalPlayerHp = document.getElementById('final-player-hp');
    const victoryMessage = document.getElementById('victory-message');
    
    // Calculate rewards
    const baseGold = currentGameLevel * 250;
    const baseExp = currentGameLevel * 100;
    
    // Bonuses
    let goldBonus = 0;
    let expBonus = 0;
    let bonusText = [];
    
    // Flawless victory (no damage)
    if (playerPet.currentHp === playerPet.maxHp) {
        goldBonus += Math.floor(baseGold * 0.5);
        expBonus += Math.floor(baseExp * 0.5);
        bonusText.push('⭐ Flawless Victory! +50%');
    }
    
    // Quick win
    if (turnCount <= 5) {
        goldBonus += Math.floor(baseGold * 0.25);
        expBonus += Math.floor(baseExp * 0.25);
        bonusText.push('⚡ Quick Win! +25%');
    }
    
    // High combo
    if (playerPet.comboCount >= 3) {
        const comboPercent = playerPet.comboCount * 0.1;
        goldBonus += Math.floor(baseGold * comboPercent);
        bonusText.push(`🔥 Max Combo ×${playerPet.comboCount}! +${Math.floor(comboPercent*100)}%`);
    }
    
    const totalGold = baseGold + goldBonus;
    const totalExp = baseExp + expBonus;
    
    completedLevel.textContent = 'Level ' + currentGameLevel + ' - ' + getEnemyDifficulty(currentGameLevel);
    finalPlayerHp.textContent = playerPet.currentHp + '/' + playerPet.maxHp;
    
    let message = 'You defeated ' + enemyPet.name + '!\n\n';
    message += '💰 Gold: ' + totalGold + '\n';
    message += '⭐ EXP: ' + totalExp + '\n';
    message += '📊 Total Damage: ' + totalDamageDealt + '\n';
    message += '🎯 Turns: ' + turnCount;
    
    if (bonusText.length > 0) {
        message += '\n\n' + bonusText.join('\n');
    }
    
    victoryMessage.textContent = message;
    victoryMessage.style.whiteSpace = 'pre-line';
    victoryMessage.style.textAlign = 'left';
    
    modal.classList.add('active');
}

function closeVictoryModal() {
    const modal = document.getElementById('victory-modal');
    modal.classList.remove('active');
    navigateTo('arena');
}

function goToNextLevel() {
    const modal = document.getElementById('victory-modal');
    modal.classList.remove('active');
    
    if (currentGameLevel < 5) {
        currentGameLevel++;
        selectLevel(currentGameLevel);
        navigateTo('arena');
    } else {
        alert('🎉 CONGRATULATIONS! You completed all levels!\n\nYou are a true Petopia Champion! 🏆');
        currentGameLevel = 1;
        selectLevel(1);
        navigateTo('arena');
    }
}

// Defeat Modal
function showDefeatModal() {
    const modal = document.getElementById('defeat-modal');
    const attemptedLevel = document.getElementById('attempted-level');
    const enemyRemainingHp = document.getElementById('enemy-remaining-hp');
    const defeatMessage = document.getElementById('defeat-message');
    
    attemptedLevel.textContent = 'Level ' + currentGameLevel + ' - ' + getEnemyDifficulty(currentGameLevel);
    enemyRemainingHp.textContent = enemyPet.currentHp + '/' + enemyPet.maxHp;
    defeatMessage.textContent = 'Your ' + playerPet.name + ' was defeated by ' + enemyPet.name + '...';
    
    modal.classList.add('active');
}

function closeDefeatModal() {
    const modal = document.getElementById('defeat-modal');
    modal.classList.remove('active');
    navigateTo('arena');
}

function retryBattle() {
    const modal = document.getElementById('defeat-modal');
    modal.classList.remove('active');
    startBattle(); // Retry with same settings
}
