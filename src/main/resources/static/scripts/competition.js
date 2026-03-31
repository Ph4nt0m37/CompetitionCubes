//export let roomId = Math.floor(Math.random()*100000)
export let roomId = new URLSearchParams(window.location.search).get("roomId");
export let userId = undefined;
import { setOppTime } from "./opptimer.js";
import { connectPrivateReceiver } from "./private_match_receiver.js";
import { setTimerEnabled, createNotification, setTimerValue } from "./timer.js";

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

let matchWinner = null;
const matchJoinAudio = document.getElementById("match-join-audio");

let user = undefined;

let userElo = -1;
let oppElo = -1;

const searchingUsersText = document.getElementById("searching-users-text");

export let hacked = false;

if (Math.floor(Math.random()*3)==0) {
    const timers = document.querySelectorAll(".timer-div");
    for (const timer of timers) {
        timer.classList.add("timer-april-fools");
        hacked = true;
    }
    document.querySelector("body").style.backgroundColor = "#151414";
    document.documentElement.style.setProperty("--timer-color","#22e422");
}

await fetch(`/api/get-user-data`).then((response)=> {
    return response.json();
}).then(function(data) {
    userId = data['userId'];
    usernameText.textContent=data.username;
    usernameText.title = data.username;
    user = data;
    userSettings = user['userSettings'];
    if (userSettings['matchSounds']) {
        matchJoinAudio.play();
    }
}).catch(function(err) {
    createNotification("Something went wrong loading your settings, so some things may not work as expected.");
    console.log('Failed to fetch!', err);
});

export function setScramble(scramble) {
    if (!matchWinner) {
        if (scramble!=="Waiting for Opponent to solve..." && scramble!=="Waiting for Opponent to confirm solve...") {
            currentScramble = scramble;
            if (!isReload()) {
                fetch("/api/reset-inactivity-timer", {
                    method: "POST",
                    body: JSON.stringify({
                        userId: userId,
                        maxTime: 120
                    }),
                    headers: {
                        "Content-type": "application/json; charset=UTF-8"
                    }
                });
            }
        }else{
            currentScramble = "";
            fetch(`/api/remove-inactivity-timer`, {
                method: "DELETE",
                headers: {
                    "Content-type": "application/json; charset=UTF-8"
                }
            });
        }
        scrambleText.textContent = scramble;
    }
}


fetch(`api/get-match-info/${roomId}`).then(response=>{
        if (response.ok) return response.json();
        createNotification("Something went wrong with this match. Redirecting back to main page...");
        setTimeout(()=>{
            //window.location.replace("/");
        },3000);
    }).then(matchJson=>{
        setMatchData(matchJson);
        console.log(matchJson);
        userElo=user.elos[matchJson.event];
        if (matchJson.users[0]==userId) {
            oppId = matchJson.users[1];
        }else if (matchJson.users[1]==userId) {
            oppId = matchJson.users[0];
        }

        //resetting info if reloaded
        const userSolves = matchJson['userSolves'][String(userId)];
        if (userSolves.length>0)
            setTimerValue((userSolves[userSolves.length-1])['timeString']);

        const oppSolves = matchJson['userSolves'][String(oppId)];
        if (oppSolves.length>0)
            setOppTime((oppSolves[oppSolves.length-1])['timeString']);

        userWins.textContent=`Wins: ${matchJson['userScores'][String(userId)]}`;
        oppWins.textContent=`Wins: ${matchJson['userScores'][String(oppId)]}`;
        

        //getting opp info
        fetch(`/api/public/get-user-data-by-id/${oppId}`).then((response)=> {
            return response.json();
            }).then(function(data) {
                oppElo=data.elos[matchData.event];
                oppUsernameText.textContent=data.username;
                oppUsernameText.title = data.username;
            }).catch(function(err) {
                console.log('Failed to fetch!', err);
            });
    }).catch(error=>{
        console.log(error);
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

export function endMatch(matchData) {
    setTimerEnabled(false);
    let winner = matchData.winner['username'];
    let eloChange = matchData.eloChange;
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
    getWaitingUserCount()
    setInterval(getWaitingUserCount,10000);
    let eloChangeText = document.getElementById("elo-change-text");
    let oppEloChangeText = document.getElementById("opp-elo-change-text");
    matchWinner=matchData.winner;
    if (matchData.winner['userId']==userId) {
        eloChangeText.innerHTML=`ELO: ${userElo}<span style="color:rgb(0,255,0)">>></span>${userElo+eloChange}`;
        oppEloChangeText.innerHTML=`ELO: ${oppElo}<span style="color:rgb(255,0,0)">>></span>${oppElo-eloChange}`;
    }else {
        eloChangeText.innerHTML=`ELO: ${userElo}<span style="color:rgb(255,0,0)">>></span>${userElo-eloChange}`;
        oppEloChangeText.innerHTML=`ELO: ${oppElo}<span style="color:rgb(0,255,0)">>></span>${oppElo+eloChange}`;
    }
    connectPrivateReceiver();
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

function getWaitingUserCount() {
    fetch("/api/waiting-list/333").then((response)=>{
        return response.json();
    }).then((numSearching)=>{
        let userWord = "users";
        if (numSearching==1) userWord = "user";
        searchingUsersText.textContent=`${numSearching} ${userWord} searching for match...`;
    });
}

//reload detection from https://stackoverflow.com/questions/5004978/check-if-page-gets-reloaded-or-refreshed-in-javascript/53307588#53307588
// There is one navigation entry per document
const entry = performance.getEntriesByType('navigation')[0];

function getNavigationType() {
  if (entry && typeof entry.type === 'string') {
    // 'navigate' | 'reload' | 'back_forward' | 'prerender'
    return entry.type;
  }
  // Fallback to the deprecated API (values: 0,1,2,255)
  if (performance.navigation) {
    const t = performance.navigation.type;
    return t === 1 ? 'reload'
         : t === 2 ? 'back_forward'
         : 'navigate';
  }
  return undefined;
}

function isReload() {
    const navType = getNavigationType();
    const isReload = navType === 'reload';
    const isBackForward = navType === 'back_forward';   
    return isReload || isBackForward;
}