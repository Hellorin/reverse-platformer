// Game Constants
const GAME_CONFIG = {
    RUNNER: {
        WIDTH: 20,
        HEIGHT: 30,
        EYE_OFFSET_X: 12,
        EYE_OFFSET_Y: 8,
        EYE_SIZE: 4,
        MOUTH_OFFSET_X: 8,
        MOUTH_OFFSET_Y: 18,
        MOUTH_WIDTH: 8,
        MOUTH_HEIGHT: 2
    },
    TRAP: {
        SIZE: 30,
        COSTS: {
            SPIKE: 50,
            BOUNCE_PAD: 20,
            SLOW_ZONE: 15
        }
    },
    CANVAS: {
        DEFAULT_WIDTH: 800,
        DEFAULT_HEIGHT: 450
    },
    MESSAGES: {
        DURATION_MS: 1500
    },
    FONTS: {
        GOAL_LABEL: '12px Arial',
        WAITING_TITLE: '24px Arial',
        WAITING_SUBTITLE: '16px Arial',
        GAME_OVER_TITLE: 'bold 48px Arial',
        GAME_OVER_SUBTITLE: '24px Arial',
        GAME_OVER_TEXT: '20px Arial',
        GAME_OVER_SMALL: '18px Arial'
    }
};

const COLORS = {
    background: '#1a1a2e',
    platform: '#4a5568',
    runner: '#ff6b6b',
    runnerDead: '#666',
    goal: '#6bcb77',
    spike: '#ff6b6b',
    bouncePad: '#4ecdc4',
    slowZone: '#a855f7',
    white: '#fff',
    yellow: '#ffd93d',
    gray: '#888',
    blue: '#4d96ff',
    overlayDark: 'rgba(0, 0, 0, 0.7)'
};

const canvas = document.getElementById('gameCanvas');
const ctx = canvas.getContext('2d');
const startBtn = document.getElementById('startBtn');
const nextLevelBtn = document.getElementById('nextLevelBtn');
const levelDisplay = document.getElementById('level');
const pointsDisplay = document.getElementById('points');
const timeDisplay = document.getElementById('time');
const statusDisplay = document.getElementById('status');
const trapButtons = document.querySelectorAll('.trap-btn');

// Set canvas internal resolution
canvas.width = GAME_CONFIG.CANVAS.DEFAULT_WIDTH;
canvas.height = GAME_CONFIG.CANVAS.DEFAULT_HEIGHT;

let socket = null;
let gameState = null;
let selectedTrap = null;
let mousePos = { x: 0, y: 0 };
let isMouseOnCanvas = false;
let reconnectAttempts = 0;
let reconnectTimeout = null;
const MAX_RECONNECT_ATTEMPTS = 5;
const RECONNECT_DELAY_MS = 3000;

// Get canvas coordinates accounting for scaling
function getCanvasCoordinates(event) {
    const rect = canvas.getBoundingClientRect();
    const scaleX = canvas.width / rect.width;
    const scaleY = canvas.height / rect.height;

    return {
        x: (event.clientX - rect.left) * scaleX,
        y: (event.clientY - rect.top) * scaleY
    };
}

// Select trap function
function selectTrap(trapType, btn) {
    document.querySelectorAll('.trap-btn').forEach(b => b.classList.remove('selected'));
    btn.classList.add('selected');
    selectedTrap = trapType;
}

// Connect to WebSocket
function connect() {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const wsUrl = `${protocol}//${window.location.host}/game`;

    try {
        socket = new WebSocket(wsUrl);

        socket.onopen = () => {
            reconnectAttempts = 0;
            statusDisplay.textContent = 'Connected - Click START';
            statusDisplay.style.color = COLORS.blue;
        };

        socket.onmessage = (event) => {
            try {
                gameState = JSON.parse(event.data);
                updateUI();
            } catch (error) {
                showMessage('Error parsing game data');
            }
        };

        socket.onclose = (event) => {
            socket = null;

            if (event.wasClean) {
                statusDisplay.textContent = 'Disconnected';
                statusDisplay.style.color = COLORS.gray;
            } else {
                attemptReconnect();
            }
        };

        socket.onerror = () => {
            statusDisplay.textContent = 'Connection error';
            statusDisplay.style.color = COLORS.runner;
        };
    } catch (error) {
        showMessage('Failed to connect to server');
        attemptReconnect();
    }
}

function attemptReconnect() {
    if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
        statusDisplay.textContent = 'Connection lost - Refresh page';
        statusDisplay.style.color = COLORS.runner;
        return;
    }

    reconnectAttempts++;
    statusDisplay.textContent = `Reconnecting... (${reconnectAttempts}/${MAX_RECONNECT_ATTEMPTS})`;
    statusDisplay.style.color = COLORS.yellow;

    clearTimeout(reconnectTimeout);
    reconnectTimeout = setTimeout(() => {
        connect();
    }, RECONNECT_DELAY_MS);
}

// Send message to server
function send(message) {
    if (!socket || socket.readyState !== WebSocket.OPEN) {
        showMessage('Not connected to server');
        return false;
    }

    try {
        socket.send(JSON.stringify(message));
        return true;
    } catch (error) {
        showMessage('Failed to send message');
        return false;
    }
}

// Start/Restart game
function startGame() {
    send({ type: 'START' });
    document.getElementById('startBtn').textContent = 'RESTART';
    nextLevelBtn.style.display = 'none';
}

// Next level
function nextLevel() {
    send({ type: 'NEXT_LEVEL' });
    nextLevelBtn.style.display = 'none';
}

// Place trap
function placeTrap(x, y) {
    if (!selectedTrap) {
        showMessage('Select a trap first!');
        return;
    }
    if (!gameState || gameState.status !== 'RUNNING') {
        showMessage('Press START to begin!');
        return;
    }

    if (gameState.playerPoints < GAME_CONFIG.TRAP.COSTS[selectedTrap]) {
        showMessage('Not enough points!');
        return;
    }

    send({
        type: 'PLACE_TRAP',
        trapType: selectedTrap,
        x: x,
        y: y
    });
}

// Show temporary message
let messageTimeout = null;
function showMessage(text) {
    statusDisplay.textContent = text;
    statusDisplay.style.color = COLORS.runner;
    clearTimeout(messageTimeout);
    messageTimeout = setTimeout(() => {
        statusDisplay.style.color = COLORS.blue;
        updateUI();
    }, GAME_CONFIG.MESSAGES.DURATION_MS);
}

// Update UI elements
function updateUI() {
    if (!gameState) return;

    levelDisplay.textContent = `Level: ${gameState.level}`;
    pointsDisplay.textContent = `Points: ${gameState.playerPoints}`;
    timeDisplay.textContent = `Time: ${gameState.elapsedTime.toFixed(1)}s`;

    const MAX_LEVEL = 5;
    switch (gameState.status) {
        case 'WAITING':
            statusDisplay.textContent = 'Click START to play';
            nextLevelBtn.style.display = 'none';
            break;
        case 'RUNNING':
            const speedPercent = Math.round(gameState.speedMultiplier * 100);
            statusDisplay.textContent = `Stop the runner! (Speed: ${speedPercent}%)`;
            nextLevelBtn.style.display = 'none';
            break;
        case 'PLAYER_WINS':
            if (gameState.level < MAX_LEVEL) {
                statusDisplay.textContent = `Level ${gameState.level} complete!`;
                nextLevelBtn.style.display = 'inline-block';
            } else {
                statusDisplay.textContent = 'YOU BEAT ALL LEVELS!';
                nextLevelBtn.style.display = 'none';
            }
            startBtn.textContent = 'RESTART';
            break;
        case 'RUNNER_WINS':
            statusDisplay.textContent = 'Runner escaped! Try again.';
            startBtn.textContent = 'RETRY';
            nextLevelBtn.style.display = 'none';
            break;
    }

    // Update trap button states (visual feedback, not disabled)
    trapButtons.forEach(btn => {
        const cost = parseInt(btn.dataset.cost);
        const canAfford = gameState.playerPoints >= cost && gameState.status === 'RUNNING';
        btn.classList.toggle('unaffordable', !canAfford);
    });
}

// Render game
function render() {
    // Clear canvas
    ctx.fillStyle = COLORS.background;
    ctx.fillRect(0, 0, canvas.width, canvas.height);

    if (!gameState) {
        drawWaitingScreen();
        return;
    }

    // Draw platforms
    gameState.platforms.forEach(platform => {
        if (platform.active) {
            ctx.fillStyle = COLORS.platform;
            ctx.fillRect(platform.x, platform.y, platform.width, platform.height);
        }
    });

    // Draw goal
    ctx.fillStyle = COLORS.goal;
    ctx.fillRect(gameState.goal.x, gameState.goal.y, gameState.goal.width, gameState.goal.height);
    ctx.fillStyle = COLORS.white;
    ctx.font = GAME_CONFIG.FONTS.GOAL_LABEL;
    ctx.textAlign = 'center';
    ctx.fillText('GOAL', gameState.goal.x + gameState.goal.width / 2, gameState.goal.y + gameState.goal.height / 2 + 4);

    // Draw traps
    gameState.traps.forEach(trap => {
        drawTrap(trap);
    });

    // Draw runner
    const runner = gameState.runner;
    ctx.fillStyle = runner.alive ? COLORS.runner : COLORS.runnerDead;
    ctx.fillRect(runner.x, runner.y, GAME_CONFIG.RUNNER.WIDTH, GAME_CONFIG.RUNNER.HEIGHT);

    // Draw runner face
    if (runner.alive) {
        ctx.fillStyle = COLORS.white;
        ctx.fillRect(
            runner.x + GAME_CONFIG.RUNNER.EYE_OFFSET_X,
            runner.y + GAME_CONFIG.RUNNER.EYE_OFFSET_Y,
            GAME_CONFIG.RUNNER.EYE_SIZE,
            GAME_CONFIG.RUNNER.EYE_SIZE
        ); // Eye
        ctx.fillRect(
            runner.x + GAME_CONFIG.RUNNER.MOUTH_OFFSET_X,
            runner.y + GAME_CONFIG.RUNNER.MOUTH_OFFSET_Y,
            GAME_CONFIG.RUNNER.MOUTH_WIDTH,
            GAME_CONFIG.RUNNER.MOUTH_HEIGHT
        ); // Mouth
    }

    // Draw game over overlay
    if (gameState.status === 'PLAYER_WINS' || gameState.status === 'RUNNER_WINS') {
        drawGameOverOverlay();
    }

    // Draw trap preview
    if (isMouseOnCanvas && selectedTrap && gameState && gameState.status === 'RUNNING') {
        const canAfford = gameState.playerPoints >= GAME_CONFIG.TRAP.COSTS[selectedTrap];
        drawTrapPreview(mousePos.x, mousePos.y, selectedTrap, canAfford);
    }
}

function drawTrap(trap) {
    const size = GAME_CONFIG.TRAP.SIZE;

    switch (trap.type) {
        case 'SPIKE':
            ctx.fillStyle = COLORS.spike;
            // Draw triangle spikes
            ctx.beginPath();
            ctx.moveTo(trap.x, trap.y + size);
            ctx.lineTo(trap.x + size / 2, trap.y);
            ctx.lineTo(trap.x + size, trap.y + size);
            ctx.closePath();
            ctx.fill();
            break;

        case 'BOUNCE_PAD':
            ctx.fillStyle = COLORS.bouncePad;
            ctx.fillRect(trap.x, trap.y + size - 10, size, 10);
            // Draw spring lines
            ctx.strokeStyle = COLORS.white;
            ctx.lineWidth = 2;
            ctx.beginPath();
            ctx.moveTo(trap.x + 5, trap.y + size - 5);
            ctx.lineTo(trap.x + 15, trap.y + size - 15);
            ctx.lineTo(trap.x + 25, trap.y + size - 5);
            ctx.stroke();
            break;

        case 'SLOW_ZONE':
            ctx.fillStyle = COLORS.slowZone + '80'; // Semi-transparent
            ctx.fillRect(trap.x, trap.y, size, size);
            ctx.strokeStyle = COLORS.slowZone;
            ctx.lineWidth = 2;
            ctx.strokeRect(trap.x, trap.y, size, size);
            break;
    }
}

function drawTrapPreview(x, y, trapType, canAfford) {
    const size = GAME_CONFIG.TRAP.SIZE;
    const centerX = x - size / 2;
    const centerY = y - size / 2;

    ctx.save();
    ctx.globalAlpha = canAfford ? 0.5 : 0.3;

    const previewTrap = { x: centerX, y: centerY, type: trapType };
    drawTrap(previewTrap);

    // Draw red X if can't afford
    if (!canAfford) {
        ctx.globalAlpha = 0.8;
        ctx.strokeStyle = COLORS.runner;
        ctx.lineWidth = 3;
        ctx.beginPath();
        ctx.moveTo(centerX + 5, centerY + 5);
        ctx.lineTo(centerX + size - 5, centerY + size - 5);
        ctx.moveTo(centerX + size - 5, centerY + 5);
        ctx.lineTo(centerX + 5, centerY + size - 5);
        ctx.stroke();
    }

    ctx.restore();
}

function drawWaitingScreen() {
    ctx.fillStyle = COLORS.white;
    ctx.font = GAME_CONFIG.FONTS.WAITING_TITLE;
    ctx.textAlign = 'center';
    ctx.fillText('SABOTEUR', canvas.width / 2, canvas.height / 2 - 20);
    ctx.font = GAME_CONFIG.FONTS.WAITING_SUBTITLE;
    ctx.fillStyle = COLORS.gray;
    ctx.fillText('Click START to begin', canvas.width / 2, canvas.height / 2 + 20);
}

function drawGameOverOverlay() {
    const MAX_LEVEL = 5;
    ctx.fillStyle = COLORS.overlayDark;
    ctx.fillRect(0, 0, canvas.width, canvas.height);

    ctx.textAlign = 'center';

    if (gameState.status === 'PLAYER_WINS') {
        ctx.fillStyle = COLORS.goal;
        ctx.font = GAME_CONFIG.FONTS.GAME_OVER_TITLE;

        if (gameState.level >= MAX_LEVEL) {
            ctx.fillText('VICTORY!', canvas.width / 2, canvas.height / 2 - 40);
            ctx.font = GAME_CONFIG.FONTS.GAME_OVER_SUBTITLE;
            ctx.fillStyle = COLORS.yellow;
            ctx.fillText(`You beat all ${MAX_LEVEL} levels!`, canvas.width / 2, canvas.height / 2 + 10);
            ctx.font = GAME_CONFIG.FONTS.GAME_OVER_SMALL;
            ctx.fillStyle = COLORS.white;
            ctx.fillText(`Final Score: ${gameState.playerPoints}`, canvas.width / 2, canvas.height / 2 + 50);
        } else {
            ctx.fillText(`LEVEL ${gameState.level} CLEAR!`, canvas.width / 2, canvas.height / 2 - 20);
            ctx.font = GAME_CONFIG.FONTS.GAME_OVER_TEXT;
            ctx.fillStyle = COLORS.white;
            ctx.fillText('Click NEXT LEVEL to continue', canvas.width / 2, canvas.height / 2 + 30);
        }
    } else {
        ctx.fillStyle = COLORS.runner;
        ctx.font = GAME_CONFIG.FONTS.GAME_OVER_TITLE;
        ctx.fillText('RUNNER ESCAPED!', canvas.width / 2, canvas.height / 2 - 20);
        ctx.font = GAME_CONFIG.FONTS.GAME_OVER_TEXT;
        ctx.fillStyle = COLORS.white;
        ctx.fillText(`Level ${gameState.level} - Click RETRY`, canvas.width / 2, canvas.height / 2 + 30);
    }
}

// Canvas mouse listeners
canvas.addEventListener('click', (event) => {
    const coords = getCanvasCoordinates(event);
    placeTrap(coords.x, coords.y);
});

canvas.addEventListener('mousemove', (event) => {
    const coords = getCanvasCoordinates(event);
    mousePos.x = coords.x;
    mousePos.y = coords.y;
    isMouseOnCanvas = true;
});

canvas.addEventListener('mouseenter', () => {
    isMouseOnCanvas = true;
});

canvas.addEventListener('mouseleave', () => {
    isMouseOnCanvas = false;
});

// Keyboard shortcuts
document.addEventListener('keydown', (event) => {
    // Don't trigger shortcuts if typing in an input field
    if (event.target.tagName === 'INPUT' || event.target.tagName === 'TEXTAREA') {
        return;
    }

    switch (event.key) {
        case '1':
            event.preventDefault();
            selectTrapByIndex(0);
            break;
        case '2':
            event.preventDefault();
            selectTrapByIndex(1);
            break;
        case '3':
            event.preventDefault();
            selectTrapByIndex(2);
            break;
        case ' ':
        case 'Enter':
            event.preventDefault();
            startGame();
            break;
        case 'Escape':
            event.preventDefault();
            deselectTrap();
            break;
    }
});

function selectTrapByIndex(index) {
    const trapBtn = trapButtons[index];
    if (trapBtn) {
        const trapType = trapBtn.dataset.trap;
        selectTrap(trapType, trapBtn);
    }
}

function deselectTrap() {
    document.querySelectorAll('.trap-btn').forEach(b => b.classList.remove('selected'));
    selectedTrap = null;
}

// Button click event listeners
trapButtons.forEach((btn, index) => {
    btn.addEventListener('click', () => {
        const trapType = btn.dataset.trap;
        selectTrap(trapType, btn);
    });
});

startBtn.addEventListener('click', startGame);
nextLevelBtn.addEventListener('click', nextLevel);

// Continuous render loop for smooth preview updates
function renderLoop() {
    render();
    requestAnimationFrame(renderLoop);
}

// Initial render and connect
renderLoop();
connect();
