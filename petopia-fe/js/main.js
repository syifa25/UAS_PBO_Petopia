// Main initialization
document.addEventListener('DOMContentLoaded', function() {
    console.log('🎮 Petopia Web Game Initialized!');
    console.log('Version: 1.0');
    console.log('Ready to play!');
    
    // Initialize home page
    initializeHomePage();
});

function initializeHomePage() {
    // Set initial selected pet
    selectedHomePet = 0;
    selectedFighterIndex = 0;
    
    // Ensure home page is shown
    navigateTo('home');
}

// Keyboard shortcuts (optional)
document.addEventListener('keydown', function(e) {
    // Ignore shortcuts when typing in inputs/textareas
    if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA') return;

    // Press 'H' to go home
    if (e.key === 'h' || e.key === 'H') {
        if (!document.querySelector('#battle-page').classList.contains('active')) {
            navigateTo('home');
        }
    }
    
    // Press 'A' to go to arena
    if (e.key === 'a' || e.key === 'A') {
        if (!document.querySelector('#battle-page').classList.contains('active')) {
            navigateTo('arena');
        }
    }
    
    // Battle shortcuts (when in battle)
    if (document.querySelector('#battle-page').classList.contains('active') && !battleEnded) {
        if (e.key === '1') performAction('attack');
        if (e.key === '2') performAction('potion');
        if (e.key === '3') performAction('flee');
    }
});

// Prevent accidental page reload/close during battle
window.addEventListener('beforeunload', function(e) {
    if (document.querySelector('#battle-page').classList.contains('active') && !battleEnded) {
        e.preventDefault();
        e.returnValue = 'Battle in progress! Are you sure you want to leave?';
        return e.returnValue;
    }
});

// Debug mode (console commands)
window.petopiaDebug = {
    showStats: function() {
        if (playerPet && enemyPet) {
            console.log('=== BATTLE STATS ===');
            console.log('Player:', playerPet.name);
            console.log('  HP:', playerPet.currentHp + '/' + playerPet.maxHp);
            console.log('  ATK:', playerPet.attack, '| DEF:', playerPet.defense, '| SPD:', playerPet.speed);
            console.log('Enemy:', enemyPet.name);
            console.log('  HP:', enemyPet.currentHp + '/' + enemyPet.maxHp);
            console.log('  ATK:', enemyPet.attack, '| DEF:', enemyPet.defense, '| SPD:', enemyPet.speed);
            console.log('Potions left:', potionCount);
            console.log('Turn:', playerTurn ? 'Player' : 'Enemy');
        } else {
            console.log('No battle in progress');
        }
    },
    
    healPlayer: function(amount = 50) {
        if (playerPet) {
            playerPet.heal(amount);
            updateHP();
            console.log('Player healed for', amount, 'HP');
        }
    },
    
    damageEnemy: function(amount = 50) {
        if (enemyPet) {
            enemyPet.takeDamage(amount);
            updateHP();
            console.log('Enemy took', amount, 'damage');
        }
    },
    
    addPotions: function(count = 3) {
        potionCount += count;
        updatePotionButton();
        console.log('Added', count, 'potions. Total:', potionCount);
    },
    
    autoWin: function() {
        if (enemyPet) {
            enemyPet.currentHp = 0;
            updateHP();
            battleEnded = true;
            addLog('🎉 VICTORY! (Debug Mode)');
            setTimeout(() => navigateTo('arena'), 2000);
        }
    }
};

console.log('💡 Debug commands available:');
console.log('  petopiaDebug.showStats() - Show battle stats');
console.log('  petopiaDebug.healPlayer(amount) - Heal player');
console.log('  petopiaDebug.damageEnemy(amount) - Damage enemy');
console.log('  petopiaDebug.addPotions(count) - Add potions');
console.log('  petopiaDebug.autoWin() - Instant victory');
console.log('');
console.log('⌨️  Keyboard shortcuts:');
console.log('  H - Go to Home');
console.log('  A - Go to Arena');
console.log('  1 - Attack (in battle)');
console.log('  2 - Use Potion (in battle)');
console.log('  3 - Flee (in battle)');

// ==========================================
// MY PETS PAGE FUNCTIONS
// ==========================================

// Set Main Pet
function setMainPet(petIndex, btnElement) {
    // 1. Update global pet index
    if (typeof selectHomePet === 'function') {
        selectHomePet(petIndex);
    }
    
    // 2. Reset all main pet buttons in the grid
    const allMainBtns = document.querySelectorAll('.my-pets-grid .btn-main-pet, .my-pets-grid .btn-set-main');
    allMainBtns.forEach(btn => {
        btn.className = 'action-btn-sm btn-set-main';
        btn.innerHTML = 'SET AS MAIN';
    });
    
    // 3. Set the clicked button as active
    if (btnElement) {
        btnElement.className = 'action-btn-sm btn-main-pet active';
        btnElement.innerHTML = '★ MAIN PET';
    }
    
    // Provide some feedback
    const petName = PET_DATA[petIndex] ? PET_DATA[petIndex].name : 'Pet';
    console.log(`${petName} has been set as the main pet.`);
}

// Upgrade Pet
function upgradePet(petIndex, cost) {
    const pet = PET_DATA[petIndex];
    if (!pet) return;
    
    // Ensure playerCoins is accessible; default to 2500 if not yet initialized
    if (typeof playerCoins === 'undefined') {
        window.playerCoins = 2500;
    }
    
    if (playerCoins >= cost) {
        playerCoins -= cost;
        
        // Upgrade logic (mock)
        pet.level += 1;
        pet.attack += 5;
        pet.defense += 5;
        pet.speed += 5;
        
        // Update UI of the card
        const petCards = document.querySelectorAll('.my-pet-card');
        if (petCards[petIndex]) {
            const card = petCards[petIndex];
            
            // Update Level Badge
            const levelBadge = card.querySelector('.pet-level-badge');
            if (levelBadge) levelBadge.textContent = `LV. ${pet.level}`;
            
            // Update Stats
            const statVals = card.querySelectorAll('.stat-val');
            if (statVals.length >= 4) {
                // Assuming order: HP(0), ATK(1), DEF(2), SPD(3) based on HTML layout
                statVals[1].textContent = pet.attack;
                statVals[2].textContent = pet.defense;
                statVals[3].textContent = pet.speed;
            }
            
            // Add a simple visual effect to the card
            card.style.transform = 'scale(1.05)';
            card.style.boxShadow = '0 0 20px rgba(255, 215, 0, 0.8)';
            card.style.borderColor = '#FFD700';
            setTimeout(() => {
                card.style.transform = '';
                card.style.boxShadow = '';
                card.style.borderColor = '';
            }, 300);
        }
        
        showUpgradeModal(true, pet, cost);
    } else {
        showUpgradeModal(false, pet, cost);
    }
}

function showUpgradeModal(isSuccess, pet, cost) {
    const modal = document.getElementById('upgrade-modal');
    const card = document.getElementById('upgrade-modal-card');
    const header = document.getElementById('upgrade-modal-header');
    const title = document.getElementById('upgrade-modal-title');
    const subtitle = document.getElementById('upgrade-modal-subtitle');
    const message = document.getElementById('upgrade-modal-message');
    const icon = document.getElementById('upgrade-modal-icon');
    const statsContainer = document.getElementById('upgrade-stats-container');
    const statAtk = document.getElementById('upgrade-stat-atk');
    const statDef = document.getElementById('upgrade-stat-def');
    const statSpd = document.getElementById('upgrade-stat-spd');
    
    if (isSuccess) {
        card.className = 'modal-content victory-card';
        if (header) header.style.borderColor = '#FFD700';
        title.className = 'modal-title victory-title';
        title.textContent = 'LEVEL UP!';
        
        icon.textContent = '🌟';
        subtitle.className = 'congrats-text';
        subtitle.style.color = '#FFD700';
        subtitle.textContent = `${pet.name} IS NOW LV. ${pet.level}!`;
        
        message.style.display = 'none';
        statsContainer.style.display = 'flex';
        
        statAtk.textContent = `${pet.attack} (+5)`;
        statDef.textContent = `${pet.defense} (+5)`;
        statSpd.textContent = `${pet.speed} (+5)`;
        
    } else {
        card.className = 'modal-content defeat-card';
        if (header) header.style.borderColor = '#8B0000';
        title.className = 'modal-title defeat-title';
        title.textContent = 'UPGRADE FAILED!';
        
        icon.textContent = '❌';
        subtitle.className = 'defeat-text';
        subtitle.style.color = '#FF8C00';
        subtitle.textContent = 'NOT ENOUGH COINS';
        
        message.style.display = 'block';
        message.textContent = `You need ${(cost - playerCoins).toLocaleString()} more coins to upgrade ${pet.name}.`;
        statsContainer.style.display = 'none';
    }
    
    modal.classList.add('active');
}

function closeUpgradeModal() {
    const modal = document.getElementById('upgrade-modal');
    if (modal) {
        modal.classList.remove('active');
    }
}

window.isLoggedIn = false;
window.currentUsername = '';

// Helper: update all navbar login buttons with username
function updateNavLoginButtons(username) {
    const loginBtns = document.querySelectorAll('.nav-login-btn');
    loginBtns.forEach(btn => {
        if (username) {
            btn.textContent = '👤 ' + username.toUpperCase();
            btn.style.color = '#FFD700';
            btn.style.borderColor = '#FFD700';
            btn.style.textShadow = '0 0 8px #FFD700';
        } else {
            btn.textContent = 'LOGIN';
            btn.style.color = '';
            btn.style.borderColor = '';
            btn.style.textShadow = '';
        }
    });
}

// Create Global Notification Function
window.showGlobalNotification = function(title, message, isError) {
    const modal = document.getElementById('global-modal');
    if (!modal) return;
    
    const card = document.getElementById('global-modal-card');
    const header = document.getElementById('global-modal-header');
    const titleEl = document.getElementById('global-modal-title');
    const icon = document.getElementById('global-modal-icon');
    const msg = document.getElementById('global-modal-message');
    
    titleEl.textContent = title;
    msg.textContent = message;
    
    if (isError) {
        card.className = 'modal-content defeat-card';
        header.style.borderColor = '#FF5050';
        titleEl.style.color = '#FF5050';
        icon.textContent = '❌';
    } else {
        card.className = 'modal-content victory-card';
        header.style.borderColor = '#FFD700';
        titleEl.style.color = '#FFD700';
        icon.textContent = '✅';
    }
    
    modal.classList.add('active');
};

// Toggle Auth Mode (Login vs Register)
function toggleAuthMode(mode) {
    const loginSec = document.getElementById('login-section');
    const regSec = document.getElementById('register-section');
    
    if (mode === 'register') {
        loginSec.style.display = 'none';
        regSec.style.display = 'block';
    } else {
        loginSec.style.display = 'block';
        regSec.style.display = 'none';
    }
}

// User Registration and Login Logic
const registeredUsers = {}; // In-memory mock database

function handleRegisterSubmit(e) {
    e.preventDefault();
    const usernameInput = document.getElementById('reg-username').value.trim();
    const emailInput = document.getElementById('reg-email').value.trim();
    const passwordInput = document.getElementById('reg-password').value.trim();

    if (!usernameInput || !passwordInput) {
        showGlobalNotification("ERROR", "Username and Password are required!", true);
        return;
    }
    
    if (registeredUsers[usernameInput]) {
        showGlobalNotification("ERROR", "Username already exists! Please choose another.", true);
        return;
    }

    registeredUsers[usernameInput] = {
        email: emailInput,
        password: passwordInput
    };
    
    showGlobalNotification("SUCCESS", `Registration successful for [${usernameInput}]! Please log in.`, false);
    
    // Clear form
    document.getElementById('reg-username').value = '';
    document.getElementById('reg-email').value = '';
    document.getElementById('reg-password').value = '';
    
    toggleAuthMode('login');
}

function handleLoginSubmit(e) {
    e.preventDefault();
    const usernameInput = document.getElementById('login-username').value.trim();
    const passwordInput = document.getElementById('login-password').value.trim();
    const hintBox = document.getElementById('login-register-hint');
    
    // Hide hint by default
    if (hintBox) hintBox.style.display = 'none';
    
    if (!usernameInput || !passwordInput) {
        showGlobalNotification("ERROR", "Please enter username and password!", true);
        return;
    }
    
    const user = registeredUsers[usernameInput];
    
    if (!user) {
        // Show inline hint!
        if (hintBox) hintBox.style.display = 'block';
        showGlobalNotification("NOT FOUND", "Player not found! You must register first.", true);
        return;
    }
    
    if (user.password !== passwordInput) {
        showGlobalNotification("ERROR", "Incorrect password!", true);
        return;
    }
    
    window.isLoggedIn = true;
    window.currentUsername = usernameInput;
    
    // Update all navbar LOGIN buttons to show username
    updateNavLoginButtons(usernameInput);
    
    showGlobalNotification("LOGIN SUCCESS", `Welcome back, ${usernameInput}! Let's battle!`, false);
    
    // Clear form
    document.getElementById('login-username').value = '';
    document.getElementById('login-password').value = '';
    
    // Go to profile page
    if (typeof navigateTo === 'function') {
        showProfilePage();
        navigateTo('profile');
    }
}

// ==========================================
// PROFILE PAGE FUNCTIONS
// ==========================================

function handleNavLoginClick() {
    if (window.isLoggedIn) {
        showProfilePage();
        navigateTo('profile');
    } else {
        navigateTo('auth');
    }
}

function showProfilePage() {
    // Update username display
    const usernameEl = document.getElementById('profile-username-display');
    if (usernameEl && window.currentUsername) {
        usernameEl.textContent = window.currentUsername.toUpperCase();
    }
    // Update coins
    const coinsEl = document.getElementById('profile-coins-display');
    if (coinsEl && typeof playerCoins !== 'undefined') {
        coinsEl.textContent = playerCoins.toLocaleString();
    }
    // Battles/wins (if tracked)
    const battlesEl = document.getElementById('profile-battles-display');
    const winsEl    = document.getElementById('profile-wins-display');
    if (battlesEl) battlesEl.textContent = window.totalBattles || 0;
    if (winsEl)    winsEl.textContent    = window.totalWins    || 0;
}

function handleLogout() {
    window.isLoggedIn    = false;
    window.currentUsername = '';
    
    // Reset all navbar login buttons
    const loginBtns = document.querySelectorAll('.nav-login-btn');
    loginBtns.forEach(btn => {
        btn.textContent = 'LOGIN';
        btn.style.color = '';
        btn.style.borderColor = '';
        btn.style.textShadow = '';
        btn.style.boxShadow = '';
        // Restore onclick to go auth not profile
        btn.setAttribute('onclick', "navigateTo('auth')");
    });
    
    showGlobalNotification("LOGGED OUT", "See you next time, Trainer! 👋", false);
    navigateTo('home');
}

// ==========================================
// MARKETPLACE FUNCTIONS
// ==========================================

let playerCoins = 2500;

function buyItem(itemName, price) {
    if (playerCoins >= price) {
        playerCoins -= price;
        
        // Update UI
        const coinsDisplay = document.getElementById('player-coins');
        if (coinsDisplay) {
            coinsDisplay.textContent = playerCoins.toLocaleString();
            
            // Add a visual flash to the coin display
            coinsDisplay.parentElement.style.boxShadow = '0 0 20px #28DD28';
            coinsDisplay.parentElement.style.borderColor = '#28DD28';
            setTimeout(() => {
                coinsDisplay.parentElement.style.boxShadow = '';
                coinsDisplay.parentElement.style.borderColor = '';
            }, 500);
        }
        
        showMarketModal(true, itemName, price);
    } else {
        showMarketModal(false, itemName, price);
    }
}

function showMarketModal(isSuccess, itemName, price) {
    const modal = document.getElementById('market-modal');
    const card = document.getElementById('market-modal-card');
    const title = document.getElementById('market-modal-title');
    const subtitle = document.getElementById('market-modal-subtitle');
    const message = document.getElementById('market-modal-message');
    const icon = document.getElementById('market-modal-icon');
    const statLabel = document.getElementById('market-stat-label');
    const statValue = document.getElementById('market-stat-value');
    
    if (isSuccess) {
        card.className = 'modal-content victory-card';
        title.className = 'modal-title victory-title';
        title.textContent = 'SUCCESS!';
        subtitle.className = 'congrats-text';
        subtitle.style.color = '#FFD700';
        subtitle.textContent = 'ITEM PURCHASED';
        message.textContent = `You have successfully bought ${itemName}.`;
        icon.textContent = '🛍️';
        statLabel.textContent = 'Remaining Coins:';
        statValue.textContent = `${playerCoins.toLocaleString()} 💰`;
        statValue.style.color = '#FFD700';
    } else {
        card.className = 'modal-content defeat-card';
        title.className = 'modal-title defeat-title';
        title.textContent = 'FAILED!';
        subtitle.className = 'defeat-text';
        subtitle.style.color = '#FF8C00';
        subtitle.textContent = 'NOT ENOUGH COINS';
        message.textContent = `You need ${(price - playerCoins).toLocaleString()} more coins to buy ${itemName}.`;
        icon.textContent = '❌';
        statLabel.textContent = 'Your Coins:';
        statValue.textContent = `${playerCoins.toLocaleString()} / ${price.toLocaleString()} 💰`;
        statValue.style.color = '#FF3030';
    }
    
    modal.classList.add('active');
}

function closeMarketModal() {
    const modal = document.getElementById('market-modal');
    if (modal) {
        modal.classList.remove('active');
    }
}

// Switch Marketplace Tabs
function switchMarketTab(tabId, btnElement) {
    // 1. Hide all market grids
    const grids = document.querySelectorAll('.market-grid');
    grids.forEach(grid => {
        grid.style.display = 'none';
    });
    
    // 2. Show the selected grid
    const targetGrid = document.getElementById(tabId);
    if (targetGrid) {
        targetGrid.style.display = 'grid'; // because the class uses grid
    }
    
    // 3. Remove 'active' class from all tabs
    const tabs = document.querySelectorAll('.market-tab');
    tabs.forEach(tab => {
        tab.classList.remove('active');
    });
    
    // 4. Add 'active' class to clicked tab
    if (btnElement) {
        btnElement.classList.add('active');
    }
}
