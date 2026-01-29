const canvas = document.getElementById('gameCanvas');
const ctx = canvas.getContext('2d');
const startBtn = document.getElementById('startBtn');
const nextLevelBtn = document.getElementById('nextLevelBtn');
const levelDisplay = document.getElementById('level');
const pointsDisplay = document.getElementById('points');
const timeDisplay = document.getElementById('time');
const statusDisplay = document.getElementById('status');
const trapButtons = document.querySelectorAll('.trap-btn');

let socket = null;
let gameState = null;
let selectedTrap = null;

// Select trap function (called from HTML onclick)
function selectTrap(trapType, btn) {
    console.log('Selecting trap:', trapType);
    document.querySelectorAll('.trap-btn').forEach(b => b.classList.remove('selected'));
    btn.classList.add('selected');
    selectedTrap = trapType;
}

// Colors
const COLORS = {
    background: '#1a1a2e',
    platform: '#4a5568',
    runner: '#ff6b6b',
    runnerDead: '#666',
    goal: '#6bcb77',
    spike: '#ff6b6b',
    bouncePad: '#4ecdc4',
    slowZone: '#a855f7'
};

// Connect to WebSocket
function connect() {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const wsUrl = `${protocol}//${window.location.host}/game`;

    socket = new WebSocket(wsUrl);

    socket.onopen = () => {
        console.log('Connected to game server');
        statusDisplay.textContent = 'Connected - Click START';
    };

    socket.onmessage = (event) => {
        gameState = JSON.parse(event.data);
        updateUI();
        render();
    };

    socket.onclose = () => {
        console.log('Disconnected from game server');
        statusDisplay.textContent = 'Disconnected - Refresh to reconnect';
    };

    socket.onerror = (error) => {
        console.error('WebSocket error:', error);
    };
}

// Send message to server
function send(message) {
    if (socket && socket.readyState === WebSocket.OPEN) {
        socket.send(JSON.stringify(message));
    }
}

// Start/Restart game
function startGame() {
    console.log('Starting game...');
    send({ type: 'START' });
    document.getElementById('startBtn').textContent = 'RESTART';
    nextLevelBtn.style.display = 'none';
}

// Next level
function nextLevel() {
    console.log('Going to next level...');
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

    const trapCosts = { SPIKE: 50, BOUNCE_PAD: 20, SLOW_ZONE: 15 };
    if (gameState.playerPoints < trapCosts[selectedTrap]) {
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
    statusDisplay.style.color = '#ff6b6b';
    clearTimeout(messageTimeout);
    messageTimeout = setTimeout(() => {
        statusDisplay.style.color = '#4d96ff';
        updateUI();
    }, 1500);
}

// Update UI elements
function updateUI() {
    if (!gameState) return;

    levelDisplay.textContent = `Level: ${gameState.level}`;
    pointsDisplay.textContent = `Points: ${gameState.playerPoints}`;
    timeDisplay.textContent = `Time: ${gameState.elapsedTime.toFixed(1)}s`;

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
            if (gameState.level < 5) {
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
    ctx.fillStyle = '#fff';
    ctx.font = '12px Arial';
    ctx.textAlign = 'center';
    ctx.fillText('GOAL', gameState.goal.x + gameState.goal.width / 2, gameState.goal.y + gameState.goal.height / 2 + 4);

    // Draw traps
    gameState.traps.forEach(trap => {
        drawTrap(trap);
    });

    // Draw runner
    const runner = gameState.runner;
    ctx.fillStyle = runner.alive ? COLORS.runner : COLORS.runnerDead;
    ctx.fillRect(runner.x, runner.y, 20, 30);

    // Draw runner face
    if (runner.alive) {
        ctx.fillStyle = '#fff';
        ctx.fillRect(runner.x + 12, runner.y + 8, 4, 4); // Eye
        ctx.fillRect(runner.x + 8, runner.y + 18, 8, 2); // Mouth
    }

    // Draw game over overlay
    if (gameState.status === 'PLAYER_WINS' || gameState.status === 'RUNNER_WINS') {
        drawGameOverOverlay();
    }
}

function drawTrap(trap) {
    const size = 30;

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
            ctx.strokeStyle = '#fff';
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

function drawWaitingScreen() {
    ctx.fillStyle = '#fff';
    ctx.font = '24px Arial';
    ctx.textAlign = 'center';
    ctx.fillText('SABOTEUR', canvas.width / 2, canvas.height / 2 - 20);
    ctx.font = '16px Arial';
    ctx.fillStyle = '#888';
    ctx.fillText('Click START to begin', canvas.width / 2, canvas.height / 2 + 20);
}

function drawGameOverOverlay() {
    ctx.fillStyle = 'rgba(0, 0, 0, 0.7)';
    ctx.fillRect(0, 0, canvas.width, canvas.height);

    ctx.textAlign = 'center';

    if (gameState.status === 'PLAYER_WINS') {
        ctx.fillStyle = '#6bcb77';
        ctx.font = 'bold 48px Arial';

        if (gameState.level >= 5) {
            ctx.fillText('VICTORY!', canvas.width / 2, canvas.height / 2 - 40);
            ctx.font = '24px Arial';
            ctx.fillStyle = '#ffd93d';
            ctx.fillText('You beat all 5 levels!', canvas.width / 2, canvas.height / 2 + 10);
            ctx.font = '18px Arial';
            ctx.fillStyle = '#fff';
            ctx.fillText(`Final Score: ${gameState.playerPoints}`, canvas.width / 2, canvas.height / 2 + 50);
        } else {
            ctx.fillText(`LEVEL ${gameState.level} CLEAR!`, canvas.width / 2, canvas.height / 2 - 20);
            ctx.font = '20px Arial';
            ctx.fillStyle = '#fff';
            ctx.fillText('Click NEXT LEVEL to continue', canvas.width / 2, canvas.height / 2 + 30);
        }
    } else {
        ctx.fillStyle = '#ff6b6b';
        ctx.font = 'bold 48px Arial';
        ctx.fillText('RUNNER ESCAPED!', canvas.width / 2, canvas.height / 2 - 20);
        ctx.font = '20px Arial';
        ctx.fillStyle = '#fff';
        ctx.fillText(`Level ${gameState.level} - Click RETRY`, canvas.width / 2, canvas.height / 2 + 30);
    }
}

// Canvas click listener
canvas.addEventListener('click', (event) => {
    const rect = canvas.getBoundingClientRect();
    const x = event.clientX - rect.left;
    const y = event.clientY - rect.top;
    console.log('Canvas clicked at:', x, y, 'Selected trap:', selectedTrap);
    placeTrap(x, y);
});

// Initial render and connect
console.log('Game script loaded!');
render();
connect();
