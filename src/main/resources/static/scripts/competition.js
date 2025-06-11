//export let roomId = Math.floor(Math.random()*100000)
export let roomId = new URLSearchParams(window.location.search).get("roomId");
export let userId = new URLSearchParams(window.location.search).get("userId");
import { stompClient } from "./comp_connect.js";

let scrambleText = document.getElementById("scramble-text");
let userWins = document.getElementById("user-wins");
let oppWins = document.getElementById("opp-wins");

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
            userWins.textContent=`Wins: ${winData[user]}`;
        }else {
            oppWins.textContent = `Wins: ${winData[user]}`;
        }  
    }
}

