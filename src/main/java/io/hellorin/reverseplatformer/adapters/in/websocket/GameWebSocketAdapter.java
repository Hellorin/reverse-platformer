package io.hellorin.reverseplatformer.adapters.in.websocket;

import io.hellorin.reverseplatformer.adapters.in.websocket.dto.GameStateDto;
import io.hellorin.reverseplatformer.application.GameSession;
import io.hellorin.reverseplatformer.application.ports.in.GameUseCase;
import io.hellorin.reverseplatformer.application.ports.in.LevelUseCase;
import io.hellorin.reverseplatformer.application.ports.in.TrapUseCase;
import io.hellorin.reverseplatformer.application.service.GameService;
import io.hellorin.reverseplatformer.application.service.LevelService;
import io.hellorin.reverseplatformer.application.service.TrapService;
import io.hellorin.reverseplatformer.domain.model.TrapType;
import io.hellorin.reverseplatformer.domain.service.PhysicsService;
import io.hellorin.reverseplatformer.domain.service.RunnerAIService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.*;

public class GameWebSocketAdapter extends TextWebSocketHandler {

    private static final long TICK_RATE_MS = 16;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ConcurrentHashMap<String, PlayerSession> sessions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        PlayerSession playerSession = createPlayerSession(session);
        sessions.put(session.getId(), playerSession);

        ScheduledFuture<?> gameLoop = scheduler.scheduleAtFixedRate(
                () -> gameLoop(session.getId()),
                0,
                TICK_RATE_MS,
                TimeUnit.MILLISECONDS
        );
        playerSession.setGameLoop(gameLoop);
    }

    private PlayerSession createPlayerSession(WebSocketSession webSocketSession) {
        GameSession gameSession = new GameSession();

        PhysicsService physicsService = new PhysicsService();
        RunnerAIService runnerAIService = new RunnerAIService();

        GameUseCase gameUseCase = new GameService(gameSession, physicsService, runnerAIService);
        LevelUseCase levelUseCase = new LevelService(gameSession);
        TrapUseCase trapUseCase = new TrapService(gameSession);

        return new PlayerSession(webSocketSession, gameUseCase, levelUseCase, trapUseCase);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        PlayerSession playerSession = sessions.get(session.getId());
        if (playerSession == null) return;

        JsonNode json = objectMapper.readTree(message.getPayload());
        String type = json.get("type").asText();

        switch (type) {
            case "START" -> playerSession.gameUseCase().startGame();
            case "RESTART" -> playerSession.levelUseCase().restartLevel();
            case "NEXT_LEVEL" -> playerSession.levelUseCase().nextLevel();
            case "PLACE_TRAP" -> {
                String trapType = json.get("trapType").asText();
                double x = json.get("x").asDouble();
                double y = json.get("y").asDouble();
                playerSession.trapUseCase().placeTrap(TrapType.valueOf(trapType), x, y);
            }
        }
    }

    private void gameLoop(String sessionId) {
        PlayerSession playerSession = sessions.get(sessionId);
        if (playerSession == null) return;

        WebSocketSession session = playerSession.webSocketSession();

        if (!session.isOpen()) {
            cleanup(sessionId);
            return;
        }

        double deltaTime = TICK_RATE_MS / 1000.0;
        playerSession.gameUseCase().update(deltaTime);

        if (playerSession.gameUseCase().getGameState() != null) {
            try {
                GameStateDto dto = GameStateDto.fromDomain(playerSession.gameUseCase().getGameState());
                String json = objectMapper.writeValueAsString(dto);
                session.sendMessage(new TextMessage(json));
            } catch (IOException e) {
                // Session might be closed
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        cleanup(session.getId());
    }

    private void cleanup(String sessionId) {
        PlayerSession playerSession = sessions.remove(sessionId);
        if (playerSession != null && playerSession.gameLoop() != null) {
            playerSession.gameLoop().cancel(true);
        }
    }

    private static class PlayerSession {
        private final WebSocketSession webSocketSession;
        private final GameUseCase gameUseCase;
        private final LevelUseCase levelUseCase;
        private final TrapUseCase trapUseCase;
        private ScheduledFuture<?> gameLoop;

        public PlayerSession(WebSocketSession webSocketSession, GameUseCase gameUseCase,
                            LevelUseCase levelUseCase, TrapUseCase trapUseCase) {
            this.webSocketSession = webSocketSession;
            this.gameUseCase = gameUseCase;
            this.levelUseCase = levelUseCase;
            this.trapUseCase = trapUseCase;
        }

        public WebSocketSession webSocketSession() { return webSocketSession; }
        public GameUseCase gameUseCase() { return gameUseCase; }
        public LevelUseCase levelUseCase() { return levelUseCase; }
        public TrapUseCase trapUseCase() { return trapUseCase; }
        public ScheduledFuture<?> gameLoop() { return gameLoop; }
        public void setGameLoop(ScheduledFuture<?> gameLoop) { this.gameLoop = gameLoop; }
    }
}
