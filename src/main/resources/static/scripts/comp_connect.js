import { roomId, userId, setScramble, matchData, setMatchData } from "./competition.js";
import { setTimerState, setTime } from "./opptimer.js"
import { timerEnabled } from "./timer.js";

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
        if (scrambleData.roomId==roomId && matchData.currentSolver==userId) {
            setScramble(scrambleData.scramble);
        }
    });

    stompClient.subscribe('/room/matches', (matchJson)=> {
        let match = JSON.parse(matchJson);
        setMatchData(match);
        if (match.currentSolver!=userId) {
            setScramble("Waiting for Opponent...");
            timerEnabled=false;
        }else {
            timerEnabled=true;
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