import { stompClient } from "./comp_connect.js";
import { currentScramble, roomId, userId } from "./competition.js";
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

let timerEnabled = true;

export function setTimerEnabled(enabled) {
    timerEnabled=enabled;
}

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

    const forfeitPopup = document.getElementById("forfeit-confirm-popup");
    forfeitPopup.style.display="none";
    const forfeitButton = document.getElementById("forfeit-button");
    forfeitButton.addEventListener("click",(event)=>{
        forfeitPopup.style.display="flex";
    });

    const forfeitConfirmButton = document.getElementById("forfeit-confirm");
    forfeitConfirmButton.addEventListener("click",()=>{
        actionsPopup.style.display="none";
        forfeitPopup.style.display="none";
        fetch("/api/forfeit-match", {
            method: "POST",
            body: JSON.stringify({
                userId: userId,
            }),
            headers: {
                "Content-type": "application/json; charset=UTF-8"
            }
        });
    });

    const forfeitDenyButton = document.getElementById("forfeit-deny");
    forfeitDenyButton.addEventListener("click",()=>{
        forfeitPopup.style.display="none";
    });

    actionsPopup.addEventListener("click",(event)=>{
        if (event.target===event.currentTarget && forfeitPopup.style.display==="none") {
            if (reportPopup.style.display==="none") {
                actionsPopup.style.display="none";
            }
            //resetting and hiding the report popup
            reportReasonDropdown.children[0].selected = "selected";
            reportPopup.style.display="none";
        }
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

    const reportReasonDropdown = document.getElementById("report-reason-dropdown");

    const reportPopup = document.getElementById("report-popup")
    const reportUserButtons = document.getElementById("report-user-button");
    reportUserButtons.addEventListener("click",()=>{
        if (forfeitPopup.style.display==="none")
            reportPopup.style.display="flex";
    });

    const closeReportButton = document.getElementById("close-report-button");
    closeReportButton.addEventListener("click",()=>{
        //resetting and hiding the report popup
        reportReasonDropdown.children[0].selected = "selected";
        reportPopup.style.display="none";
    });

    const closeMenuButton = document.getElementById("close-menu-button");
    closeMenuButton.addEventListener("click",()=>{
        if (forfeitPopup.style.display==="none") {
            actionsPopup.style.display="none";
            reportPopup.style.display="none";
        }
    });

    let timerState = timerStates.STOPPED;

    let timerInterval = null;

    let canStartTimer = false;
    let spaceDown = false;
    let startSpaceDown = 0;

    let startTime = Date.now();
    let rawTime = 356400000; //99 hours
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
                    if (Date.now().valueOf()-startSpaceDown>499) {
                        userTimer.style.color="rgb(0,255,0)";
                        canStartTimer=true;
                    }else{
                        userTimer.style.color="rgb(238, 255, 0)";
                    }
                }
            }
            if (timerState===timerStates.TIMING) {
                clearInterval(timerInterval);
                fetch("/api/reset-inactivity-timer", {
                    method: "POST",
                    body: JSON.stringify({
                        userId: userId,
                        maxTime: 60
                    }),
                    headers: {
                        "Content-type": "application/json; charset=UTF-8"
                    }
                });
                userTimer.style.color="black";

                rawTime = Date.now()-startTime;
                time = calculateTime();
                userTimer.textContent = time;
                
                stompClient.publish({
                    destination: "/app/switchTimer",
                    body: JSON.stringify({
                        'roomId':roomId,
                        'state':timerStates.STOPPED,
                        'userId':userId
                    })
                });

                if (currentPenalty!=Penalty.DNF) {
                    stompClient.publish({
                        destination: "/app/solveCompleted",
                        body: JSON.stringify({
                            'roomId':roomId,
                            'time':time,
                            'userId':userId
                        })
                    });
                }else {
                    publishSolveData("DNF");
                }
                setTimerEnabled(false);
            }
        }
    });

    document.addEventListener("keyup", e=>{
        if (timerState===timerStates.TIMING) {
            timerState = timerStates.STOPPED;
            if (currentPenalty!=Penalty.DNF) penaltiesDiv.style.display="flex";
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
                    stompClient.publish({
                        destination: "/app/switchTimer",
                        body: JSON.stringify({
                            'roomId':roomId,
                            'state':timerStates.INSPECTION,
                            'userId':userId
                        })
                    });
                    let inspectionTime = 15;
                    userTimer.textContent=inspectionTime.toString();
                    timerInterval = setInterval(()=> {
                        inspectionTime--;
                        if (inspectionTime>0) {
                            userTimer.textContent=inspectionTime.toString();
                            if (inspectionTime==7) {
                                eightSecondsAudio.play();
                            }else if (inspectionTime==3) {
                                twelveSecondsAudio.play();
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
                        userTimer.style.color = "black";
                        fetch("/api/reset-inactivity-timer", {
                            method: "POST",
                            body: JSON.stringify({
                                userId: userId,
                                maxTime: 2147483647
                            }),
                            headers: {
                                "Content-type": "application/json; charset=UTF-8"
                            }
                        });
                        //sending start data
                        stompClient.publish({
                            destination: "/app/switchTimer",
                            body: JSON.stringify({
                                'roomId':roomId,
                                'state':timerStates.TIMING,
                                'userId':userId
                            })
                        });

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

    function publishSolveData(penalty) {
        stompClient.publish({
            destination: "/app/solveData",
            body: JSON.stringify({
                'roomId':roomId,
                'time':time,
                'penalty':penalty,
                'scramble': currentScramble,
                'userId': userId
            })
        });
        setTimeout(() => {
            stompClient.publish({
                destination: "/app/update-match",
                body: JSON.stringify({
                    'roomId':roomId,
                    'command':"solveFinished"
                })
            });
        }, 100);
    }
}