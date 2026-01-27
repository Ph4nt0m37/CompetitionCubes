package dev.pakn.competitioncubes;

import java.util.HashMap;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListener {

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
            System.out.println("user connected: "+userId);
            sessionUserIdMap.put(headers.getSessionId(), userId);
            matchDisconnectTimer.remove(userId);
        }catch (NumberFormatException e) {
            System.out.println("wth how did they get a userId that is not a number???");
            e.printStackTrace();
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(final SessionDisconnectEvent event) {
        int userId = sessionUserIdMap.get(event.getSessionId());
        MatchFinder.removeFromWaitingList(userId);
        System.out.println("removed "+userId+" from waiting list!");
        Match userMatch = DBController.getUsers().get(userId).getCurrentMatch();
        System.out.println(userMatch);
        if (userMatch!=null && userMatch.getWinner()==null) matchDisconnectTimer.put(userId, 3);
    }

    @Scheduled(fixedRate = 1000)
    public void decrementDisconnect() {
        for (Integer userId:matchDisconnectTimer.keySet()) {
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
