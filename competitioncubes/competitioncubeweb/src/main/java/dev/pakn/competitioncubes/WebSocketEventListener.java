package dev.pakn.competitioncubes;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListener {
    
    @EventListener
    public void handleWebSocketConnectListener(final SessionConnectedEvent event) {
        System.out.println("New user!");
    }

    @EventListener
    public void handleWebSocketDisconnectListener(final SessionDisconnectEvent event) {
        System.out.println("Bye user!");
    }
}
