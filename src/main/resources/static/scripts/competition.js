//export let roomId = Math.floor(Math.random()*100000)
export let roomId = new URLSearchParams(window.location.search).get("roomId");
export let userId = new URLSearchParams(window.location.search).get("userId");
import { setTimerEnabled } from "./timer.js";

let scrambleText = document.getElementById("scramble-text");

let userWins = document.getElementById("user-wins");
let oppWins = document.getElementById("opp-wins");

let userao5 = document.getElementById("user-ao5")
let oppao5 = document.getElementById("opp-ao5");

let numWins = 0;

export let matchData;

export let currentScramble = "";

export function setScramble(scramble) {
    if (scramble!=="Waiting for Opponent...") {
        currentScramble = scramble;
    }else{
        currentScramble = "";
    }
    scrambleText.textContent = scramble;
}


fetch(`${window.location.origin}/get-match-info/${roomId}`).then(response=>{
        return response.json()
    }).then(matchJson=>{
        setMatchData(matchJson);
    });

export function setMatchData(data) {
    matchData=data;
}

export function setWins(winData) {
    for (var user in winData) {
        if (user==userId) {
            let newWinCount = winData[user];
            if (newWinCount>numWins) {
                userWins.classList.toggle("wonRound");
                setTimeout(() => {
                    userWins.classList.toggle("wonRound");
                }, 10);
            }else {
                userWins.classList.toggle("lostRound");
                setTimeout(() => {
                    userWins.classList.toggle("lostRound");
                }, 10);
            }
            numWins = newWinCount;
            userWins.textContent=`Wins: ${numWins}`;
        }else {
            let newWinCount = winData[user];
            if (newWinCount>Number(oppWins.textContent.substring(6))) {
                oppWins.classList.toggle("wonRound");
                setTimeout(() => {
                    oppWins.classList.toggle("wonRound");
                }, 10);
            }else {
                oppWins.classList.toggle("lostRound");
                setTimeout(() => {
                    oppWins.classList.toggle("lostRound");
                }, 10);
            }
            oppWins.textContent = `Wins: ${winData[user]}`;
        }  
    }
}

export function endMatch(winner) {
    setTimerEnabled(false);
    scrambleText.textContent = `${winner} has won the match!`;
    scrambleText.style.color = "lime";
}

export function setAo5s(ao5Json) {
    for (var user in ao5Json) {
        if (ao5Json[user]) {
            if (user==userId) {
                userao5.textContent = `ao5: ${ao5Json[user]}`;
            }else {
                oppao5.textContent = `ao5: ${ao5Json[user]}`;
            }
        }
    }
}

//moved ao5 calculation to server
/*export function calculateAo5s(solveData) {
    for (var user in solveData) {
        let totalTime = 0;
        solveData[user].forEach(time => {
            totalTime+=timeToFloat(time);
        });
        let ao5=
    }
}

function timeToFloat(time) {
    let times = time.replace(" ","").split(":");
    if (times.length>1) {
        let minutes = parseFloat(times[0]);
        let seconds = parseFloat(times[1]);
        return (minutes*60)+seconds;
    }else{
        return parseFloat(times[0]);
    }
}*/

