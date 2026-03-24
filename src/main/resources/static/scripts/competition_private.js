//export let roomId = Math.floor(Math.random()*100000)
export let roomId = new URLSearchParams(window.location.search).get("roomId");
export let userId = undefined
import { connectPrivateReceiver, stompClient } from "./private_match_receiver.js";
import { setTimerEnabled, createNotification } from "./timer_private.js";

let scrambleText = document.getElementById("scramble-text");

let userWins = document.getElementById("user-wins");
let oppWins = document.getElementById("opp-wins");

let userao5 = document.getElementById("user-ao5")
let oppao5 = document.getElementById("opp-ao5");

const usernameText = document.getElementById("username");
const oppUsernameText = document.getElementById("opp-username");

let footerDiv = document.getElementById("footer-div");
let numWins = 0;

export let matchData;

export let currentScramble = "";

export let oppId = null;

export let userSettings = undefined;

let user = undefined;

let matchWinner = null;
const matchJoinAudio = document.getElementById("match-join-audio");

await fetch(`/api/get-user-data`).then((response)=> {
    return response.json();
}).then(function(data) {
    userId = data['userId'];
    usernameText.textContent=data.username;
    usernameText.title = data.username;
    user = data;
    let userSettings = null;
    fetch(`/api/get-user-settings/${userId}`).then((resp)=>{
        if (resp.ok)
            return resp.json();
        createNotification("Something went wrong loading your settings, so some things may not work as expected.");
    }).then(settings=>{
        userSettings = settings;
        if (userSettings['inspectionAudio']) {
            matchJoinAudio.play();
        }
    }); 
}).catch(function(err) {
    console.log('Failed to fetch!', err);
});

const rematchButton = document.getElementById("rematch-button");
const rematchText = document.getElementById("rematch-text");

export function setScramble(scramble) {
    if (!matchWinner) {
        if (scramble!=="Waiting for Opponent to solve..." && scramble!=="Waiting for Opponent to confirm solve...") {
            currentScramble = scramble;
        }else{
            currentScramble = "";
        }
        scrambleText.textContent = scramble;
    }
}


fetch(`api/get-match-info/${roomId}`).then(response=>{
        if (response.ok) return response.json();
        createNotification("Something went wrong with this match. Redirecting back to main page...");
        setTimeout(window.location.replace("/"),3000);
    }).then(matchJson=>{
        setMatchData(matchJson);
        if (matchJson.users[0]==userId) {
            oppId = matchJson.users[1];
        }else if (matchJson.users[1]==userId) {
            oppId = matchJson.users[0];
        }

        console.log(userId+" | "+oppId);

        fetch(`/api/public/get-user-data-by-id/${oppId}`).then((response)=> {
            return response.json();
            }).then(function(data) {
                oppUsernameText.textContent = data.username;
                oppUsernameText.title = data.username;
            }).catch(function(err) {
                console.log('Failed to fetch!', err);
            });
    });

export function setMatchData(data) {
    matchData=data;
}

export function setWins(winData) {
    console.log(winData);
    for (var userWin in winData) {
        if (userWin==userId) {
            let newWinCount = winData[userWin];
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
            let newWinCount = winData[userWin];
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
            oppWins.textContent = `Wins: ${newWinCount}`;
        }  
    }
}

export function endMatch(matchData) {
    setTimerEnabled(false);
    let winner = matchData.winner['username'];
    if (matchData.quitUser) {
        scrambleText.innerHTML = `${matchData.quitUser['username']} forfeit the match!<br>${winner} has won the match!`
    }else {
        scrambleText.textContent = `${winner} has won the match!`;
    }

    scrambleText.style.color = "lime";

    const homeButton = document.getElementById("home-button");
    homeButton.addEventListener("click", ()=>{
        window.location.href="/";
    });
    homeButton.style.display="block";
    homeButton.classList.add("fade-in-element");
    footerDiv.style.display="flex";
    footerDiv.classList.add("fade-in-element");
    matchWinner=matchData.winner;
    connectPrivateReceiver();

    rematchButton.addEventListener("click",()=>{
        stompClient.publish({
            destination: `/app/rematch/${roomId}`,
            body: JSON.stringify({
                'userId':userId,
                'oppId':oppId,
                'event':matchData['event']
            }),
            headers: {
                receipt: "rematch_sent"
            }
        });
        rematchButton.textContent="Sent ✅";
        rematchButton.disabled = true;
        rematchText.textContent = "Successfully sent rematch request.";
        rematchText.style.display = "block";
    });
}

export function setAo5s(ao5Json) {
    for (var ao5UserId in ao5Json) {
        if (ao5Json[ao5UserId]) {
            if (ao5UserId==userId) {
                userao5.textContent = `ao5: ${ao5Json[ao5UserId]}`;
            }else {
                oppao5.textContent = `ao5: ${ao5Json[ao5UserId]}`;
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