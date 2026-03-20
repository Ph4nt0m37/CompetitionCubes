
export const timerStates = {
    TIMING: 0,
    INSPECTION: 1,
    STOPPED: 2
};

const Penalty = {
    OK: 0,
    PLUS_2: 1,
    DNF: 2
}

let userId = -1;

let userElo = 100;
let oppElo = 100;

let timerEnabled = true;

export function setTimerEnabled(enabled) {
    timerEnabled=enabled;
}

const matchJoinAudio = document.getElementById("match-join-audio");

let scrambleText = document.getElementById("scramble-text");
const usernameText = document.getElementById("username");

const footerDiv = document.getElementById("footer-div");
const footerHomeButton = document.getElementById("search-button");

footerHomeButton.addEventListener("click", ()=>{
    window.location.href="/";
});

let username = "Username Not Found";

let userWins = document.getElementById("user-wins");
let oppWins = document.getElementById("opp-wins");

await fetch(`/api/get-user-data`).then(async (response)=> {
    return response.json();
    }).then(function(data) {
        userId = data['userId'];
        console.log(data);
        userElo=data.elos['THREE_BY_THREE'];
        console.log(userElo);
        oppElo = userElo;
        username = data.username;
        usernameText.textContent=username;
        return;
    }).catch(function(err) {
        console.log('Failed to fetch!', err);
        return;
    });

setScramble("L' D2 U2 B U2 B2 U2 L2 B' R2 U2 F D' L2 F' L2 F' L' B' D");

let rawTime = 356400000; //99 hours



// Tutorial Box Stuff //

const tutorialBox = document.getElementById("tutorial-box");
const infoText = document.getElementById("info-text");

const tipText = document.getElementById("tip-text");

let currBox = 0;

class TutorialBox {
    constructor(right, top, text, tipText, isButton) {
        this.right = right;
        this.top = top;
        this.text = text;
        this.tipText = tipText;
        this.isButton = isButton;
    }

    show() {
        tutorialBox.style.right = this.right;
        tutorialBox.style.top = this.top;
        infoText.innerHTML = this.text;
        tipText.textContent = this.tipText;
        if (this.isButton) {
            tipText.classList.add("button");
        }else {
            tipText.classList.remove("button");
        }
    }
}

const tutorialBoxes = [
    new TutorialBox("calc(50vw - calc(15rem / 2))","3rem","Before beginning the match, let's go over some important buttons.","Next >",true),
    new TutorialBox("0rem","0rem","^<br>The menu button has a couple of options, such as a forfeit and report user button.","Next >",true),
    new TutorialBox("4rem","65vh","This button allows you to report your opponent's solve. If you find their solve to be suspicious, report it. > > >","Next >",true),
    new TutorialBox("calc(50vw - calc(15rem / 2))","3rem","^<br>Now onto the match. The scramble for this solve will appear at the top. Scramble your cube with this scramble.","Next >",true),
    new TutorialBox("calc(37vw)","20rem","< < < Press space to start inspection. Then, press and hold space to start the timer. Finally, once your finish your solve, press any key to stop the timer.","(Press Space)", false),
    new TutorialBox("calc(42vw)","29rem","< < < Select a penalty or press OK for no penalty.", "(Select Penalty)", false),
    new TutorialBox("calc(50vw - calc(15rem / 2))","calc(50vh - calc(15rem / 2))","Now, your opponent will get the same scramble you did. Wait for them to finish their solve and see who wins!", "(Wait)", false),
    new TutorialBox("calc(50vw - calc(15rem / 2))","15rem","The winner of the match will gain ELO and the loser will lose ELO. ELO is a number that determines your skill and ranking.<br>v v v", "", false),
    
]

tutorialBoxes[currBox].show();

tipText.addEventListener("click",()=>{
    if (tipText.classList.contains("button")) {
        tutorialBoxes[++currBox].show();
    }
});


window.onload = ()=>{
    const userTimer = document.getElementById("user-timer");
    const okButton = document.getElementById("ok-button");

    const penaltiesDiv = document.getElementById("penalties-div");
    const plusTwoButton = document.getElementById("plus-2-button");
    const dnfButton = document.getElementById("dnf-button");
    const penaltyText = document.getElementById("penalty-text");

    const menuButton = document.getElementById("menu-button");
    const actionsPopup = document.getElementById("background-overlay");
    menuButton.addEventListener("click",()=>{
        actionsPopup.style.display="grid";
    });

    let userSettings = null;
    fetch(`/api/get-user-settings/${userId}`).then((resp)=>{
        if (resp.ok)
            return resp.json();
        createNotification("Something went wrong loading your settings, so some things may not work as expected.");
    }).then(settings=>{
        userSettings = settings;
    }); 

    document.addEventListener("keydown",(event)=>{
        if (event.key=="Escape" && forfeitPopup.style.display==="none") {
            if (reportPopup.style.display==="none") {
                actionsPopup.style.display="none";
            }
            //resetting and hiding the report popup
            reportReasonDropdown.children[0].selected = "selected";
            reportPopup.style.display="none";
        }
    });

    const reportSolveButton = document.getElementById("report-solve-button");
    reportSolveButton.addEventListener("click",()=>{
        reportSolveButton.blur();
        createNotification("Successfully reported this solve.");
    });

    let timerState = timerStates.STOPPED;

    let timerInterval = null;

    let canStartTimer = false;
    let spaceDown = false;
    let startSpaceDown = 0;

    let startTime = Date.now();
    let time = "99:00.00";

    let currentPenalty = Penalty.OK;

    const eightSecondsAudio = document.getElementById("8s-audio");
    const twelveSecondsAudio = document.getElementById("12s-audio");

    okButton.addEventListener("click",()=>{
        if (timerState===timerStates.STOPPED) {
            penaltiesDiv.style.display="none";
            penaltyText.style.display="none";
            penaltyText.style.color="#242424";
            publishSolveData("OK");
        }
    });

    plusTwoButton.addEventListener("click",()=>{
        if (timerState===timerStates.STOPPED) {
            penaltiesDiv.style.display="none";
            penaltyText.style.display="block";
            penaltyText.style.color="#d7e233";
            if (currentPenalty!=Penalty.PLUS_2) {
                penaltyText.textContent="+2";
                rawTime+=2000;
                let ms = Math.floor(((rawTime)/10)%100);
                let s = Math.floor(((rawTime)/1000)%60);
                let min = Math.floor((rawTime)/60000);

                if (min) {
                    time = `${min.toString()}:${s.toString().padStart(2,0)}.${(ms).toString().padStart(2,0)}`;;
                } else {
                    time = `${s.toString()}.${(ms).toString().padStart(2,0)}`;
                }
                userTimer.textContent = time;
                publishSolveData("+2");
            }else {
                penaltyText.textContent="+4";
                rawTime+=4000;
                let ms = Math.floor(((rawTime)/10)%100);
                let s = Math.floor(((rawTime)/1000)%60);
                let min = Math.floor((rawTime)/60000);

                if (min) {
                    time = `${min.toString()}:${s.toString().padStart(2,0)}.${(ms).toString().padStart(2,0)}`;;
                } else {
                    time = `${s.toString()}.${(ms).toString().padStart(2,0)}`;
                }
                userTimer.textContent = time;
                publishSolveData("+4");
            }
        }
    });

    dnfButton.addEventListener("click",()=>{
        if (timerState===timerStates.STOPPED) {
            penaltiesDiv.style.display="none";
            penaltyText.style.display="block";
            penaltyText.textContent="DNF";
            penaltyText.style.color="#e23333";
            publishSolveData("DNF");
        }
    });


    document.addEventListener("keydown", e=>{
        console.log(timerEnabled);
        if (timerEnabled) {
            if (e.key===" ") {
                if (timerState===timerStates.STOPPED && !spaceDown) {
                    userTimer.style.color = "rgb(0,255,0)";
                }
                if (!spaceDown) startSpaceDown = Date.now().valueOf();
                spaceDown=true;
                if (timerState===timerStates.INSPECTION) {
                    if (Date.now().valueOf()-startSpaceDown>299) {
                        userTimer.style.color="rgb(0,255,0)";
                        canStartTimer=true;
                    }else{
                        userTimer.style.color="rgb(238, 255, 0)";
                    }
                }
            }
            if (timerState===timerStates.TIMING) {
                clearInterval(timerInterval);
                userTimer.style.color="#242424";

                rawTime = Date.now()-startTime;
                time = calculateTime();
                userTimer.textContent = time;
                

                if (currentPenalty==Penalty.DNF) {
                    publishSolveData("DNF");
                }
                setTimerEnabled(false);
            }
        }
    });

    document.addEventListener("keyup", e=>{
        if (timerState===timerStates.TIMING) {
            timerState = timerStates.STOPPED;
            if (currentPenalty!=Penalty.DNF) {
                penaltiesDiv.style.display="flex";
                tutorialBoxes[5].show();
            }
            spaceDown=false;
            return;
        }
        if (timerEnabled) {
            if (e.key===" ") {
                spaceDown=false;
                if (timerState===timerStates.STOPPED && !spaceDown) {
                    timerState=timerStates.INSPECTION;
                    penaltyText.style.display="none";
                    penaltyText.style.color="#242424";
                    userTimer.style.color = "rgb(255,0,0)";
                    let inspectionTime = 15;
                    userTimer.textContent=inspectionTime.toString();
                    timerInterval = setInterval(()=> {
                        inspectionTime--;
                        if (inspectionTime>0) {
                            userTimer.textContent=inspectionTime.toString();
                            if (userSettings['inspectionAudio']) {
                                if (inspectionTime==7) {
                                    eightSecondsAudio.play();
                                }else if (inspectionTime==3) {
                                    twelveSecondsAudio.play();
                                }
                            }
                        }else if (inspectionTime<1 && inspectionTime>-2) {
                            userTimer.textContent="+2";
                            penaltyText.style.display="block";
                            penaltyText.textContent="+2";
                            penaltyText.style.color="#d7e233";
                            currentPenalty=Penalty.PLUS_2;
                        }else {
                            userTimer.textContent="DNF";
                            penaltyText.style.display="block";
                            penaltyText.textContent="DNF";
                            penaltyText.style.color="#e23333";
                            currentPenalty=Penalty.DNF;
                            clearInterval(timerInterval);
                        }
                    },1000);
                } else if (timerState===timerStates.INSPECTION) {
                    if (!canStartTimer) {
                        userTimer.style.color = "rgb(255,0,0)";
                    }else {
                        clearInterval(timerInterval)
                        canStartTimer = false;
                        timerState = timerStates.TIMING;
                        userTimer.style.color = "#242424";

                        startTime = Date.now();
                        timerInterval = setInterval(()=> {
                            userTimer.textContent=calculateTime();
                        },10);
                    }
                }
            }
        }
    });

    function calculateTime() {
        let currentTime = Date.now();
        let ms = Math.floor(((currentTime-startTime)/10)%100);
        let s = Math.floor(((currentTime-startTime)/1000)%60);
        let min = Math.floor((currentTime-startTime)/60000);

        let time = null;

        if (min) {
            time = `${min.toString()}:${s.toString().padStart(2,0)}.${(ms).toString().padStart(2,0)}`;;
        } else {
            time = `${s.toString()}.${(ms).toString().padStart(2,0)}`;
        }

        return time;
    }
}

const notificationTemplate = document.querySelector(".notification.template");
const notificationBox = document.getElementById("notif-div");

function createNotification(text) {
    const notif = notificationTemplate.cloneNode(true);
    notif.textContent = text;
    notif.classList.remove("template");
    notificationBox.appendChild(notif);
    setTimeout(()=>{
        notif.classList.add("fade-out");
        setTimeout(()=>{
            notif.remove();
        },1500);
    },5000);
}

function setScramble(scramble) {
    scrambleText.textContent = scramble;
}

function publishSolveData(penalty) {
    tutorialBoxes[6].show();
    setScramble("Waiting for Opponent to solve...");
    setTimeout(()=>{
        setOppTimerState(timerStates.INSPECTION);
        setTimeout(()=>{
            setOppTimerState(timerStates.TIMING);
            setTimeout(()=>{
                setOppTimerState(timerStates.STOPPED);
                setScramble("Waiting for Opponent to confirm solve...");
                setTimeout(()=>{
                    oppPenaltyText.style.color="#e23333";
                    oppPenaltyText.textContent="DNF";
                    oppPenaltyText.style.display="block";
                    userWins.textContent=`Wins: 1`;
                    userWins.classList.toggle("wonRound");
                    setTimeout(() => {
                        userWins.classList.toggle("wonRound");
                    }, 10);
                    oppWins.textContent=`Wins: 0`;
                    oppWins.classList.toggle("lostRound");
                    setTimeout(() => {
                        oppWins.classList.toggle("lostRound");
                    }, 10);
                    endMatch()
                },3500);
            },rawTime+(Math.random()*2000));
        },8000);
    },8000);
}

let oppTimer = document.getElementById("opp-timer");
let oppPenaltyText = document.getElementById("penalty-text-opp");
let timerState = null;

let timerInterval = null;

function setOppTimerState(ts) {
    timerState = timerStates.STOPPED;
    timerState = ts;
    if (timerState===timerStates.STOPPED) {
        oppTimer.style.color="#242424";
        clearInterval(timerInterval);
    }else if (timerState===timerStates.INSPECTION) {
        
        oppTimer.style.color="rgb(255, 0, 0)";
        let inspectionTime = 15;
        clearInterval(timerInterval);
        oppTimer.textContent=inspectionTime.toString();
        timerInterval = setInterval(()=> {
            inspectionTime--;
            if (inspectionTime>0) {
                oppTimer.textContent=inspectionTime.toString();
            }else if (inspectionTime<1 && inspectionTime>-2) {
                oppTimer.textContent="+2";
            }else {
                oppTimer.textContent="DNF";
                clearInterval(timerInterval);
            }
        },1000);
        
    }else if (timerState===timerStates.TIMING) {
        oppTimer.style.color="#242424";
        clearInterval(timerInterval);
        let startTime = Date.now();
        timerInterval = setInterval(()=> {
            let ms = Math.floor(((Date.now()-startTime)/10)%100);
            let s = Math.floor(((Date.now()-startTime)/1000)%60);
            let min = Math.floor((Date.now()-startTime)/60000);

            if (min) {
                oppTimer.textContent=`${min.toString()}:${s.toString().padStart(2,0)}.${(ms).toString().padStart(2,0)}`;
            } else {
                oppTimer.textContent=`${s.toString()}.${(ms).toString().padStart(2,0)}`;
            }
        },10);
    }
}

function setOppTime(time) {
    oppTimer.textContent=time;
}

function endMatch() {
    tutorialBoxes[7].show();
    setTimerEnabled(false);
    let eloChange = 15;
    scrambleText.textContent = `${username} has won the match!`;

    scrambleText.style.color = "lime";

    const homeButton = document.getElementById("home-button");
    homeButton.addEventListener("click", ()=>{
        window.location.href="/";
    });
    homeButton.style.display="block";
    homeButton.classList.add("fade-in-element");
    footerDiv.style.display="flex";
    footerDiv.classList.add("fade-in-element");
    let eloChangeText = document.getElementById("elo-change-text");
    let oppEloChangeText = document.getElementById("opp-elo-change-text");
    eloChangeText.innerHTML=`ELO: ${userElo}<span style="color:rgb(0,255,0)">>></span>${userElo+eloChange}`;
    oppEloChangeText.innerHTML=`ELO: ${oppElo}<span style="color:rgb(255,0,0)">>></span>${oppElo-eloChange}`;

}