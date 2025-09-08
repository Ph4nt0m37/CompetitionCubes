package dev.pakn.competitioncubes;

import java.util.HashMap;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListener {

    HashMap<String, Integer> sessionUserIdMap = new HashMap<>();
    
    @EventListener
    public void handleWebSocketConnectedListener(final SessionConnectedEvent event) {
        
    }

    @EventListener
    public void handleWebSocketConnectListener(final SessionConnectEvent event) {
        StompHeaderAccessor headers = StompHeaderAccessor.wrap(event.getMessage());
        try {
            sessionUserIdMap.put(headers.getSessionId(), Integer.parseInt(headers.getFirstNativeHeader("user_id")));
        }catch (NumberFormatException e) {
            System.out.println("wth how did they get a userId that is not a number???");
            e.printStackTrace();
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(final SessionDisconnectEvent event) {
        int userId = sessionUserIdMap.get(event.getSessionId());
        CompController.removeFromWaitingList(userId);
        System.out.println("removed "+userId+" from waiting list!");
        Match userMatch = MatchController.getCurrentUserMatch(userId);
        if (userMatch!=null) {
            for (int matchUserId:userMatch.getUsers()) {
                if (matchUserId!=userId) userMatch.setWinner(matchUserId);
            }
        }
    }
}
