package dev.pakn.competitioncubes;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.PostConstruct;

@RestController
public class HeartbeatHandler {

    private static Logger logger = LoggerFactory.getLogger(HeartbeatHandler.class);

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;
    private static SimpMessagingTemplate staticSimpMessagingTemplate;

    private static ConcurrentHashMap<Integer, UserWebSocketConnection> connectedMap = new ConcurrentHashMap<>();

    private static ScheduledExecutorService cleanupService = Executors.newScheduledThreadPool(1);

    @PostConstruct
    public void init() {
        staticSimpMessagingTemplate = simpMessagingTemplate;
    }

    @Scheduled(fixedRate = 5000)
    private void sendPings() {
        simpMessagingTemplate.convertAndSend("/room/ping", "");

        cleanUpConnections();
    }

    @MessageMapping("/pong/{userId}")
    public void receivePong(@Header("simpSessionId") String sessionId,
            @DestinationVariable int userId, int roomId) {
        if (connectedMap.get(userId) != null && connectedMap.get(userId).getRoomId() >= 0) {
            if (roomId < 0 || connectedMap.get(userId).getRoomId() != (int) roomId) {
                setMatchForfeit(userId, roomId);
            }
        }
        connectedMap.put(userId, new UserWebSocketConnection(userId, sessionId, 5000, roomId));
    }

    public static ConcurrentHashMap<Integer, UserWebSocketConnection> getCurrentConnections() {
        cleanUpConnections();
        return connectedMap;
    }

    private static void cleanUpMatchConnections() {
        Integer[] connectedUserIds = connectedMap.keySet().toArray(new Integer[0]);
        for (int i = 0; i < connectedUserIds.length; i++) {
            Integer userId = connectedUserIds[i];
            if (userId == null) {
                connectedMap.remove(null);
                continue;
            }
            User disconnectedUser = DBController.getUserByIDList(userId);
            if (disconnectedUser != null && disconnectedUser.getCurrentMatch() != null) {
                checkUserConnection(userId);
            }
        }
    }

    private static void cleanUpConnections() {
        Integer[] connectedUserIds = connectedMap.keySet().toArray(new Integer[0]);
        for (int i = 0; i < connectedUserIds.length; i++) {
            Integer userId = connectedUserIds[i];
            if (userId == null) {
                connectedMap.remove(null);
                continue;
            }
            checkUserConnection(userId);
        }
    }

    private static void checkUserConnection(int userId) {
        long currentTime = System.currentTimeMillis();
        UserWebSocketConnection userConnection = connectedMap.get(userId);
        if (currentTime - userConnection.getLastSeen() >= userConnection.getDisconnectTime()) { // default: 1 ping
            connectedMap.remove(userId);
            MatchFinder.removeFromWaitingList(userConnection.getUserId());
            setUserMatchForfeit(userConnection.getUserId());
            return;
        }
    }

    public static void sendPing(int userId) {
        staticSimpMessagingTemplate.convertAndSend("/room/ping/" + userId, "");
    }

    private static void setUserMatchForfeit(int userId) {
        User disconnectedUser = DBController.getUserByIDList(userId);
        if (disconnectedUser != null) {
            Match match = disconnectedUser.getCurrentMatch();
            if (match != null) {
                match.setQuitUser(disconnectedUser);
                for (int matchUserId : match.getUsers()) {
                    if (matchUserId != userId)
                        match.setWinner(matchUserId);
                }
                MatchController.sendMatchData(match);
            }
            disconnectedUser.setCurrentMatch(null);
        }
    }

    private static void setMatchForfeit(int userId, int roomId) {
        User disconnectedUser = DBController.getUserByIDList(userId);
        if (disconnectedUser != null) {
            Match match = MatchController.getMatches().get(roomId);
            if (match != null && match.containsUser(userId)) {
                match.setQuitUser(disconnectedUser);
                for (int matchUserId : match.getUsers()) {
                    if (matchUserId != userId)
                        match.setWinner(matchUserId);
                }
                MatchController.sendMatchData(match);
            }
            disconnectedUser.setCurrentMatch(null);
        }
    }

    public static void checkHeartbeat(int userId, int disconnectTime) {
        // kind of ugly because we have to remove then put. unfortunately sets are
        // designed to be this way, such that we cannot replace an "equivalent" object
        // in the set (equivalency defined by the equals and hashCode methods)
        connectedMap.put(userId, new UserWebSocketConnection(userId, null, disconnectTime, -1));
        staticSimpMessagingTemplate.convertAndSend("/room/ping/" + userId, "");
        cleanupService.schedule(() -> {
            checkUserConnection(userId);
        }, disconnectTime, TimeUnit.MILLISECONDS);
    }

    public static boolean removeConnection(int userId) {
        return connectedMap.remove(userId) != null;
    }
}
