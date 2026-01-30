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
    nextLevelBtn.classList.add('hidden');
}

// Next level
function nextLevel() {
    send({ type: 'NEXT_LEVEL' });
    nextLevelBtn.classList.add('hidden');
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
            nextLevelBtn.classList.add('hidden');
            break;
        case 'RUNNING':
            const speedPercent = Math.round(gameState.speedMultiplier * 100);
            statusDisplay.textContent = `Stop the runner! (Speed: ${speedPercent}%)`;
            nextLevelBtn.classList.add('hidden');
            break;
        case 'PLAYER_WINS':
            if (gameState.level < MAX_LEVEL) {
                statusDisplay.textContent = `Level ${gameState.level} complete!`;
                nextLevelBtn.classList.remove('hidden');
            } else {
                statusDisplay.textContent = 'YOU BEAT ALL LEVELS!';
                nextLevelBtn.classList.add('hidden');
            }
            startBtn.textContent = 'RESTART';
            break;
        case 'RUNNER_WINS':
            statusDisplay.textContent = 'Runner escaped! Try again.';
            startBtn.textContent = 'RETRY';
            nextLevelBtn.classList.add('hidden');
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

    // Draw platforms with depth
    gameState.platforms.forEach(platform => {
        if (platform.active) {
            // Add shadow
            ctx.shadowColor = 'rgba(0, 0, 0, 0.5)';
            ctx.shadowBlur = 10;
            ctx.shadowOffsetX = 0;
            ctx.shadowOffsetY = 4;

            // Draw platform with gradient
            const platformGradient = ctx.createLinearGradient(
                platform.x, platform.y,
                platform.x, platform.y + platform.height
            );
            platformGradient.addColorStop(0, '#5a6578');
            platformGradient.addColorStop(1, '#4a5568');
            ctx.fillStyle = platformGradient;
            ctx.fillRect(platform.x, platform.y, platform.width, platform.height);

            // Add highlight on top
            ctx.shadowBlur = 0;
            ctx.fillStyle = 'rgba(255, 255, 255, 0.1)';
            ctx.fillRect(platform.x, platform.y, platform.width, 2);
        }
    });

    // Reset shadow
    ctx.shadowBlur = 0;
    ctx.shadowOffsetX = 0;
    ctx.shadowOffsetY = 0;

    // Draw goal with glow
    ctx.shadowColor = 'rgba(107, 203, 119, 0.8)';
    ctx.shadowBlur = 30;
    ctx.shadowOffsetX = 0;
    ctx.shadowOffsetY = 0;

    const goalGradient = ctx.createLinearGradient(
        gameState.goal.x, gameState.goal.y,
        gameState.goal.x, gameState.goal.y + gameState.goal.height
    );
    goalGradient.addColorStop(0, '#8bdb9d');
    goalGradient.addColorStop(1, '#6bcb77');
    ctx.fillStyle = goalGradient;
    ctx.fillRect(gameState.goal.x, gameState.goal.y, gameState.goal.width, gameState.goal.height);

    // Add animated pulse border
    const time = Date.now() / 1000;
    const pulseAlpha = Math.sin(time * 3) * 0.3 + 0.7;
    ctx.strokeStyle = `rgba(255, 255, 255, ${pulseAlpha})`;
    ctx.lineWidth = 3;
    ctx.strokeRect(gameState.goal.x, gameState.goal.y, gameState.goal.width, gameState.goal.height);

    ctx.shadowBlur = 5;
    ctx.fillStyle = COLORS.white;
    ctx.font = 'bold ' + GAME_CONFIG.FONTS.GOAL_LABEL;
    ctx.textAlign = 'center';
    ctx.fillText('GOAL', gameState.goal.x + gameState.goal.width / 2, gameState.goal.y + gameState.goal.height / 2 + 4);

    // Reset shadow
    ctx.shadowBlur = 0;

    // Draw traps
    gameState.traps.forEach(trap => {
        drawTrap(trap);
    });

    // Draw runner with effects
    const runner = gameState.runner;

    if (runner.alive) {
        // Add glow for alive runner
        ctx.shadowColor = 'rgba(255, 107, 107, 0.8)';
        ctx.shadowBlur = 20;
        ctx.shadowOffsetX = 0;
        ctx.shadowOffsetY = 0;

        // Draw runner with gradient
        const runnerGradient = ctx.createLinearGradient(
            runner.x, runner.y,
            runner.x, runner.y + GAME_CONFIG.RUNNER.HEIGHT
        );
        runnerGradient.addColorStop(0, '#ff8787');
        runnerGradient.addColorStop(1, '#ff6b6b');
        ctx.fillStyle = runnerGradient;
    } else {
        // Dead runner - no glow
        ctx.shadowBlur = 5;
        ctx.shadowColor = 'rgba(0, 0, 0, 0.5)';
        ctx.fillStyle = COLORS.runnerDead;
    }

    ctx.fillRect(runner.x, runner.y, GAME_CONFIG.RUNNER.WIDTH, GAME_CONFIG.RUNNER.HEIGHT);

    // Reset shadow
    ctx.shadowBlur = 0;
    ctx.shadowOffsetX = 0;
    ctx.shadowOffsetY = 0;

    // Draw runner face
    if (runner.alive) {
        // Eye with shine
        ctx.fillStyle = COLORS.white;
        ctx.fillRect(
            runner.x + GAME_CONFIG.RUNNER.EYE_OFFSET_X,
            runner.y + GAME_CONFIG.RUNNER.EYE_OFFSET_Y,
            GAME_CONFIG.RUNNER.EYE_SIZE,
            GAME_CONFIG.RUNNER.EYE_SIZE
        );

        // Mouth with better shape
        ctx.fillStyle = COLORS.white;
        ctx.fillRect(
            runner.x + GAME_CONFIG.RUNNER.MOUTH_OFFSET_X,
            runner.y + GAME_CONFIG.RUNNER.MOUTH_OFFSET_Y,
            GAME_CONFIG.RUNNER.MOUTH_WIDTH,
            GAME_CONFIG.RUNNER.MOUTH_HEIGHT
        );

        // Add highlight on runner body
        ctx.fillStyle = 'rgba(255, 255, 255, 0.3)';
        ctx.fillRect(runner.x + 2, runner.y + 2, GAME_CONFIG.RUNNER.WIDTH - 4, 3);
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
            // Add shadow
            ctx.shadowColor = 'rgba(255, 107, 107, 0.5)';
            ctx.shadowBlur = 15;
            ctx.shadowOffsetX = 0;
            ctx.shadowOffsetY = 4;

            // Draw gradient triangle spikes
            const spikeGradient = ctx.createLinearGradient(trap.x, trap.y, trap.x, trap.y + size);
            spikeGradient.addColorStop(0, '#ff8787');
            spikeGradient.addColorStop(1, '#ff6b6b');
            ctx.fillStyle = spikeGradient;

            ctx.beginPath();
            ctx.moveTo(trap.x, trap.y + size);
            ctx.lineTo(trap.x + size / 2, trap.y);
            ctx.lineTo(trap.x + size, trap.y + size);
            ctx.closePath();
            ctx.fill();

            // Add highlight
            ctx.shadowBlur = 0;
            ctx.strokeStyle = 'rgba(255, 255, 255, 0.3)';
            ctx.lineWidth = 2;
            ctx.beginPath();
            ctx.moveTo(trap.x + size / 2, trap.y);
            ctx.lineTo(trap.x + size * 0.3, trap.y + size * 0.6);
            ctx.stroke();
            break;

        case 'BOUNCE_PAD':
            // Add shadow
            ctx.shadowColor = 'rgba(78, 205, 196, 0.5)';
            ctx.shadowBlur = 15;
            ctx.shadowOffsetX = 0;
            ctx.shadowOffsetY = 4;

            // Draw gradient bounce pad
            const bounceGradient = ctx.createLinearGradient(trap.x, trap.y + size - 10, trap.x, trap.y + size);
            bounceGradient.addColorStop(0, '#6ee7df');
            bounceGradient.addColorStop(1, '#4ecdc4');
            ctx.fillStyle = bounceGradient;
            ctx.fillRect(trap.x, trap.y + size - 10, size, 10);

            // Draw spring lines with glow
            ctx.shadowBlur = 8;
            ctx.shadowColor = 'rgba(255, 255, 255, 0.8)';
            ctx.strokeStyle = COLORS.white;
            ctx.lineWidth = 2.5;
            ctx.beginPath();
            ctx.moveTo(trap.x + 5, trap.y + size - 5);
            ctx.lineTo(trap.x + 15, trap.y + size - 15);
            ctx.lineTo(trap.x + 25, trap.y + size - 5);
            ctx.stroke();
            break;

        case 'SLOW_ZONE':
            // Add outer glow
            ctx.shadowColor = 'rgba(168, 85, 247, 0.6)';
            ctx.shadowBlur = 20;
            ctx.shadowOffsetX = 0;
            ctx.shadowOffsetY = 0;

            // Draw gradient slow zone
            const slowGradient = ctx.createRadialGradient(
                trap.x + size / 2, trap.y + size / 2, 0,
                trap.x + size / 2, trap.y + size / 2, size / 2
            );
            slowGradient.addColorStop(0, 'rgba(168, 85, 247, 0.6)');
            slowGradient.addColorStop(1, 'rgba(168, 85, 247, 0.3)');
            ctx.fillStyle = slowGradient;
            ctx.fillRect(trap.x, trap.y, size, size);

            // Draw pulsing border
            ctx.shadowBlur = 10;
            ctx.strokeStyle = COLORS.slowZone;
            ctx.lineWidth = 2;
            ctx.strokeRect(trap.x, trap.y, size, size);

            // Draw inner pattern
            ctx.shadowBlur = 0;
            ctx.strokeStyle = 'rgba(168, 85, 247, 0.4)';
            ctx.lineWidth = 1;
            const lines = 4;
            for (let i = 1; i < lines; i++) {
                const offset = (size / lines) * i;
                ctx.beginPath();
                ctx.moveTo(trap.x + offset, trap.y);
                ctx.lineTo(trap.x + offset, trap.y + size);
                ctx.stroke();
                ctx.beginPath();
                ctx.moveTo(trap.x, trap.y + offset);
                ctx.lineTo(trap.x + size, trap.y + offset);
                ctx.stroke();
            }
            break;
    }

    // Reset shadow
    ctx.shadowBlur = 0;
    ctx.shadowOffsetX = 0;
    ctx.shadowOffsetY = 0;
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
    // Draw title with glow effect
    ctx.shadowColor = 'rgba(255, 107, 107, 0.8)';
    ctx.shadowBlur = 30;
    ctx.fillStyle = COLORS.white;
    ctx.font = 'bold 48px Orbitron, Arial';
    ctx.textAlign = 'center';
    ctx.fillText('SABOTEUR', canvas.width / 2, canvas.height / 2 - 20);

    // Reset shadow
    ctx.shadowBlur = 0;

    // Draw subtitle
    ctx.font = '20px Rajdhani, Arial';
    ctx.fillStyle = COLORS.gray;
    ctx.fillText('Click START to begin', canvas.width / 2, canvas.height / 2 + 20);

    // Draw pulsing indicator
    const time = Date.now() / 1000;
    const pulseAlpha = Math.sin(time * 2) * 0.4 + 0.6;
    ctx.fillStyle = `rgba(255, 107, 107, ${pulseAlpha})`;
    ctx.font = '16px Rajdhani, Arial';
    ctx.fillText('▼', canvas.width / 2, canvas.height / 2 + 50);
}

function drawGameOverOverlay() {
    const MAX_LEVEL = 5;

    // Draw gradient overlay
    const gradient = ctx.createRadialGradient(
        canvas.width / 2, canvas.height / 2, 0,
        canvas.width / 2, canvas.height / 2, canvas.width / 2
    );
    gradient.addColorStop(0, 'rgba(0, 0, 0, 0.6)');
    gradient.addColorStop(1, 'rgba(0, 0, 0, 0.9)');
    ctx.fillStyle = gradient;
    ctx.fillRect(0, 0, canvas.width, canvas.height);

    ctx.textAlign = 'center';

    if (gameState.status === 'PLAYER_WINS') {
        // Victory state with glow
        ctx.shadowColor = 'rgba(107, 203, 119, 1)';
        ctx.shadowBlur = 40;
        ctx.fillStyle = COLORS.goal;
        ctx.font = 'bold 64px Orbitron, Arial';

        if (gameState.level >= MAX_LEVEL) {
            ctx.fillText('VICTORY!', canvas.width / 2, canvas.height / 2 - 40);

            ctx.shadowBlur = 20;
            ctx.font = '32px Rajdhani, Arial';
            ctx.fillStyle = COLORS.yellow;
            ctx.fillText(`You beat all ${MAX_LEVEL} levels!`, canvas.width / 2, canvas.height / 2 + 10);

            ctx.shadowBlur = 10;
            ctx.font = 'bold 24px Rajdhani, Arial';
            ctx.fillStyle = COLORS.white;
            ctx.fillText(`Final Score: ${gameState.playerPoints}`, canvas.width / 2, canvas.height / 2 + 50);

            // Draw stars for complete victory
            ctx.shadowBlur = 15;
            ctx.fillStyle = COLORS.yellow;
            ctx.font = '40px Arial';
            for (let i = 0; i < 5; i++) {
                const x = canvas.width / 2 - 100 + i * 50;
                const time = Date.now() / 1000;
                const bounce = Math.sin(time * 3 + i) * 5;
                ctx.fillText('★', x, canvas.height / 2 + 100 + bounce);
            }
        } else {
            ctx.fillText(`LEVEL ${gameState.level} CLEAR!`, canvas.width / 2, canvas.height / 2 - 20);

            ctx.shadowBlur = 15;
            ctx.font = '28px Rajdhani, Arial';
            ctx.fillStyle = COLORS.white;
            ctx.fillText('Click NEXT LEVEL to continue', canvas.width / 2, canvas.height / 2 + 30);

            // Draw arrow indicator
            const time = Date.now() / 1000;
            const pulseAlpha = Math.sin(time * 3) * 0.4 + 0.6;
            ctx.shadowBlur = 20;
            ctx.fillStyle = `rgba(107, 203, 119, ${pulseAlpha})`;
            ctx.font = '32px Arial';
            ctx.fillText('▶', canvas.width / 2, canvas.height / 2 + 70);
        }
    } else {
        // Defeat state with red glow
        ctx.shadowColor = 'rgba(255, 107, 107, 1)';
        ctx.shadowBlur = 40;
        ctx.fillStyle = COLORS.runner;
        ctx.font = 'bold 56px Orbitron, Arial';
        ctx.fillText('RUNNER ESCAPED!', canvas.width / 2, canvas.height / 2 - 20);

        ctx.shadowBlur = 15;
        ctx.font = '28px Rajdhani, Arial';
        ctx.fillStyle = COLORS.white;
        ctx.fillText(`Level ${gameState.level} - Click RETRY`, canvas.width / 2, canvas.height / 2 + 30);

        // Draw warning icon
        const time = Date.now() / 1000;
        const pulseAlpha = Math.sin(time * 3) * 0.3 + 0.7;
        ctx.shadowBlur = 25;
        ctx.fillStyle = `rgba(255, 107, 107, ${pulseAlpha})`;
        ctx.font = 'bold 48px Arial';
        ctx.fillText('⚠', canvas.width / 2, canvas.height / 2 + 80);
    }

    // Reset shadow
    ctx.shadowBlur = 0;
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

// Particle background effect
const particlesCanvas = document.getElementById('particles');
const particlesCtx = particlesCanvas.getContext('2d');

particlesCanvas.width = window.innerWidth;
particlesCanvas.height = window.innerHeight;

window.addEventListener('resize', () => {
    particlesCanvas.width = window.innerWidth;
    particlesCanvas.height = window.innerHeight;
});

class Particle {
    constructor() {
        this.x = Math.random() * particlesCanvas.width;
        this.y = Math.random() * particlesCanvas.height;
        this.size = Math.random() * 2 + 1;
        this.speedX = Math.random() * 0.5 - 0.25;
        this.speedY = Math.random() * 0.5 - 0.25;
        this.opacity = Math.random() * 0.5 + 0.2;
    }

    update() {
        this.x += this.speedX;
        this.y += this.speedY;

        if (this.x < 0 || this.x > particlesCanvas.width) this.speedX *= -1;
        if (this.y < 0 || this.y > particlesCanvas.height) this.speedY *= -1;
    }

    draw() {
        particlesCtx.fillStyle = `rgba(255, 107, 107, ${this.opacity})`;
        particlesCtx.beginPath();
        particlesCtx.arc(this.x, this.y, this.size, 0, Math.PI * 2);
        particlesCtx.fill();
    }
}

const particles = [];
for (let i = 0; i < 80; i++) {
    particles.push(new Particle());
}

function animateParticles() {
    particlesCtx.clearRect(0, 0, particlesCanvas.width, particlesCanvas.height);

    particles.forEach(particle => {
        particle.update();
        particle.draw();
    });

    // Draw connections between nearby particles
    for (let i = 0; i < particles.length; i++) {
        for (let j = i + 1; j < particles.length; j++) {
            const dx = particles[i].x - particles[j].x;
            const dy = particles[i].y - particles[j].y;
            const distance = Math.sqrt(dx * dx + dy * dy);

            if (distance < 120) {
                particlesCtx.strokeStyle = `rgba(255, 107, 107, ${0.1 * (1 - distance / 120)})`;
                particlesCtx.lineWidth = 1;
                particlesCtx.beginPath();
                particlesCtx.moveTo(particles[i].x, particles[i].y);
                particlesCtx.lineTo(particles[j].x, particles[j].y);
                particlesCtx.stroke();
            }
        }
    }

    requestAnimationFrame(animateParticles);
}

animateParticles();
