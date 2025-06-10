import { roomId, userId } from "./competition.js";
import { setTimerState, setTime } from "./opptimer.js"

export const stompClient = new StompJs.Client({
    brokerURL: `ws://${window.location.host}/user-connect`
});

stompClient.onConnect = (frame)=>{
    console.log("connected: "+ frame);
    stompClient.subscribe('/room/solves', (solveJSON) => {
        let solve = JSON.parse(solveJSON.body)
        if (solve.roomId==roomId && solve.userId!=userId) {
            setTime(solve.time);
        }
    });

    stompClient.subscribe('/room/switchTimer', (timerStateJSON) => {
        let timerState = JSON.parse(timerStateJSON.body)
        if (timerState.roomId==roomId && timerState.userId!=userId) {
            setTimerState(timerState.state);
        }
    });
}

stompClient.onDisconnect = (frame)=>{
    console.log("disconnected: "+frame);
}

stompClient.onWebSocketError = (error) => {
    console.error('Error with websocket', error);
};

stompClient.onStompError = (frame) => {
    console.error('Broker reported error: ' + frame.headers['message']);
    console.error('Additional details: ' + frame.body);
};

stompClient.activate();