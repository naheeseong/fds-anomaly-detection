package com.fds.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fds.entity.Transaction;
import com.fds.service.AnomalyDetectionResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * docs/api-spec.md 5절의 ws://.../ws/transactions 실시간 거래 스트림을 처리한다.
 * SUBSCRIBE 메시지로 특정 userId만 구독할 수 있고, 생략하면 전체 거래를 구독한다.
 */
@Slf4j
@Component
public class TransactionWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final Map<String, Long> subscriptions = new ConcurrentHashMap<>();
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    private final AtomicLong totalProcessed = new AtomicLong();
    private final AtomicLong abnormalCount = new AtomicLong();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.put(session.getId(), session);
        log.info("WebSocket 연결: {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
        subscriptions.remove(session.getId());
        log.info("WebSocket 종료: {} ({})", session.getId(), status);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        Map<String, Object> payload = objectMapper.readValue(message.getPayload(), Map.class);
        if ("SUBSCRIBE".equals(payload.get("type")) && payload.get("userId") != null) {
            subscriptions.put(session.getId(), ((Number) payload.get("userId")).longValue());
        }
    }

    public void broadcastTransactionCreated(Transaction transaction) {
        totalProcessed.incrementAndGet();
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("type", "TRANSACTION_CREATED");
        message.put("transaction", toTransactionSummary(transaction));
        send(transaction.getUserId(), message);
        broadcastStatistics();
    }

    public void broadcastAnomalyDetected(Transaction transaction, AnomalyDetectionResult result) {
        abnormalCount.incrementAndGet();
        Map<String, Object> alert = new LinkedHashMap<>();
        alert.put("severity", result.getConfidenceScore() >= 0.6 ? "CRITICAL"
                : result.getConfidenceScore() >= 0.4 ? "WARNING" : "INFO");
        alert.put("message", result.reasonSummary() + " 탐지");
        alert.put("confidenceScore", result.getConfidenceScore());
        alert.put("transaction", toTransactionSummary(transaction));

        Map<String, Object> message = new LinkedHashMap<>();
        message.put("type", "ANOMALY_DETECTED");
        message.put("alert", alert);
        send(transaction.getUserId(), message);
    }

    private void broadcastStatistics() {
        Map<String, Object> statistics = new LinkedHashMap<>();
        statistics.put("totalProcessed", totalProcessed.get());
        statistics.put("abnormalCount", abnormalCount.get());

        Map<String, Object> message = new LinkedHashMap<>();
        message.put("type", "STATISTICS_UPDATED");
        message.put("statistics", statistics);
        broadcastAll(message);
    }

    private Map<String, Object> toTransactionSummary(Transaction transaction) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("id", transaction.getId());
        summary.put("userId", transaction.getUserId());
        summary.put("amount", transaction.getAmount());
        summary.put("merchantName", transaction.getMerchantName());
        summary.put("isAbnormal", transaction.getIsAbnormal());
        summary.put("timestamp", LocalDateTime.now().toString());
        return summary;
    }

    /** userId를 구독 중인 세션과, 전체(미지정) 구독 세션에만 전송한다. */
    private void send(Long userId, Map<String, Object> message) {
        String json = writeJson(message);
        if (json == null) {
            return;
        }
        sessions.forEach((sessionId, session) -> {
            Long subscribedUserId = subscriptions.get(sessionId);
            if (subscribedUserId == null || subscribedUserId.equals(userId)) {
                sendSafely(session, json);
            }
        });
    }

    private void broadcastAll(Map<String, Object> message) {
        String json = writeJson(message);
        if (json == null) {
            return;
        }
        sessions.values().forEach(session -> sendSafely(session, json));
    }

    private String writeJson(Map<String, Object> message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (IOException e) {
            log.error("WebSocket 메시지 직렬화 실패", e);
            return null;
        }
    }

    private void sendSafely(WebSocketSession session, String json) {
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(json));
            }
        } catch (IOException e) {
            log.warn("WebSocket 전송 실패: {}", session.getId(), e);
        }
    }
}
