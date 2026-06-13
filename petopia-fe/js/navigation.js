// Page Navigation
function navigateTo(pageName) {
    // Authentication guard
    if (!window.isLoggedIn && (pageName === 'mypets' || pageName === 'marketplace' || pageName === 'arena')) {
        if (typeof showGlobalNotification === 'function') {
            showGlobalNotification("ACCESS DENIED", "You must login to access this area.", true);
        } else {
            alert("❌ You must login first!");
        }
        pageName = 'auth';
    }

    // Hide all pages
    const pages = document.querySelectorAll('.page');
    pages.forEach(page => page.classList.remove('active'));
    
    // Show selected page
    const targetPage = document.getElementById(pageName + '-page');
    if (targetPage) {
        targetPage.classList.add('active');
    }
    
    // Update navbar active state
    updateNavbar(pageName);
}

function updateNavbar(activePage) {
    const allNavButtons = document.querySelectorAll('.nav-btn');
    allNavButtons.forEach(btn => btn.classList.remove('active'));

    // Map page names to button text
    const pageButtonMap = {
        'home':        'HOME',
        'mypets':      'MY PETS',
        'arena':       'ARENA',
        'marketplace': 'MARKETPLACE',
        'auth':        'LOGIN'
    };

    // Highlight the correct active button
    const activeLabel = pageButtonMap[activePage];
    if (activeLabel) {
        allNavButtons.forEach(btn => {
            // Don't mark login btn as active — it shows username instead
            if (btn.classList.contains('nav-login-btn')) return;
            if (btn.textContent.trim() === activeLabel) {
                btn.classList.add('active');
            }
        });
    }

    // Restore username display on all login buttons after navigation
    if (window.isLoggedIn && window.currentUsername) {
        const loginBtns = document.querySelectorAll('.nav-login-btn');
        loginBtns.forEach(btn => {
            btn.textContent = '👤 ' + window.currentUsername.toUpperCase();
            btn.style.color = '#FFD700';
            btn.style.borderColor = '#FFD700';
            btn.style.textShadow = '0 0 8px rgba(255,215,0,0.8)';
            btn.style.boxShadow = '0 0 6px rgba(255,215,0,0.2)';
        });
    }
}

// Home page pet selection
let selectedHomePet = 0;

function selectHomePet(petIndex) {
    selectedHomePet = petIndex;
    
    // Update button states
    const petButtons = document.querySelectorAll('.pet-btn');
    petButtons.forEach((btn, index) => {
        if (index === petIndex) {
            btn.classList.add('selected');
        } else {
            btn.classList.remove('selected');
        }
    });
    
    // Update preview image
    const playerPreview = document.querySelector('#home-player-pet .home-pet-image');
    if (playerPreview) {
        playerPreview.src = PET_DATA[petIndex].image;
    }
}

// Arena page fighter selection
let selectedFighterIndex = 0;

function selectFighter(fighterIndex) {
    selectedFighterIndex = fighterIndex;
    
    // Update card states
    const fighterCards = document.querySelectorAll('.fighter-card');
    fighterCards.forEach((card, index) => {
        if (index === fighterIndex) {
            card.classList.add('selected');
        } else {
            card.classList.remove('selected');
        }
    });
}

// Level selection
function selectLevel(level) {
    currentGameLevel = level;
    
    // Update button states
    const levelButtons = document.querySelectorAll('.level-btn');
    levelButtons.forEach((btn, index) => {
        if (index + 1 === level) {
            btn.classList.add('selected');
        } else {
            btn.classList.remove('selected');
        }
    });
}

// How to Play modal
function showHowToPlay() {
    const modal = document.getElementById('howto-modal');
    if (modal) {
        modal.classList.add('active');
    }
}

function closeHowToPlay() {
    const modal = document.getElementById('howto-modal');
    if (modal) {
        modal.classList.remove('active');
    }
}
