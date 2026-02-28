import { roomId, userId, setScramble, matchData, setMatchData, setWins, endMatch, setAo5s } from "./competition.js";
import { startMatchSearchClient } from "./next_match_search.js";
import { setTimerState, setTime, setEarlyTime, setPenalty, clearPenalty } from "./opptimer.js"
import { setTimerEnabled } from "./timer.js";

export const stompClient = new StompJs.Client({
    brokerURL: `wss://${window.location.host}/user-connect`,
    connectHeaders: {
        user_id: sessionStorage.getItem("userId")
    }
});

stompClient.activate();

stompClient.onConnect = (frame)=>{
    console.log("connected: "+ frame);
    stompClient.subscribe(`/room/solves/${roomId}`, (solveJSON) => {
        let solve = JSON.parse(solveJSON.body)
        if (solve.userId!=userId) {
            setTime(solve.time);
            if (solve.penalty!=="OK") setPenalty(solve.penalty);
            stompClient.publish({
                destination: "/app/scramble/3x3",
                body: roomId
            });
        }
    });

    stompClient.subscribe(`/room/solveCompleted/${roomId}`, (solveJSON) => {
        let solve = JSON.parse(solveJSON.body)
        if (solve.userId!=userId) {
            setEarlyTime(solve.time);
        }
    });

    stompClient.subscribe(`/room/switchTimer/${roomId}`, (timerStateJSON) => {
        let timerState = JSON.parse(timerStateJSON.body)
        if (timerState.roomId==roomId && timerState.userId!=userId) {
            setTimerState(timerState.state);
            clearPenalty();
        }
    });

    /*stompClient.subscribe('/room/scrambles', (scrambleJson) => {
        let scrambleData = JSON.parse(scrambleJson.body)
        if (scrambleData.roomId==roomId && matchData.currentSolver==userId) {
            setScramble(scrambleData.scramble);
        }
    });*/

    stompClient.subscribe(`/room/matches/${roomId}`, (matchJson)=> {
        let match = JSON.parse(matchJson.body);
        console.log(match);
        setMatchData(match);
        setAo5s(match.userAo5s);

        if (match.currentSolve!==0 && match.solverIndex==0) {
            setWins(match.userScores);
        }

        console.log(match.winner)

        if (match.winner && match.winner['username']) {
            endMatch(match);
            stompClient.deactivate();
            startMatchSearchClient();
            return;
        }

        if (!match.winner && match.currentSolver!=userId) {
            setScramble("Waiting for Opponent to solve...");
            setTimerEnabled(false);
        }else {
            setScramble(match.currentScramble);
            setTimerEnabled(true);
        }

        
    });

    stompClient.subscribe('/room/found-match', (matchJson)=> {
        let match = JSON.parse(matchJson.body);
        let users = match.users;
        let roomId = match.roomId;
        if (users && users.includes(userId)) {
            fetch(`${window.location.origin}/waiting-list`, {
                method: "DELETE",
                body: JSON.stringify({
                    userId: userId
                }),
                headers: {
                    "Content-type": "application/json; charset=UTF-8"
                }
            }).catch(error=>{
                //do nothing!
            });
            window.location.replace(`${window.location.origin}/competition?roomId=${roomId}&userId=${userId}`);
        }
    });
    
    fetch(`api/get-match-info/${roomId}`).then(response=>{
        return response.json()
    }).then(matchJson=>{
        setMatchData(matchJson);
    }).catch(function(err) {
        console.log('Failed to fetch!', err);
    });

    if (matchData.currentSolver!=userId) {
        setScramble("Waiting for Opponent to solve...");
        setTimerEnabled(false);
    }else {
        setScramble(matchData.currentScramble);
        setTimerEnabled(true);
    }
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