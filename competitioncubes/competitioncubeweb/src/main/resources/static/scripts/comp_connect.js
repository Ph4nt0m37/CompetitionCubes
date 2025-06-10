import { roomId, userId, setScramble } from "./competition.js";
import { setTimerState, setTime } from "./opptimer.js"

export const stompClient = new StompJs.Client({
    brokerURL: `wss://${window.location.host}/user-connect`
});

stompClient.activate();

stompClient.onConnect = (frame)=>{
    console.log("connected: "+ frame);
    stompClient.subscribe('/room/solves', (solveJSON) => {
        let solve = JSON.parse(solveJSON.body)
        if (solve.roomId==roomId && solve.userId!=userId) {
            setTime(solve.time);
            stompClient.publish({
                destination: "/app/scramble/3x3",
                body: roomId
            });
        }
    });

    stompClient.subscribe('/room/switchTimer', (timerStateJSON) => {
        let timerState = JSON.parse(timerStateJSON.body)
        if (timerState.roomId==roomId && timerState.userId!=userId) {
            setTimerState(timerState.state);
        }
    });

    stompClient.subscribe('/room/scrambles', (scrambleJson) => {
        let scrambleData = JSON.parse(scrambleJson.body)
        if (scrambleData.roomId==roomId) {
            setScramble(scrambleData.scramble);
        }
    });

    stompClient.publish({
                destination: "/app/scramble/3x3",
                body: roomId
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