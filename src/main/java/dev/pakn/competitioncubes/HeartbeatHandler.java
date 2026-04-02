package dev.pakn.competitioncubes;

import java.util.concurrent.ConcurrentHashMap;

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

    @PostConstruct
    public void init() {
        staticSimpMessagingTemplate = simpMessagingTemplate;
    }
    
    @Scheduled(fixedRate = 5000)
    private void sendPing() {
        simpMessagingTemplate.convertAndSend("/room/ping","");

        cleanUpConnections();
    }

    @MessageMapping("/pong/{userId}")
    public void receivePong(@Header("simpSessionId") String sessionId, @DestinationVariable int userId, boolean matchConnection) {
        connectedMap.put(userId, new UserWebSocketConnection(userId, sessionId, 5000, matchConnection));
    }

    public static ConcurrentHashMap<Integer, UserWebSocketConnection> getCurrentConnections() {
        cleanUpConnections();
        return connectedMap;
    }

    private static void cleanUpConnections() {
        long currentTime = System.currentTimeMillis();
        Integer[] connectedUserIds = connectedMap.keySet().toArray(new Integer[0]);
        for (int i=0;i<connectedUserIds.length;i++) {
            Integer userId = connectedUserIds[i];
            UserWebSocketConnection connection = connectedMap.get(userId);
            if (currentTime - connection.getLastSeen() > connection.getDisconnectTime()) { //default: 1 ping
                connectedMap.remove(userId);
                MatchFinder.removeFromWaitingList(connection.getUserId());
                User disconnectedUser = DBController.getUserByIDList(connection.getUserId());
                Match match = disconnectedUser.getCurrentMatch();
                if (match!=null) {
                    if (match!=null) {
                        match.setQuitUser(disconnectedUser);
                        for (int matchUserId:match.getUsers()) {
                            if (matchUserId!=userId)
                                match.setWinner(matchUserId);
                        }
                        MatchController.sendMatchData(match);
                    }
                }
                disconnectedUser.setCurrentMatch(null);
            }
        }
    }

    public static void sendPing(int userId) {
        staticSimpMessagingTemplate.convertAndSend("/room/ping/"+userId,"");
    }

    public static void checkHeartbeat(int userId, int disconnectTime) {
        //kind of ugly because we have to remove then put. unfortunately sets are designed to be this way, such that we cannot replace an "equivalent" object in the set (equivalency defined by the equals and hashCode methods)
        connectedMap.put(userId, new UserWebSocketConnection(userId, null, disconnectTime, false));
        staticSimpMessagingTemplate.convertAndSend("/room/ping/"+userId,"");
        logger.info(connectedMap.toString());
    }

    public static boolean removeConnection(int userId) {
        return connectedMap.remove(userId)!=null;
    }
}
