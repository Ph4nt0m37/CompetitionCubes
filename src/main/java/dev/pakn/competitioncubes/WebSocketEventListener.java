package dev.pakn.competitioncubes;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListener {

    private static Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    HashMap<String, Integer> sessionUserIdMap = new HashMap<>();

    HashMap<Integer, Integer> matchDisconnectTimer = new HashMap<>();
    
    @EventListener
    public void handleWebSocketConnectedListener(final SessionConnectedEvent event) {
        
    }

    //TODO: fix
    @EventListener
    public void handleWebSocketConnectListener(final SessionConnectEvent event) {
        StompHeaderAccessor headers = StompHeaderAccessor.wrap(event.getMessage());
        try {
            //here
            int userId = Integer.parseInt(headers.getFirstNativeHeader("user_id"));
            sessionUserIdMap.put(headers.getSessionId(), userId);
            matchDisconnectTimer.remove(userId);
        }catch (NumberFormatException e) {
            logger.error("wth how did they get a userId that is not a number???",e);
            e.printStackTrace();
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(final SessionDisconnectEvent event) {
        int userId = sessionUserIdMap.get(event.getSessionId());
        MatchFinder.removeFromWaitingList(userId);
        logger.debug("removed "+userId+" from waiting list!");
        Match userMatch = DBController.getUsers().get(userId).getCurrentMatch();
        if (userMatch!=null && userMatch.getWinner()==null) matchDisconnectTimer.put(userId, 3);
    }

    @Scheduled(fixedRate = 1000)
    public void decrementDisconnect() {
        Integer[] matchDisconnectArr = matchDisconnectTimer.keySet().toArray(new Integer[0]);
        for (Integer userId:matchDisconnectArr) {
            try {
                matchDisconnectTimer.put(userId,matchDisconnectTimer.get(userId)-1);
                Match userMatch = DBController.getUsers().get(userId).getCurrentMatch();
                if (userMatch!=null && matchDisconnectTimer.get(userId)<=0) {
                    matchDisconnectTimer.remove(userId);
                    for (int matchUserId:userMatch.getUsers()) {
                        if (matchUserId!=(int) userId) {
                            User quitUser = DBController.getUsers().get(userId);
                            userMatch.setQuitUser(quitUser);
                            quitUser.setCurrentMatch(null);
                            userMatch.setWinner(matchUserId);
                            MatchController.sendMatchData(userMatch);
                        }
                    }
                    
                }
            }catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
}
